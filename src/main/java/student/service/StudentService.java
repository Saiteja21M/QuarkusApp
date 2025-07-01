package student.service;

import io.quarkus.cache.CacheInvalidateAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import student.client.StudentClient;
import student.entity.Student;
import student.entity.TvShow;
import student.repository.StudentRepository;
import student.scheduler.StudentMarksJob;

import java.util.List;
import java.util.Set;

@ApplicationScoped
public class StudentService {

    @Inject
    StudentRepository studentRepository;

    @Inject
    @RestClient
    StudentClient studentClient;

    @Inject
    Logger logger;

    @Inject
    Scheduler scheduler;

    @Transactional
    public Response calculateTotalMarks(Student student) {

        student.setTvShow(getStudentFavoriteShow());
        studentRepository.persist(student);
        logger.infov("saved {0} student : ", student);
        invalidateAll();

        // Schedule job after transaction completes
        scheduleStudentMarksCalculationAsync(student.getName(), 10);

        return Response.ok(studentRepository.findById((long) student.getStudentId())).build();
    }

    public Response getStudentDetails() {
        List<Student> studentDetails = studentRepository.listAll();
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
     * Creates a one-time trigger to calculate marks for a specific student
     * This method runs outside of any transaction context
     */
    @Transactional(TxType.NOT_SUPPORTED)
    public void scheduleStudentMarksCalculation(String studentName, int delayInSeconds) {
        try {
            // Create a job detail
            JobDetail jobDetail = JobBuilder.newJob(StudentMarksJob.class)
                    .withIdentity("calculate-marks-" + studentName, "student-jobs")
                    .usingJobData("studentName", studentName)
                    .build();

            // Create a trigger that fires once after the specified delay
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("trigger-marks-" + studentName, "student-triggers")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * * * ?"))
                    .build();

            // Schedule the job
            scheduler.scheduleJob(jobDetail, trigger);
            logger.infov("Scheduled marks calculation for student {0} to run in {1} seconds", studentName, delayInSeconds);

        } catch (SchedulerException e) {
            logger.error("Failed to schedule job for student: " + studentName, e);
        }
    }

    /**
     * Async version that can be called from within a transaction
     * Uses a separate thread to avoid transaction conflicts
     */
    public void scheduleStudentMarksCalculationAsync(String studentName, int delayInSeconds) {
        // Execute in a separate thread to avoid transaction conflicts
        new Thread(() -> {
            try {
                // Small delay to ensure current transaction completes
                Thread.sleep(200);
                scheduleStudentMarksCalculation(studentName, delayInSeconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted while scheduling job for student: " + studentName, e);
            } catch (Exception e) {
                logger.error("Error scheduling job for student: " + studentName, e);
            }
        }).start();
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
    public Response listAllScheduledJobs() {
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
}
