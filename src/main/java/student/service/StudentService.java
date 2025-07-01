package student.service;

import io.quarkus.cache.CacheInvalidateAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import student.client.StudentClient;
import student.entity.Student;
import student.entity.TvShow;
import student.repository.StudentRepository;

import java.util.List;

@ApplicationScoped
public class StudentService {

    @Inject
    StudentRepository studentRepository;

    @Inject
    @RestClient
    StudentClient studentClient;

    @Inject
    Logger logger;

    @Transactional
    public Response calculateTotalMarks(Student student) {

        student.setTvShow(getStudentFavoriteShow());
        studentRepository.persist(student);
        logger.infov("saved {0} student : ", student);
        invalidateAll();

        return Response.ok(studentRepository.findById((long) student.getStudentId())).build();
    }

    public Response getStudentDetails() {
        List<Student> studentDetails = studentRepository.listAll();
        logger.infov("fetched {0} student details", studentDetails);
        return Response.ok(studentDetails).build();

    }

    public TvShow getStudentFavoriteShow() {
        return new TvShow();
    }

    @CacheInvalidateAll(cacheName = "student-details")
    public void invalidateAll() {
    }

    public Response getStudentDetailsByClient() {
        return studentClient.getStudentDetails("124758");
    }

    public boolean deleteStudentById(long id) {
        boolean deleted = studentRepository.deleteById(id);
        if (deleted) {
            logger.infov("deleted student id: {0} ", id);
            invalidateAll();
        }
        return deleted;
    }
}
