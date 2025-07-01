package student.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import student.entity.Student;
import student.repository.StudentRepository;

import java.util.List;

@ApplicationScoped
public class StudentMarksJob implements Job {

    @Inject
    StudentRepository studentRepository;

    @Inject
    Logger logger;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            String studentName = context.getJobDetail().getJobDataMap().getString("studentName");
            logger.infov("Executing marks calculation job for student: {0}", studentName);

            // Find student by name
            List<Student> students = studentRepository.list("name", studentName);
            
            if (!students.isEmpty()) {
                Student student = students.get(0);
                if (student.getSubject() != null) {
                    int totalMarks = student.getSubject().getEnglish() + 
                                   student.getSubject().getTelugu() + 
                                   student.getSubject().getMaths() + 
                                   student.getSubject().getHindi();
                    student.setTotalMarks(totalMarks);
                    studentRepository.persist(student);
                    logger.infov("Updated total marks for student {0}: {1}", studentName, totalMarks);
                } else {
                    logger.warnv("No subject found for student: {0}", studentName);
                }
            } else {
                logger.warnv("Student not found: {0}", studentName);
            }
        } catch (Exception e) {
            logger.error("Error executing StudentMarksJob", e);
            throw new JobExecutionException(e);
        }
    }
}
