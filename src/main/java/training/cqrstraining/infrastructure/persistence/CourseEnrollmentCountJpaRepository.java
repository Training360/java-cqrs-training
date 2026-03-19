package training.cqrstraining.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import training.cqrstraining.application.dto.CourseEnrollmentCountDto;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentCountJpaRepository extends JpaRepository<CourseEnrollmentCountJpaEntity, Long> {

	@Query("SELECT new training.cqrstraining.application.dto.CourseEnrollmentCountDto(c.courseId, c.totalEnrollmentCount, c.enrollmentVersion) " +
			"FROM CourseEnrollmentCountJpaEntity c ORDER BY c.courseId")
	List<CourseEnrollmentCountDto> findCourseEnrollmentCounts();

	@Query("SELECT new training.cqrstraining.application.dto.CourseEnrollmentCountDto(c.courseId, c.totalEnrollmentCount, c.enrollmentVersion) " +
			"FROM CourseEnrollmentCountJpaEntity c WHERE c.courseId = :courseId")
	Optional<CourseEnrollmentCountDto> findCourseEnrollmentCountByCourseId(Long courseId);
}


