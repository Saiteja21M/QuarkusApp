package student.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import student.entity.Student;
import student.repository.StudentRepository;

import java.util.List;

@ApplicationScoped
public class StudentSyncJob implements Job {

    @Inject
    StudentRepository studentRepository;

    @Inject
    Logger logger;

    @Override
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
