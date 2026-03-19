package training.cqrstraining.infrastructure.persistence;

import org.springframework.stereotype.Repository;
import training.cqrstraining.domain.model.CourseId;
import training.cqrstraining.domain.model.EmployeeId;
import training.cqrstraining.domain.model.Enrollment;
import training.cqrstraining.domain.repository.EnrollmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class EnrollmentRepositoryJpaAdapter implements EnrollmentRepository {

    private final EnrollmentJpaRepository jpaRepository;

    public EnrollmentRepositoryJpaAdapter(EnrollmentJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Enrollment> findByCourseId(CourseId courseId) {
        return jpaRepository.findByCourseId(courseId.value())
                .map(this::toDomain);
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        EnrollmentJpaEntity entity = jpaRepository.findByCourseId(enrollment.courseId().value())
                .orElseGet(() -> new EnrollmentJpaEntity(enrollment.courseId().value(), Set.of()));

        Set<Long> employeeIds = enrollment.employeeIds()
                .stream()
                .map(EmployeeId::value)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        entity.setEmployeeIds(employeeIds);

        EnrollmentJpaEntity saved = jpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    @Override
    public List<Enrollment> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Enrollment toDomain(EnrollmentJpaEntity entity) {
        Set<EmployeeId> employees = entity.getEmployeeIds()
                .stream()
                .map(EmployeeId::new)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        Long version = entity.getVersion() == null ? 0L : entity.getVersion();
        return new Enrollment(new CourseId(entity.getCourseId()), employees, version);
    }
}
