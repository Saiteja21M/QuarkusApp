package student.service;

import io.quarkus.cache.CacheInvalidateAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import student.entity.Student;
import student.repository.StudentRepository;
import student.scheduler.StudentScheduler;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

    /**
     * Cancels a scheduled job
     */
    public boolean cancelScheduledJob(String jobName, String groupName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, groupName);
            return scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            logger.error("Failed to cancel job: " + jobName, e);
            return false;
        }
    }

    /**
     * Lists all currently scheduled jobs for debugging
     */
    public Response getScheduledJobs() {
        try {
            List<String> jobInfo = new java.util.ArrayList<>();

            // Get all job groups
            List<String> jobGroups = scheduler.getJobGroupNames();
            logger.infov("Found {0} job groups", jobGroups.size());

            for (String group : jobGroups) {
                jobInfo.add("Job Group: " + group);

                // Get all jobs in this group
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group));

                for (JobKey jobKey : jobKeys) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

                    String jobDescription = String.format(
                            "  Job: %s.%s (Class: %s, Triggers: %d)",
                            jobKey.getGroup(),
                            jobKey.getName(),
                            jobDetail.getJobClass().getSimpleName(),
                            triggers.size()
                    );
                    jobInfo.add(jobDescription);

                    // Add trigger details
                    for (Trigger trigger : triggers) {
                        String triggerInfo = String.format(
                                "    Trigger: %s (Next: %s, State: %s)",
                                trigger.getKey().getName(),
                                trigger.getNextFireTime(),
                                scheduler.getTriggerState(trigger.getKey())
                        );
                        jobInfo.add(triggerInfo);
                    }
                }
            }

            if (jobInfo.isEmpty()) {
                jobInfo.add("No jobs currently scheduled");
            }

            return Response.ok(jobInfo).build();

        } catch (SchedulerException e) {
            logger.error("Failed to list scheduled jobs", e);
            return Response.serverError().entity("Failed to list jobs: " + e.getMessage()).build();
        }
    }

    /**
     * Disables (pauses) a scheduled job - keeps it in database but stops execution
     */
    public boolean disableJob(String jobName, String groupName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, groupName);

            // Check if job exists first
            if (!scheduler.checkExists(jobKey)) {
                logger.warnv("Job does not exist: {0}.{1}", groupName, jobName);
                return false;
            }

            // Pause the job - this disables all triggers for this job
            scheduler.pauseJob(jobKey);
            logger.infov("Successfully disabled job: {0}.{1}", groupName, jobName);
            return true;

        } catch (SchedulerException e) {
            logger.error("Failed to disable job: " + jobName, e);
            return false;
        }
    }

    /**
     * Enables (resumes) a previously disabled job
     */
    public boolean enableJob(String jobName, String groupName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, groupName);

            // Check if job exists first
            if (!scheduler.checkExists(jobKey)) {
                logger.warnv("Job does not exist: {0}.{1}", groupName, jobName);
                return false;
            }

            // Resume the job - this re-enables all triggers for this job
            scheduler.resumeJob(jobKey);
            logger.infov("Successfully enabled job: {0}.{1}", groupName, jobName);
            return true;

        } catch (SchedulerException e) {
            logger.error("Failed to enable job: " + jobName, e);
            return false;
        }
    }

    /**
     * Gets the status of a specific job (NORMAL, PAUSED, BLOCKED, ERROR, NONE)
     */
    public Response getJobStatus(String jobName, String groupName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, groupName);

            if (!scheduler.checkExists(jobKey)) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Job not found: " + jobName + " in group: " + groupName).build();
            }

            // Get all triggers for this job and their states
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            List<java.util.Map<String, Object>> triggerStates = new java.util.ArrayList<>();

            for (Trigger trigger : triggers) {
                Trigger.TriggerState state = scheduler.getTriggerState(trigger.getKey());
                triggerStates.add(java.util.Map.of(
                        "triggerName", trigger.getKey().getName(),
                        "triggerGroup", trigger.getKey().getGroup(),
                        "state", state.toString(),
                        "nextFireTime", trigger.getNextFireTime(),
                        "previousFireTime", trigger.getPreviousFireTime()
                ));
            }

            return Response.ok().entity(java.util.Map.of(
                    "jobName", jobName,
                    "jobGroup", groupName,
                    "triggers", triggerStates
            )).build();

        } catch (SchedulerException e) {
            logger.error("Failed to get job status: " + jobName, e);
            return Response.serverError().entity("Failed to get job status: " + e.getMessage()).build();
        }
    }

    @Transactional(Transactional.TxType.NOT_SUPPORTED)
    public void scheduleJob(Class<? extends Job> jobClass, String jobName, String groupName, int studentId) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(jobName, groupName)
                    .storeDurably(true)
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "Trigger" + studentId, groupName)
                    .startAt(Date.from(LocalDateTime.now().plusSeconds(30)
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
