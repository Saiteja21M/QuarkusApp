package student.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@ApplicationScoped
@RegisterRestClient(configKey = "student-api")
@Path("/student/student-details")
public interface StudentClient {

    @GET
    Response getStudentDetails(@HeaderParam("Authorization") String authorization);
}
