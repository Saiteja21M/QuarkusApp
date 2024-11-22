package student.repository;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import student.entity.Student;

import java.util.List;

@ApplicationScoped
public class StudentRepository {

    @Transactional
    public boolean saveStudentDetails(Student student) {
        student.persist();
        return student.isPersistent();
    }


    @CacheResult(cacheName = "student-details")
    public List<Student> getStudentDetails() {
        return Student.listAll();
    }

    public Response getStudentDetailsByName(String name) {
        return Response.ok(Student.list("name", name)).build();
    }

    @Transactional
    public boolean deleteStudentById(long id) {
        return Student.deleteById(id);
    }
}
