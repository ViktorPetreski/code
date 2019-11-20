package com.fri.code.exercises.services.beans;

import com.fri.code.exercises.lib.CompilerOutput;
import com.fri.code.exercises.lib.CompilerReadyInput;
import com.fri.code.exercises.lib.ExerciseMetadata;
import com.fri.code.exercises.lib.InputMetadata;
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
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
public class ExerciseMetadataBeam {

    private Logger log = Logger.getLogger(ExerciseMetadataBeam.class.getName());

    @Inject
    private EntityManager em;

    @Inject
    private IntegrationConfiguration integrationConfiguration;

    private Client httpClient;
//
//    @Inject
//    @DiscoverService(value = "code-inputs")
//    private Optional<String> basePath;
    private String baseURL;

    private String compilerApiUrl;

    @PostConstruct
    void init() {
        httpClient = ClientBuilder.newClient();
        baseURL = integrationConfiguration.getInputsServiceURL();
        compilerApiUrl = "https://api.jdoodle.com/v1/execute";
    }

    public String getConfig() {
        String response =
                "{" + "\"code-inputs-enabled\": \"%b\"," + "\"code-inputs-url-exists\": \"%b\",";
        response += "}";
        response = String.format(
                response,
                integrationConfiguration.isInputsServiceEnabled(),
                integrationConfiguration.getInputsServiceURL()
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
            throw new NotFoundException();
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
//        if(basePath.isPresent()) {
            try {
                return httpClient
                        .target(String.format("%s/v1/inputs", baseURL))
                        .queryParam("exerciseID", exerciseID)
                        .request().get(new GenericType<List<InputMetadata>>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
//        }
//        return null;
    }

    public List<CompilerOutput> getOutput(List<InputMetadata> inputs, ExerciseMetadata exercise) {
//        String content = inputMetadata.getContent();
        List<CompilerOutput> outputs = new ArrayList<>();
        for(InputMetadata in : inputs) {
            String script = "x=input()\nprint(x)"; // treba da e getScript(exerciseID, currentUserID);
            CompilerReadyInput input = new CompilerReadyInput();
            input.setLanguage("python3"); // treba da e setLanguage(getSubject(exerciseID).getLanguage())
            input.setScript(script);
            if(!in.getContent().isEmpty()) input.setStdin(in.getContent());
            input.setVersionIndex("2");
            try {
                CompilerOutput out = httpClient.target(compilerApiUrl).request().post(Entity.entity(input, MediaType.APPLICATION_JSON), CompilerOutput.class);
                outputs.add(out);
            } catch (WebApplicationException | ProcessingException e) {
                log.severe(e.getMessage());
                throw new InternalServerErrorException(e);
            }
        }
        return outputs;
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
