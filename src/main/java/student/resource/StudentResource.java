package student.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.quartz.Scheduler;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import student.entity.Student;
import student.secure.Authorize;
import student.service.StudentService;

@Path("/student")
@Authenticated
public class StudentResource {

    @Inject
    StudentService studentService;

    @Inject
    Authorize authorize;

    @Inject
    Scheduler scheduler;

    @POST
    @Path("/total-marks")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Set total marks for a student", description = "Stores the total marks for a student.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Student marks saved successfully"),
            @APIResponse(responseCode = "400", description = "Bad request or unauthorized")
    })
    public Response setTotalStudentMarks(
            @Parameter(description = "Authorization token", required = true) @HeaderParam("Authorization") String authorization,
            @Parameter(description = "Student object containing marks", required = true) Student request) {

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
            @Parameter(description = "Authorization token", required = true) @HeaderParam("Authorization") String authorization) {

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
            @Parameter(description = "Authorization token", required = true) @HeaderParam("Authorization") String authorization,
            @Parameter(description = "ID of the student to delete", required = true) @QueryParam("id") long id) {
        if (authorize.authorizeSender(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return studentService.deleteStudentById(id) ? studentService.getStudentDetails()
                : Response.notModified().build();
    }
}
