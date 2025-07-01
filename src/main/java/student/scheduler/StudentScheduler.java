package student.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import student.repository.StudentRepository;
import student.service.StudentService;

public class StudentScheduler {

    @Inject
    StudentRepository studentRepository;

    @Inject
    StudentService service;

    @Inject
    Logger logger;

    @Scheduled(cron = "${jobs.student.calculatetotalmarks.cron}", identity = "calculate-student-marks")
    public void calculateStudentTotalMarks() {

        logger.info("Start calculating student total marks");

        service.calculateAndSetTotalMarks();

        logger.info("Finished calculating student total marks");
    }
}
