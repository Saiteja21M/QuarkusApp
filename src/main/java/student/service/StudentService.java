package student.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.jboss.logging.Logger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import io.quarkus.cache.CacheInvalidateAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import student.entity.Student;
import student.repository.StudentRepository;
import student.scheduler.StudentScheduler;

@ApplicationScoped
public class StudentService {

    @Inject
    StudentRepository studentRepository;

    @Inject
    Logger logger;

    @Inject
    Scheduler scheduler;

    @Transactional
    public Response saveStudent(Student student) {
        studentRepository.persist(student);
        logger.infov("saved {0} student : ", student);
        invalidateAll();

        scheduleJob(StudentScheduler.class, "syncStudents", "io.quarkus.scheduler", student.getStudentId());

        return Response.ok(studentRepository.findById((long) student.getStudentId())).build();
    }

    public Response getStudentDetails() {
        List<Student> studentDetails = studentRepository.listAll();
        logger.infov("fetched {0} student details", studentDetails);
        return Response.ok(studentDetails).build();

    }

    @Transactional
    public void calculateAndSetTotalMarks() {
        studentRepository.listAll()
                .stream().filter(student -> student.getTotalMarks() == 0).forEach(student -> {
                    if (student.getSubject() != null) {
                        int totalMarks = student.getSubject().getEnglish() +
                                student.getSubject().getTelugu() +
                                student.getSubject().getMaths() +
                                student.getSubject().getHindi();
                        student.setTotalMarks(totalMarks);
                        studentRepository.persist(student);
                        logger.infov("Updated total marks for student {0}: {1}", student.getName(), totalMarks);
                    } else {
                        logger.warnv("No subject found for student: {0}", student.getName());
                    }
                });
        invalidateAll();
    }

    @CacheInvalidateAll(cacheName = "student-details")
    public void invalidateAll() {
        // This method will invalidate the cache for all student details
        logger.infov("Invalidated all student details cache");
    }

    @Transactional
    public boolean deleteStudentById(long id) {
        boolean deleted = studentRepository.deleteById(id);
        if (deleted) {
            logger.infov("deleted student id: {0} ", id);
            invalidateAll();
        }
        return deleted;
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void scheduleJob(Class<? extends Job> jobClass, String jobName, String groupName, int studentId) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName, groupName)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "Trigger" + studentId, groupName)
                    .startAt(Date.from(LocalDateTime.now().plusSeconds(120)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()))// Start 5 minutes from now
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            logger.infov("Scheduled job: {0}.{1} with start time: {2}", groupName, jobName, "5 minutes from now");
        } catch (SchedulerException e) {
            logger.error("Failed to schedule job: " + jobName, e);
        }
    }
}
