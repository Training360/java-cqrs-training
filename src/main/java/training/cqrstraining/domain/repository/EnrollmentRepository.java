package training.cqrstraining.domain.repository;

import training.cqrstraining.domain.model.CourseId;
import training.cqrstraining.domain.model.Enrollment;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository {

    Optional<Enrollment> findByCourseId(CourseId courseId);

    Enrollment save(Enrollment enrollment);

    List<Enrollment> findAll();
}
