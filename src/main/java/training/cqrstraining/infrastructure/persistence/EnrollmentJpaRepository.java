package training.cqrstraining.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import training.cqrstraining.application.dto.CourseEnrollmentCountDto;

import java.util.List;
import java.util.Optional;

public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentJpaEntity, Long> {

    Optional<EnrollmentJpaEntity> findByCourseId(Long courseId);

    @Query("SELECT new training.cqrstraining.application.dto.CourseEnrollmentCountDto(e.courseId, COUNT(em)) " +
            "FROM EnrollmentJpaEntity e JOIN e.employeeIds em GROUP BY e.courseId")
    List<CourseEnrollmentCountDto> countEnrollmentsByCourse();
}
