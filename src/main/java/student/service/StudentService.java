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
import student.scheduler.StudentSyncJob;
import student.scheduler.StudentReportJob;

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

    @Inject
    io.agroal.api.AgroalDataSource dataSource;

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
     * Creates a recurring trigger for periodic student data sync
     */
    public void scheduleRecurringStudentSync(int intervalInMinutes) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(StudentSyncJob.class)
                    .withIdentity("student-sync-job", "sync-jobs")
                    .build();

            // Create a recurring trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("student-sync-trigger", "sync-triggers")
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMinutes(intervalInMinutes)
                            .repeatForever())
                    .build();

            // Schedule the job
            scheduler.scheduleJob(jobDetail, trigger);
            logger.infov("Scheduled recurring student sync every {0} minutes", intervalInMinutes);

        } catch (SchedulerException e) {
            logger.error("Failed to schedule recurring student sync", e);
        }
    }

    /**
     * Creates a cron-based trigger for daily reports
     */
    public void scheduleDailyStudentReport(String cronExpression) {
        try {
            JobDetail jobDetail = JobBuilder.newJob(StudentReportJob.class)
                    .withIdentity("daily-report-job", "report-jobs")
                    .build();

            // Create a cron trigger
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("daily-report-trigger", "report-triggers")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
            logger.infov("Scheduled daily student report with cron: {0}", cronExpression);

        } catch (SchedulerException e) {
            logger.error("Failed to schedule daily report", e);
        }
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
     * Check what Quartz tables exist in the database
     */
    public Response checkQuartzTables() {
        try (java.sql.Connection connection = dataSource.getConnection()) {
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            java.sql.ResultSet tables = metaData.getTables(null, null, "%qrtz%", new String[]{"TABLE"});
            
            List<String> tableNames = new java.util.ArrayList<>();
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                tableNames.add(tableName);
            }
            
            if (tableNames.isEmpty()) {
                // Try with uppercase
                tables = metaData.getTables(null, null, "%QRTZ%", new String[]{"TABLE"});
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    tableNames.add(tableName);
                }
            }
            
            if (tableNames.isEmpty()) {
                // Try getting all tables and filter
                tables = metaData.getTables(null, null, null, new String[]{"TABLE"});
                List<String> allTables = new java.util.ArrayList<>();
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    allTables.add(tableName);
                    if (tableName.toLowerCase().contains("qrtz") || tableName.toLowerCase().contains("quartz")) {
                        tableNames.add(tableName);
                    }
                }
                
                if (tableNames.isEmpty()) {
                    return Response.ok().entity(java.util.Map.of(
                        "quartzTables", tableNames,
                        "message", "No Quartz tables found",
                        "allTables", allTables.size() > 50 ? allTables.subList(0, 50) : allTables
                    )).build();
                }
            }
            
            return Response.ok().entity(java.util.Map.of(
                "quartzTables", tableNames,
                "message", "Found " + tableNames.size() + " Quartz tables"
            )).build();
            
        } catch (Exception e) {
            logger.error("Failed to check Quartz tables", e);
            return Response.serverError().entity("Failed to check tables: " + e.getMessage()).build();
        }
    }
}
