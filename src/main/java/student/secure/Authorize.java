package student.secure;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Authorize {

    public boolean authorizeSender(String bearer) {
        return bearer == null || Integer.parseInt(bearer) != 124758;
    }
}
