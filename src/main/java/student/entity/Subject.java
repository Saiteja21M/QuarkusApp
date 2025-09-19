package student.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Subject extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subject_id")
    private int subjectId;
    @Column(name = "telugu")
    private int telugu;
    @Column(name = "hindi")
    private int hindi;
    @Column(name = "english")
    private int english;
    @Column(name = "maths")
    private int maths;
}