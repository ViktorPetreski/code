package com.fri.code.exercises.api.v1.resources;


import com.fri.code.exercises.api.v1.dtos.ApiError;
import com.fri.code.exercises.lib.ExerciseMetadata;
import com.fri.code.exercises.lib.OutputMetadata;
import com.fri.code.exercises.services.beans.ExerciseMetadataBean;
import com.kumuluz.ee.logs.cdi.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Map;

@Log
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
    @Operation(summary = "Get all exercises details", description = "Returns all exercises.")
    @ApiResponses({
            @ApiResponse(description = "List of all exercises", responseCode = "200", content = @Content(array = @ArraySchema(schema = @Schema(implementation =
                    ExerciseMetadata.class))))
    })
    @Timed
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
    @Operation(summary = "Get exercise details", description = "Returns details for chosen exercise id")
    @ApiResponses({
            @ApiResponse(description = "Exercise details", responseCode = "200", content = @Content(schema = @Schema(implementation =
                    ExerciseMetadata.class))),
            @ApiResponse(description = "Exercise not found", responseCode = "404")
    })
    @Path("/{exerciseID}")
    public Response getExerciseMetadata(@PathParam("exerciseID") Integer exerciseID) {
        try {
            ExerciseMetadata exerciseMetadata = exerciseMetadataBean.getExerciseMetadata(exerciseID);
            return Response.status(Response.Status.OK).entity(exerciseMetadata).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(getNotFoundApiError(e.getMessage())).build();
        }
    }

    @POST
    @Operation(summary = "Add exercise", description = "Adds exercise and returns it")
    @ApiResponses({
            @ApiResponse(description = "New exercise", responseCode = "200", content = @Content(schema = @Schema(implementation =
                    ExerciseMetadata.class))),
            @ApiResponse(description = "Can not create exercise", responseCode = "400")
    })
    public Response createExerciseMetadata(ExerciseMetadata exerciseMetadata) {
        if ((exerciseMetadata.getContent() == null) || (exerciseMetadata.getDescription() == null) || (exerciseMetadata.getSubjectID() == null)) {
            ApiError error = new ApiError();
            error.setCode(Response.Status.BAD_REQUEST.toString());
            error.setMessage("You are missing some of the parameters");
            error.setStatus(Response.Status.BAD_REQUEST.getStatusCode());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        } else
            exerciseMetadata = exerciseMetadataBean.createExerciseMetadata(exerciseMetadata);

        return Response.status(Response.Status.OK).entity(exerciseMetadata).build();

    }

    @PUT
    @Operation(summary = "Update exercise", description = "Updates chosen exercise and returns it")
    @ApiResponses({
            @ApiResponse(description = "Updated exercise", responseCode = "200", content = @Content(schema = @Schema(implementation =
                    ExerciseMetadata.class))),
            @ApiResponse(description = "Exercise not found", responseCode = "404")
    })
    @Path("{exerciseID}")
    public Response updateExerciseMetadata(@PathParam("exerciseID") Integer exerciseID, ExerciseMetadata newExercise) {
        newExercise = exerciseMetadataBean.putExerciseMetadata(exerciseID, newExercise);
        if (newExercise == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(getNotFoundApiError("")).build();
        }
        return Response.status(Response.Status.NOT_MODIFIED).build();
    }

    @DELETE
    @Operation(summary = "Delete exercise", description = "Deletes chosen exercise")
    @ApiResponses({
            @ApiResponse(description = "Exercise deleted", responseCode = "204"),
            @ApiResponse(description = "Exercise not found", responseCode = "404")
    })
    @Path("/{exerciseID}")
    public Response deleteExerciseMetadata(@PathParam("exerciseID") Integer exerciseID) {
        if (exerciseMetadataBean.deleteExerciseMetadata(exerciseID)) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(getNotFoundApiError("")).build();
        }
    }

    @GET
    @Operation(summary = "Get all outputs for exercise", description = "Outputs for specified exercise")
    @ApiResponses({
            @ApiResponse(description = "List of outputs", responseCode = "200", content = @Content(schema = @Schema(implementation =
                    OutputMetadata.class))),
            @ApiResponse(description = "Exercise not found", responseCode = "404"),
            @ApiResponse(description = "Outputs service is not available", responseCode = "503")
    })
    @Path("{exerciseID}/outputs")
    public Response getOutputsForExercise(@PathParam("exerciseID") Integer exerciseID) {
        try {
            List<OutputMetadata> outputs = exerciseMetadataBean.getOutput(exerciseID);
            exerciseMetadataBean.checkIfSolved(outputs, exerciseID);
            return Response.status(STATUS_OK).entity(outputs).build();
        } catch (InternalServerErrorException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(getNotFoundApiError(e.getMessage())).build();
        } catch (ServiceUnavailableException e) {
            ApiError error = new ApiError();
            error.setStatus(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
            error.setCode(Response.Status.SERVICE_UNAVAILABLE.toString());
            error.setMessage("The service is currently disabled");
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(error).build();
        }

    }

    @GET
    @Operation(summary = "Get exercises for specified subject", description = "Returns list of exercises for subject")
    @ApiResponses({
            @ApiResponse(description = "Exercise details", responseCode = "200", content = @Content(schema = @Schema(implementation =
                    ExerciseMetadata.class)))
    })
    @Path("/subject/{subjectID}")
    public Response getExercisesForSubject(@PathParam("subjectID") Integer subjectID) {
        List<ExerciseMetadata> exercises = exerciseMetadataBean.getExercisesForSubject(subjectID);
        return Response.ok(exercises).build();
    }

    private ApiError getNotFoundApiError(String message) {
        ApiError error = new ApiError();
        if (message.isEmpty()) message = "The exercise was not found";
        error.setCode(Response.Status.NOT_FOUND.toString());
        error.setMessage(message);
        error.setStatus(Response.Status.NOT_FOUND.getStatusCode());
        return error;
    }


}
