package student.model;


public enum QuartzJobModel {

CALCULATE_STUDENT_MARKS(Identity.CALCULATE_STUDENT_MARKS);

    QuartzJobModel(String jobName) {
        this.jobName=jobName;
    }

    private final String jobName;

    public String getName() {
        return jobName;
    }

    public static class Identity{
        public static final String CALCULATE_STUDENT_MARKS = "calculate-student-marks";
    }

}
