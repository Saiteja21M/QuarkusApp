package student.secure;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Authorize {

    public boolean authorizeSender(String bearer) {
        return Integer.parseInt(bearer) == 124758;
    }
}
