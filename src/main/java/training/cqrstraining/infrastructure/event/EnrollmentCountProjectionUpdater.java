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

    public void onEmployeesEnrolled(Long courseId, Long totalEnrollmentCount) {
        updateCount(courseId, totalEnrollmentCount);
    }

    public void onEmployeesCancelled(Long courseId, Long totalEnrollmentCount) {
        updateCount(courseId, totalEnrollmentCount);
    }

    private void updateCount(Long courseId, Long totalEnrollmentCount) {
        if (totalEnrollmentCount == 0L) {
            countRepository.deleteById(courseId);
            return;
        }

        CourseEnrollmentCountJpaEntity readModel = countRepository.findById(courseId)
                .orElseGet(() -> new CourseEnrollmentCountJpaEntity(courseId, totalEnrollmentCount));

        readModel.setTotalEnrollmentCount(totalEnrollmentCount);
        countRepository.save(readModel);
    }
}



