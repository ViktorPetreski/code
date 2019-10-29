package com.fri.code.exercises.api.v1.resources;


import com.fri.code.exercises.lib.ExerciseMetadata;
import com.fri.code.exercises.services.beans.ExerciseMetadataBeam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.ResourceBundle;

@ApplicationScoped
@Path("/exercises")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExerciseMetadataResource {

    public static Response.Status STATUS_OK = Response.Status.OK;
    @Inject
    private ExerciseMetadataBeam exerciseMetadataBeam;

    @Context
    protected UriInfo uriInfo;

    @GET
    public Response getExerciseMetadata() {
        List<ExerciseMetadata> exerciseMetadata = exerciseMetadataBeam.getExercisesMetadata();
        return Response.status(Response.Status.OK).entity(exerciseMetadata).build();
    }

    @GET
    @Path("/{exerciseID}?")
    public Response getExerciseMetadata(@PathParam("exerciseID") Integer exerciseID) {
        ExerciseMetadata exerciseMetadata = exerciseMetadataBeam.getExerciseMetadata(exerciseID);

        if (exerciseMetadata == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.status(Response.Status.OK).entity(exerciseMetadata).build();
    }

    @POST
    public Response createExerciseMetadata(ExerciseMetadata exerciseMetadata) {
        if ((exerciseMetadata.getContent() == null) || (exerciseMetadata.getDescription() == null) || (exerciseMetadata.getSubjectID() == null))
            return Response.status(Response.Status.BAD_REQUEST).build();
        else
            exerciseMetadata = exerciseMetadataBeam.createExerciseMetadata(exerciseMetadata);

        return Response.status(Response.Status.OK).entity(exerciseMetadata).build();

    }

    @DELETE
    @Path("/{exerciseID}")
    public Response deleteExerciseMetadata(@PathParam("exerciseID") Integer exerciseID) {
        if (exerciseMetadataBeam.deleteExerciseMetadata(exerciseID)) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
