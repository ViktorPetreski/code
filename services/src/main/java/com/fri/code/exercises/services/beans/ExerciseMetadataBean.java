package com.fri.code.exercises.services.beans;

import com.fri.code.exercises.lib.ExerciseMetadata;
import com.fri.code.exercises.lib.InputMetadata;
import com.fri.code.exercises.lib.OutputMetadata;
import com.fri.code.exercises.models.converters.ExerciseMetadataConverter;
import com.fri.code.exercises.models.entities.ExerciseMetadataEntity;
import com.fri.code.exercises.services.config.IntegrationConfiguration;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
public class ExerciseMetadataBean {

    private Logger log = Logger.getLogger(ExerciseMetadataBean.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    private IntegrationConfiguration integrationConfiguration;

    private Client httpClient;

    @Inject
    @DiscoverService(value = "code-inputs")
    private Optional<String> basePath;

    @Inject
    @DiscoverService(value = "code-outputs")
    private Optional<String> outputsPath;
//    private String baseURL;


    @PostConstruct
    void init() {
        httpClient = ClientBuilder.newClient();
    }

    public String getConfig() {
        String response =
                "{" + "\"code-inputs-enabled\": \"%b\"," + "\"code-outputs-enabled\": \"%b\"," + "\"code-inputs-url-exists\": \"%b\",";
        if (basePath.isPresent()) {
            response += String.format("\"code-inputs-url\": \"%s\" \n,", basePath.get());
        }
        if (outputsPath.isPresent()) {
            response += String.format("\"code-outputs-url\": \"%s\"", outputsPath.get());
        }
        response += "}";
        response = String.format(
                response,
                integrationConfiguration.isInputsServiceEnabled(),
                integrationConfiguration.isOutputsServiceEnabled(),
                basePath.isPresent()
                );
        return response;
    }
    public List<ExerciseMetadata> getExercisesMetadata() {
        TypedQuery<ExerciseMetadataEntity> query = em.createNamedQuery("ExerciseMetadataEntity.getAll", ExerciseMetadataEntity.class);
        return query.getResultList().stream().map(ExerciseMetadataConverter::toDto).collect(Collectors.toList());
    }

    public List<ExerciseMetadata> getExercisesMetadataFilter(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0).build();
        return JPAUtils.queryEntities(em, ExerciseMetadataEntity.class, queryParameters).stream().map(ExerciseMetadataConverter::toDto).collect(Collectors.toList());
    }

    public ExerciseMetadata getExerciseMetadata(Integer exerciseID) {
        ExerciseMetadataEntity entity = em.find(ExerciseMetadataEntity.class, exerciseID);

        if (entity == null) {
            throw new NotFoundException("The exercise does not exist");
        }

        ExerciseMetadata exerciseMetadata = ExerciseMetadataConverter.toDto(entity);
        if(integrationConfiguration.isInputsServiceEnabled())
            exerciseMetadata.setInputs(getInputsForExercise(exerciseID));

        return exerciseMetadata;
    }

    public ExerciseMetadata createExerciseMetadata(ExerciseMetadata exerciseMetadata) {
        ExerciseMetadataEntity exerciseMetadataEntity = ExerciseMetadataConverter.toEntity(exerciseMetadata);

        try {
            beginTx();
            em.persist(exerciseMetadataEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (exerciseMetadataEntity.getId() == null) {
            throw new RuntimeException("Entity was not persisted");
        }

        return ExerciseMetadataConverter.toDto(exerciseMetadataEntity);
    }

    public ExerciseMetadata putExerciseMetadata(Integer exerciseID, ExerciseMetadata exerciseMetadata) {
        ExerciseMetadataEntity entity = em.find(ExerciseMetadataEntity.class, exerciseID);
        if (entity == null) {
           return null;
        }

        ExerciseMetadataEntity updatedEntity = ExerciseMetadataConverter.toEntity(exerciseMetadata);

        try {
            beginTx();
            updatedEntity.setId(entity.getId());
            updatedEntity = em.merge(updatedEntity);
            commitTx();
        } catch (Exception e) {
            log.severe(e.getMessage());
            rollbackTx();
        }

        return ExerciseMetadataConverter.toDto(updatedEntity);
    }

    public boolean deleteExerciseMetadata(Integer exerciseID) {
        ExerciseMetadataEntity exerciseMetadataEntity = em.find(ExerciseMetadataEntity.class, exerciseID);
        if (exerciseMetadataEntity != null) {
            try {
                beginTx();
                em.remove(exerciseMetadataEntity);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        }
        else{
            return false;
        }
        return true;
    }

    public List<InputMetadata> getInputsForExercise(Integer exerciseID) {
        if(basePath.isPresent()) {
            try {
                return httpClient
                        .target(String.format("%s/v1/inputs", basePath.get()))
                        .queryParam("exerciseID", exerciseID)
                        .request().get(new GenericType<List<InputMetadata>>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
        }
        return null;
    }

    public  List<OutputMetadata> getOutput(Integer exerciseID) {
        if(integrationConfiguration.isOutputsServiceEnabled() && outputsPath.isPresent()) {
            List<InputMetadata> inputs = getInputsForExercise(exerciseID);
                try {
                    String outputURL = String.format("%s/v1/outputs/results/", outputsPath.get());
                    Response r = httpClient.target(outputURL).request(MediaType.APPLICATION_JSON).post(Entity.entity(inputs, MediaType.APPLICATION_JSON));
                    return r.readEntity(new GenericType<List<OutputMetadata>>(){});
                } catch (WebApplicationException | ProcessingException e) {
                    log.severe(e.getMessage());
                    throw new InternalServerErrorException(e);
                }
        }
        else{
            throw new ServiceUnavailableException();
        }
    }

    public void checkIfSolved(List<OutputMetadata> outputs, Integer exerciseID) {
        int count = 0;
        for(OutputMetadata output : outputs) {
           if(output.getSolved()) count++;
        }
        if (count == outputs.size()) updateSolved(exerciseID);
    }

    public void updateSolved(Integer exerciseID) {
        ExerciseMetadataEntity exerciseMetadataEntity = em.find(ExerciseMetadataEntity.class, exerciseID);
        if (exerciseMetadataEntity == null) {
            throw new NotFoundException("Exercise not found");
        }
        try {
            beginTx();
            exerciseMetadataEntity.setFinished(true);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }
    }

    public List<ExerciseMetadata> getExercisesForSubject(Integer subjectID) {
        TypedQuery<ExerciseMetadataEntity> query = em.createNamedQuery("ExerciseMetadataEntity.getExercisesForSubject", ExerciseMetadataEntity.class).setParameter(1, subjectID);
        return query.getResultList().stream().map(ExerciseMetadataConverter::toDto).collect(Collectors.toList());
    }


    private void beginTx() {
        if (!em.getTransaction().isActive())
            em.getTransaction().begin();
    }

    private void commitTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().commit();
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive())
            em.getTransaction().rollback();
    }


}
