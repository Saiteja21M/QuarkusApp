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
}
