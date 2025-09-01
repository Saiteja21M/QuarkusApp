package student.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import student.entity.Student;
import student.secure.Authorize;
import student.service.StudentService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;


@Path("/student")
public class StudentResource {

    @Inject
    StudentService studentService;

    @Inject
    Authorize authorize;

    @POST
    @Path("/total-marks")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Set total marks for a student", description = "Stores the total marks for a student.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Student marks saved successfully"),
        @APIResponse(responseCode = "400", description = "Bad request or unauthorized")
    })
    public Response setTotalStudentMarks(
            @Parameter(description = "Authorization token", required = true)
            @HeaderParam("Authorization") String authorization,
            @Parameter(description = "Student object containing marks", required = true)
            Student request) {

        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.saveStudent(request);

    }

    @GET
    @Path("/student-details")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all student details", description = "Retrieves details of all students.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Student details fetched successfully"),
        @APIResponse(responseCode = "400", description = "Bad request or unauthorized")
    })
    public Response getTotalStudentDetails(
            @Parameter(description = "Authorization token", required = true)
            @HeaderParam("Authorization") String authorization) {

        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.getStudentDetails();

    }

    @DELETE
    @Path("/delete-student")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Delete a student by ID", description = "Deletes a student record by its ID.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Student deleted successfully"),
        @APIResponse(responseCode = "304", description = "Student not modified"),
        @APIResponse(responseCode = "400", description = "Bad request or unauthorized")
    })
    public Response deleteStudentById(
            @Parameter(description = "Authorization token", required = true)
            @HeaderParam("Authorization") String authorization,
            @Parameter(description = "ID of the student to delete", required = true)
            @QueryParam("id") long id) {
        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.deleteStudentById(id) ? studentService.getStudentDetails() : Response.notModified().build();
    }

    @DELETE
    @Path("/cancel-job")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Cancel a scheduled job", description = "Cancels a scheduled job by name and group.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Job cancelled successfully"),
        @APIResponse(responseCode = "400", description = "Bad request or unauthorized"),
        @APIResponse(responseCode = "404", description = "Job not found or could not be cancelled")
    })
    public Response cancelScheduledJob(
            @Parameter(description = "Authorization token", required = true)
            @HeaderParam("Authorization") String authorization,
            @Parameter(description = "Name of the job to cancel", required = true)
            @QueryParam("jobName") String jobName,
            @Parameter(description = "Group name of the job", required = false)
            @QueryParam("groupName") String groupName) {
        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (jobName == null || jobName.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Job name is required").build();
        }

        // Default group name if not provided
        String group = groupName != null ? groupName : "DEFAULT";

        boolean cancelled = studentService.cancelScheduledJob(jobName, group);

        if (cancelled) {
            return Response.ok("Successfully cancelled job: " + jobName + " in group: " + group).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Job not found or could not be cancelled: " + jobName).build();
        }
    }

    @PUT
    @Path("/disable-job")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Disable a job", description = "Disables a scheduled job by name and group.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Job disabled successfully"),
        @APIResponse(responseCode = "400", description = "Bad request or unauthorized"),
        @APIResponse(responseCode = "404", description = "Job not found or could not be disabled")
    })
    public Response disableJob(@HeaderParam("Authorization") String authorization,
                               @QueryParam("jobName") String jobName,
                               @QueryParam("groupName") String groupName) {
        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (jobName == null || jobName.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Job name is required").build();
        }

        // Default group name if not provided
        String group = groupName != null ? groupName : "DEFAULT";

        boolean disabled = studentService.disableJob(jobName, group);

        if (disabled) {
            return Response.ok("Successfully disabled job: " + jobName + " in group: " + group).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Job not found or could not be disabled: " + jobName).build();
        }
    }

    @PUT
    @Path("/enable-job")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Enable a job", description = "Enables a scheduled job by name and group.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Job enabled successfully"),
        @APIResponse(responseCode = "400", description = "Bad request or unauthorized"),
        @APIResponse(responseCode = "404", description = "Job not found or could not be enabled")
    })
    public Response enableJob(@HeaderParam("Authorization") String authorization,
                              @QueryParam("jobName") String jobName,
                              @QueryParam("groupName") String groupName) {
        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (jobName == null || jobName.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Job name is required").build();
        }

        // Default group name if not provided
        String group = groupName != null ? groupName : "DEFAULT";

        boolean enabled = studentService.enableJob(jobName, group);

        if (enabled) {
            return Response.ok("Successfully enabled job: " + jobName + " in group: " + group).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Job not found or could not be enabled: " + jobName).build();
        }
    }

    @GET
    @Path("/job-status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get job status", description = "Retrieves the status of a scheduled job by name and group.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Job status fetched successfully"),
        @APIResponse(responseCode = "400", description = "Bad request or unauthorized")
    })
    public Response getJobStatus(@HeaderParam("Authorization") String authorization,
                                 @QueryParam("jobName") String jobName,
                                 @QueryParam("groupName") String groupName) {
        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (jobName == null || jobName.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Job name is required").build();
        }

        // Default group name if not provided
        String group = groupName != null ? groupName : "DEFAULT";

        return studentService.getJobStatus(jobName, group);
    }

    @GET
    @Path("/scheduled-jobs")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Get all scheduled jobs", description = "Retrieves all scheduled jobs.")
    @APIResponses({
        @APIResponse(responseCode = "200", description = "Scheduled jobs fetched successfully"),
        @APIResponse(responseCode = "400", description = "Bad request or unauthorized")
    })
    public Response getScheduledJobs(@HeaderParam("Authorization") String authorization) {
        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.getScheduledJobs();
    }
}
