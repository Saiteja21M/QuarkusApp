package student.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @POST
    @Path("{input1}/{input2}")
    @Produces(MediaType.TEXT_PLAIN)
    public String add(@PathParam("input1") int a, @PathParam("input2") int b) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(a + b);

    }

}
