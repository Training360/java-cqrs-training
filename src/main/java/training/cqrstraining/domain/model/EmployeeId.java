package training.cqrstraining.domain.model;

public record EmployeeId(Long value) {

    public EmployeeId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Employee ID must be a positive number.");
        }
    }
}
