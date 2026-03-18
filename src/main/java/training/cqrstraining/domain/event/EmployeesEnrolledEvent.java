package training.cqrstraining.domain.event;

import training.cqrstraining.domain.model.CourseId;
import training.cqrstraining.domain.model.EmployeeId;

import java.util.Set;

public record EmployeesEnrolledEvent(CourseId courseId, Set<EmployeeId> employeeIds, Long totalEnrollmentCount) implements DomainEvent {
}

