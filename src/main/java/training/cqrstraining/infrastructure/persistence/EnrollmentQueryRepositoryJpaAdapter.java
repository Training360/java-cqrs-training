package training.cqrstraining.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import training.cqrstraining.application.dto.CourseEnrollmentCountDto;
import training.cqrstraining.application.query.EnrollmentQueryRepository;

import java.util.List;

@Repository
public class EnrollmentQueryRepositoryJpaAdapter implements EnrollmentQueryRepository {

    private final EnrollmentJpaRepository enrollmentJpaRepository;

    public EnrollmentQueryRepositoryJpaAdapter(EnrollmentJpaRepository enrollmentJpaRepository) {
        this.enrollmentJpaRepository = enrollmentJpaRepository;
    }

    @Override
    public List<CourseEnrollmentCountDto> countEnrollmentsByCourse() {
        return enrollmentJpaRepository.countEnrollmentsByCourse();
    }
}

