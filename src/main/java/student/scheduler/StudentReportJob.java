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
public class StudentReportJob implements Job {

    @Inject
    StudentRepository studentRepository;

    @Inject
    Logger logger;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            logger.info("Executing daily student report job");

            List<Student> students = studentRepository.listAll();
            
            // Generate report statistics
            long totalStudents = students.size();
            long studentsWithMarks = students.stream()
                    .filter(s -> s.getTotalMarks() > 0)
                    .count();
            
            double averageMarks = students.stream()
                    .filter(s -> s.getTotalMarks() > 0)
                    .mapToInt(Student::getTotalMarks)
                    .average()
                    .orElse(0.0);

            logger.infov("Daily Report - Total Students: {0}, Students with Marks: {1}, Average Marks: {2}", 
                        totalStudents, studentsWithMarks, averageMarks);
            
            // Here you could send email, save to file, etc.
            
        } catch (Exception e) {
            logger.error("Error executing StudentReportJob", e);
            throw new JobExecutionException(e);
        }
    }
}
