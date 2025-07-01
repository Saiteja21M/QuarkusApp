package student.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import student.repository.StudentRepository;
import student.service.StudentService;

import static student.model.QuartzJobModel.Identity.CALCULATE_STUDENT_MARKS;

@ApplicationScoped
public class StudentScheduler {

    @Inject
    StudentRepository studentRepository;

    @Inject
    StudentService service;

    @Inject
    Logger logger;

    @Scheduled(cron = "${jobs.student.calculatetotalmarks.cron}", identity = CALCULATE_STUDENT_MARKS)
    public void calculateStudentTotalMarks() {

        logger.info("Start calculating student total marks");

        service.calculateAndSetTotalMarks();

        logger.info("Finished calculating student total marks");
    }
}
