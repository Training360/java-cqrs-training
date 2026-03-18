package training.cqrstraining.infrastructure.event.stream;

import java.util.List;

public record EmployeesEnrollmentsUpdatedStreamEvent(EventType eventType, Long courseId, List<Long> employeeIds, Long totalEnrollmentCount) implements EnrollmentStreamEvent {
}

