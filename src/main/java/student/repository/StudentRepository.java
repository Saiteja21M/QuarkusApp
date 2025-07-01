package student.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import student.entity.Student;

@ApplicationScoped
public class StudentRepository implements PanacheRepository<Student> {
}
