package student.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import student.entity.Student;
import student.secure.Authorize;
import student.service.StudentService;


@Path("/student")
public class StudentResource {

    @Inject
    StudentService studentService;

    @Inject
    Authorize authorize;

    @POST
    @Path("/total-marks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setTotalStudentMarks(@HeaderParam("Authorization") String authorization, Student request) {

        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.saveStudent(request);

    }

    @GET
    @Path("/student-details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTotalStudentDetails(@HeaderParam("Authorization") String authorization) {

        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.getStudentDetails();

    }

    @DELETE
    @Path("/delete-student")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteStudentById(@HeaderParam("Authorization") String authorization, @QueryParam("id") long id) {
        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.deleteStudentById(id) ? studentService.getStudentDetails() : Response.notModified().build();
    }

    @DELETE
    @Path("/cancel-job")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelScheduledJob(@HeaderParam("Authorization") String authorization,
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
    public Response getScheduledJobs(@HeaderParam("Authorization") String authorization) {
        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.getScheduledJobs();
    }
}
