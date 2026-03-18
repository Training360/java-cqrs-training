package training.cqrstraining.infrastructure.event;

import org.springframework.stereotype.Component;
import training.cqrstraining.infrastructure.persistence.CourseEnrollmentCountJpaEntity;
import training.cqrstraining.infrastructure.persistence.CourseEnrollmentCountJpaRepository;

@Component
public class EnrollmentCountProjectionUpdater {

    private final CourseEnrollmentCountJpaRepository countRepository;

    public EnrollmentCountProjectionUpdater(CourseEnrollmentCountJpaRepository countRepository) {
        this.countRepository = countRepository;
    }

    public void onEmployeesEnrolled(Long courseId, int enrolledEmployeesCount) {
        applyDelta(courseId, enrolledEmployeesCount);
    }

    public void onEmployeesCancelled(Long courseId, int cancelledEmployeesCount) {
        applyDelta(courseId, -cancelledEmployeesCount);
    }

    private void applyDelta(Long courseId, int delta) {
        CourseEnrollmentCountJpaEntity readModel = countRepository.findById(courseId)
                .orElseGet(() -> new CourseEnrollmentCountJpaEntity(courseId, 0L));

        long updatedCount = Math.max(0L, readModel.getEnrollmentCount() + delta);
        if (updatedCount == 0L) {
            countRepository.deleteById(courseId);
            return;
        }

        readModel.setEnrollmentCount(updatedCount);
        countRepository.save(readModel);
    }
}



