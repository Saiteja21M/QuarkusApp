package student.resource;

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

        if (!authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.calculateTotalMarks(request);

    }

    @GET
    @Path("/student-details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTotalStudentDetails(@HeaderParam("Authorization") String authorization) {

        if (!authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.getStudentDetails();

    }

    @GET
    @Path("/student-details-byClient")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTotalStudentDetailsByClient(@HeaderParam("Authorization") String authorization) {

        if (!authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.getStudentDetailsByClient();

    }

    @DELETE
    @Path("/delete-student")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteStudentById(@HeaderParam("Authorization") String authorization, @QueryParam("id") long id) {
        if (!authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.deleteStudentById(id) ? studentService.getStudentDetails() : Response.notModified().build();
    }

    @POST
    @Path("/schedule-marks-calculation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response scheduleMarksCalculation(@HeaderParam("Authorization") String authorization,
                                           @QueryParam("studentName") String studentName,
                                           @QueryParam("delaySeconds") int delaySeconds) {
        if (!authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        studentService.scheduleStudentMarksCalculation(studentName, delaySeconds);
        return Response.ok("Scheduled marks calculation for student: " + studentName).build();
    }

    @POST
    @Path("/schedule-daily-report")
    @Produces(MediaType.APPLICATION_JSON)
    public Response scheduleDailyReport(@HeaderParam("Authorization") String authorization,
                                      @QueryParam("cronExpression") String cronExpression) {
        if (!authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // Default to daily at 9 AM if no cron provided
        String cron = cronExpression != null ? cronExpression : "0 0 9 * * ?";
        studentService.scheduleDailyStudentReport(cron);
        return Response.ok("Scheduled daily report with cron: " + cron).build();
    }

    @POST
    @Path("/schedule-sync")
    @Produces(MediaType.APPLICATION_JSON)
    public Response scheduleRecurringSync(@HeaderParam("Authorization") String authorization,
                                        @QueryParam("intervalMinutes") int intervalMinutes) {
        if (!authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        studentService.scheduleRecurringStudentSync(intervalMinutes);
        return Response.ok("Scheduled recurring sync every " + intervalMinutes + " minutes").build();
    }

    @DELETE
    @Path("/cancel-job")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelScheduledJob(@HeaderParam("Authorization") String authorization,
                                     @QueryParam("jobName") String jobName,
                                     @QueryParam("groupName") String groupName) {
        if (!authorize.authorizeSender(authorization)) {
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
}
