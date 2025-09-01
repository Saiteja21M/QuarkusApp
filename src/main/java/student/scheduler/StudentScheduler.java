package student.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import student.entity.Student;
import student.repository.StudentRepository;
import student.service.StudentService;

import java.util.List;

@ApplicationScoped
public class StudentScheduler implements Job {

    @Inject
    StudentRepository studentRepository;

    @Inject
    StudentService service;

    @Inject
    Logger logger;

    @Scheduled(cron = "${jobs.student.calculatetotalmarks.cron}", identity = "CALCULATE_STUDENT_MARKS")
    public void calculateStudentTotalMarks() {

        logger.info("Start calculating student total marks");

        service.calculateAndSetTotalMarks();

        logger.info("Finished calculating student total marks");
    }

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            logger.info("Executing student sync job");

            // Example: Sync student data with external system
            List<Student> students = studentRepository.listAll();

            for (Student student : students) {
                // Perform sync operations here
                // This could be API calls, data validation, etc.
                logger.debugv("Syncing student: {0}", student.getName());
            }

            logger.infov("Student sync completed. Processed {0} students", students.size());

        } catch (Exception e) {
            logger.error("Error executing StudentSyncJob", e);
            throw new JobExecutionException(e);
        }
    }
}
