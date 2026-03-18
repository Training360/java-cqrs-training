package training.cqrstraining.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import training.cqrstraining.application.dto.CourseEnrollmentCountDto;
import training.cqrstraining.application.query.EnrollmentQueryRepository;

import java.util.List;

@Repository
public class EnrollmentQueryRepositoryJpaAdapter implements EnrollmentQueryRepository {

    private final CourseEnrollmentCountJpaRepository countJpaRepository;

    public EnrollmentQueryRepositoryJpaAdapter(CourseEnrollmentCountJpaRepository countJpaRepository) {
        this.countJpaRepository = countJpaRepository;
    }

    @Override
    public List<CourseEnrollmentCountDto> countEnrollmentsByCourse() {
        return countJpaRepository.findCourseEnrollmentCounts();
    }
}

