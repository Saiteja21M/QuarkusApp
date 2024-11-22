package student.service;

import io.quarkus.cache.CacheInvalidateAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import student.client.StudentClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import student.entity.Student;
import student.entity.TvShow;
import org.jboss.logging.Logger;
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

    public Response calculateTotalMarks(Student student) {

        student.setTotalMarks(student.getSubject().getEnglish() + student.getSubject().getHindi() + student.getSubject().getTelugu() + student.getSubject().getMaths());
        student.setTvShow(getStudentFavoriteShow());
        if (studentRepository.saveStudentDetails(student)) {
            logger.infov("saved {0} student : ", student);
            invalidateAll();
        }
        return studentRepository.getStudentDetailsByName(student.getName());
    }

    public Response getStudentDetails() {
        List<Student> studentDetails = studentRepository.getStudentDetails();
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
        boolean deleted = studentRepository.deleteStudentById(id);
        if (deleted) {
            logger.infov("deleted student id: {0} ", id);
            invalidateAll();
        }
        return deleted;
    }
}
