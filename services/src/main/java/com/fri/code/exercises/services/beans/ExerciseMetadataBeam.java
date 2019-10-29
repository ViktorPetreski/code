package com.fri.code.exercises.services.beans;

import com.fri.code.exercises.lib.ExerciseMetadata;
import com.fri.code.exercises.models.converters.ExerciseMetadataConverter;
import com.fri.code.exercises.models.entities.ExerciseMetadataEntity;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@ApplicationScoped
public class ExerciseMetadataBeam {

    private Logger log = Logger.getLogger(ExerciseMetadataBeam.class.getName());

    @Inject
    private EntityManager em;

    private Client httpClient;

    private String baseURL;

    @PostConstruct
    void init() {
        httpClient = ClientBuilder.newClient();
        baseURL = "http://localhost:8081";
    }

//    @SuppressWarnings("JpaQueryApiInspection")
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
