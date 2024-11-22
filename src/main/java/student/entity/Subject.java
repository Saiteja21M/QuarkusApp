package student.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Subject extends PanacheEntityBase {

    @Id
    private int subjectId;
    private int telugu;
    private int hindi;
    private int english;
    private int maths;

    public int getMaths() {
        return maths;
    }

    public void setMaths(int maths) {
        this.maths = maths;
    }

    public int getTelugu() {
        return telugu;
    }

    public void setTelugu(int telugu) {
        this.telugu = telugu;
    }

    public int getEnglish() {
        return english;
    }

    public void setEnglish(int english) {
        this.english = english;
    }

    public int getHindi() {
        return hindi;
    }

    public void setHindi(int hindi) {
        this.hindi = hindi;
    }

    public void setSubjectId(int subjectId) { this.subjectId = subjectId;}

    public int getSubjectId(){return this.subjectId;}

    @Override
    public String toString() {
        return "Subject{" +
                "telugu=" + telugu +
                ", hindi=" + hindi +
                ", english=" + english +
                ", maths=" + maths +
                '}';
    }
}