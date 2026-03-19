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

    public boolean onEmployeesEnrolled(Long courseId, Long totalEnrollmentCount, Long version) {
        return updateCount(courseId, totalEnrollmentCount, version);
    }

    public boolean onEmployeesCancelled(Long courseId, Long totalEnrollmentCount, Long version) {
        return updateCount(courseId, totalEnrollmentCount, version);
    }

    private boolean updateCount(Long courseId, Long totalEnrollmentCount, Long version) {
        long safeVersion = version == null ? 0L : version;

        CourseEnrollmentCountJpaEntity readModel = countRepository.findById(courseId)
                .orElseGet(() -> new CourseEnrollmentCountJpaEntity(courseId, 0L, 0L));

        if (safeVersion <= readModel.getEnrollmentVersion()) {
            return false;
        }

        readModel.setTotalEnrollmentCount(totalEnrollmentCount);
        readModel.setEnrollmentVersion(safeVersion);
        countRepository.save(readModel);
        return true;
    }
}



