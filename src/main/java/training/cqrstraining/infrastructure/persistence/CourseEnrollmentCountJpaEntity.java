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

    @Column(name = "enrollment_version", nullable = false)
    private Long enrollmentVersion;

    protected CourseEnrollmentCountJpaEntity() {
    }

    public CourseEnrollmentCountJpaEntity(Long courseId, Long totalEnrollmentCount, Long enrollmentVersion) {
        this.courseId = courseId;
        this.totalEnrollmentCount = totalEnrollmentCount;
        this.enrollmentVersion = enrollmentVersion;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setTotalEnrollmentCount(Long totalEnrollmentCount) {
        this.totalEnrollmentCount = totalEnrollmentCount;
    }

    public Long getEnrollmentVersion() {
        return enrollmentVersion;
    }

    public void setEnrollmentVersion(Long enrollmentVersion) {
        this.enrollmentVersion = enrollmentVersion;
    }
}

