package training.cqrstraining.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "course_enrollment_counts")
public class CourseEnrollmentCountJpaEntity {

    @Id
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "enrollment_count", nullable = false)
    private Long enrollmentCount;

    protected CourseEnrollmentCountJpaEntity() {
    }

    public CourseEnrollmentCountJpaEntity(Long courseId, Long enrollmentCount) {
        this.courseId = courseId;
        this.enrollmentCount = enrollmentCount;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getEnrollmentCount() {
        return enrollmentCount;
    }

    public void setEnrollmentCount(Long enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }
}

