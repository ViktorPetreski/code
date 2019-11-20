package com.fri.code.exercises.api.v1.resources;


import com.fri.code.exercises.api.v1.dtos.ApiError;
import com.fri.code.exercises.lib.ExerciseMetadata;
import com.fri.code.exercises.services.beans.ExerciseMetadataBean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Path("/exercises")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExerciseMetadataResource {

    public static Response.Status STATUS_OK = Response.Status.OK;
    @Inject
    private ExerciseMetadataBean exerciseMetadataBean;

    @Context
    protected UriInfo uriInfo;

    @GET
    public Response getExerciseMetadata() {
        List<ExerciseMetadata> exerciseMetadata = exerciseMetadataBean.getExercisesMetadata();
        return Response.status(Response.Status.OK).entity(exerciseMetadata).build();
    }

    @GET
    @Path("/config")
    public Response getConfig() {
        String response = exerciseMetadataBean.getConfig();
        return Response.ok(response).build();
    }

    @GET
    @Path("/{exerciseID}")
    public Response getExerciseMetadata(@PathParam("exerciseID") Integer exerciseID) {
        try {
            ExerciseMetadata exerciseMetadata = exerciseMetadataBean.getExerciseMetadata(exerciseID);
            return Response.status(Response.Status.OK).entity(exerciseMetadata).build();
        }catch (Exception e){
            return Response.status(Response.Status.NOT_FOUND).entity(getNotFoundApiError(e.getMessage())).build();
        }
    }

    @POST
    public Response createExerciseMetadata(ExerciseMetadata exerciseMetadata) {
        if ((exerciseMetadata.getContent() == null) || (exerciseMetadata.getDescription() == null) || (exerciseMetadata.getSubjectID() == null)) {
            ApiError error = new ApiError();
            error.setCode(Response.Status.BAD_REQUEST.toString());
            error.setMessage("You are missing some of the parameters");
            error.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }
        else
            exerciseMetadata = exerciseMetadataBean.createExerciseMetadata(exerciseMetadata);

        return Response.status(Response.Status.OK).entity(exerciseMetadata).build();

    }

    @PUT
    @Path("{exerciseID}")
    public Response updateExerciseMetadata(@PathParam("exerciseID") Integer exerciseID, ExerciseMetadata newExercise) {
        newExercise = exerciseMetadataBean.putExerciseMetadata(exerciseID, newExercise);
        if (newExercise == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(getNotFoundApiError("")).build();
        }
        return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    @DELETE
    @Path("/{exerciseID}")
    public Response deleteExerciseMetadata(@PathParam("exerciseID") Integer exerciseID) {
        if (exerciseMetadataBean.deleteExerciseMetadata(exerciseID)) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else{
            return Response.status(Response.Status.NOT_FOUND).entity(getNotFoundApiError("")).build();
        }
    }

    @GET
    @Path("{exerciseID}/outputs")
    public Response getOutputsForExercise(@PathParam("exerciseID") Integer exerciseID) {
        try {
            Map<Integer, Boolean> outputs = exerciseMetadataBean.getOutput(exerciseID);
            return Response.status(STATUS_OK).entity(outputs).build();

        }catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(getNotFoundApiError(e.getMessage())).build();
        }

    }

    private ApiError getNotFoundApiError(String message) {
        ApiError error = new ApiError();
        if(message.isEmpty()) message = "The exercise was not found";
        error.setCode(Response.Status.NOT_FOUND.toString());
        error.setMessage(message);
        error.setStatus(Response.Status.NOT_FOUND.getStatusCode());
        return error;
    }


}
