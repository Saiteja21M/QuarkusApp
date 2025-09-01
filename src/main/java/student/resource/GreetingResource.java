package student.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("")
    public String hello() {
        return "Hello there from Quarkus REST ";
    }

}
