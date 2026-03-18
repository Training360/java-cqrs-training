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

    @Column(name = "total_enrollment_count", nullable = false)
    private Long totalEnrollmentCount;

    protected CourseEnrollmentCountJpaEntity() {
    }

    public CourseEnrollmentCountJpaEntity(Long courseId, Long totalEnrollmentCount) {
        this.courseId = courseId;
        this.totalEnrollmentCount = totalEnrollmentCount;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setTotalEnrollmentCount(Long totalEnrollmentCount) {
        this.totalEnrollmentCount = totalEnrollmentCount;
    }
}

