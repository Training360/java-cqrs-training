package training.cqrstraining.domain.exception;

public class EnrollmentNotFoundException extends RuntimeException {

    public EnrollmentNotFoundException(Long courseId, Long employeeId) {
        super("Enrollment not found for employee " + employeeId + " in course " + courseId + ".");
    }
}
