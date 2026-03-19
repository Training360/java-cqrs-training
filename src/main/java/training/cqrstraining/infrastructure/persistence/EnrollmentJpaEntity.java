package training.cqrstraining.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "enrollments",
        uniqueConstraints = @UniqueConstraint(name = "uk_course", columnNames = {"course_id"})
)
public class EnrollmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @ElementCollection
    @CollectionTable(
            name = "enrollment_members",
            joinColumns = @JoinColumn(name = "enrollment_id")
    )
    @Column(name = "employee_id", nullable = false)
    private Set<Long> employeeIds = new LinkedHashSet<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected EnrollmentJpaEntity() {
    }

    public EnrollmentJpaEntity(Long courseId, Set<Long> employeeIds) {
        this.courseId = courseId;
        this.employeeIds = new LinkedHashSet<>(employeeIds);
    }

    public Long getId() {
        return id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Set<Long> getEmployeeIds() {
        return employeeIds;
    }

    public Long getVersion() {
        return version;
    }

    public void setEmployeeIds(Set<Long> employeeIds) {
        this.employeeIds = new LinkedHashSet<>(employeeIds);
    }
}
