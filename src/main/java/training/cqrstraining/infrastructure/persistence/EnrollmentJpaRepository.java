package training.cqrstraining.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentJpaRepository extends JpaRepository<EnrollmentJpaEntity, Long> {

    Optional<EnrollmentJpaEntity> findByCourseId(Long courseId);
}
