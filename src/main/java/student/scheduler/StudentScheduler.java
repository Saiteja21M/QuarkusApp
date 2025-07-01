package student.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import student.entity.Student;
import student.repository.StudentRepository;
import student.service.StudentService;

import java.util.List;

@ApplicationScoped
public class StudentScheduler {

    @Inject
    StudentRepository studentRepository;

    @Inject
    StudentService studentService;

    @Inject
    Logger logger;

    @Transactional
    @Scheduled(every = "1m", identity = "calculate_student_total_marks")
    public void calculateStudentTotalMarks() {

        List<Student> studentList = studentRepository.listAll();

        studentList.stream()
                .filter(student -> student.getTotalMarks() == 0)
                .forEach(student -> {
                    int totalMarks = student.getSubject().getEnglish() + student.getSubject().getTelugu() + student.getSubject().getMaths();
                    student.setTotalMarks(totalMarks);
                    studentRepository.persist(student);
                    logger.info("saved total marks = " + student.getTotalMarks() + " for student = " + student.getName());
                });
        studentService.invalidateAll();
    }

}
