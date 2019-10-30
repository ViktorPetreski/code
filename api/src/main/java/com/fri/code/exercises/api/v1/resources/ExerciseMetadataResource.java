package com.fri.code.exercises.api.v1.resources;


import com.fri.code.exercises.lib.CompilerOutput;
import com.fri.code.exercises.lib.ExerciseMetadata;
import com.fri.code.exercises.lib.InputMetadata;
import com.fri.code.exercises.services.beans.ExerciseMetadataBeam;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@ApplicationScoped
@Path("/exercises")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ExerciseMetadataResource {

    public static Response.Status STATUS_OK = Response.Status.OK;
    @Inject
    private ExerciseMetadataBeam exerciseMetadataBean;

    @Context
    protected UriInfo uriInfo;

    @GET
    public Response getExerciseMetadata() {
        List<ExerciseMetadata> exerciseMetadata = exerciseMetadataBean.getExercisesMetadata();
        return Response.status(Response.Status.OK).entity(exerciseMetadata).build();
    }

    @GET
    @Path("/{exerciseID}")
    public Response getExerciseMetadata(@PathParam("exerciseID") Integer exerciseID) {
        ExerciseMetadata exerciseMetadata = exerciseMetadataBean.getExerciseMetadata(exerciseID);

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
            exerciseMetadata = exerciseMetadataBean.createExerciseMetadata(exerciseMetadata);

        return Response.status(Response.Status.OK).entity(exerciseMetadata).build();

    }

    @DELETE
    @Path("/{exerciseID}")
    public Response deleteExerciseMetadata(@PathParam("exerciseID") Integer exerciseID) {
        if (exerciseMetadataBean.deleteExerciseMetadata(exerciseID)) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{exerciseID}/outputs")
    public Response getOutputsForExercise(@PathParam("exerciseID") Integer exerciseID) {
        List<InputMetadata> inputs = exerciseMetadataBean.getInputsForExercise(exerciseID);
        ExerciseMetadata exercise = exerciseMetadataBean.getExerciseMetadata(exerciseID);
        List<CompilerOutput> outputs = exerciseMetadataBean.getOutput(inputs, exercise);
        if(!outputs.isEmpty())
            return Response.status(STATUS_OK).entity(outputs).build();
        else return Response.status(Response.Status.NOT_FOUND).build();
    }
}
