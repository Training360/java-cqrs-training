package training.cqrstraining.domain.exception;

public class DuplicateEnrollmentException extends RuntimeException {

    public DuplicateEnrollmentException(Long courseId, Long employeeId) {
        super("Employee " + employeeId + " is already enrolled in course " + courseId + ".");
    }
}
