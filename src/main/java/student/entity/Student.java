package student.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Entity
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private int studentId;
    @Column(name = "name")
    private String name;
    @Column(name = "age")
    private int age;
    @Column(name = "total_marks")
    private int totalMarks;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "subject_id")
    private Subject subject;

}


