package training.cqrstraining.domain.model;

public record CourseId(Long value) {

    public CourseId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Course ID must be a positive number.");
        }
    }
}
