package training.cqrstraining.domain.model;

import training.cqrstraining.domain.event.DomainEvent;
import training.cqrstraining.domain.event.EmployeesCancelledEvent;
import training.cqrstraining.domain.event.EmployeesEnrolledEvent;
import training.cqrstraining.domain.exception.DuplicateEnrollmentException;
import training.cqrstraining.domain.exception.EnrollmentNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Enrollment {

    private final CourseId courseId;
    private final Set<EmployeeId> employeeIds;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Enrollment(CourseId courseId) {
        this(courseId, new LinkedHashSet<>());
    }

    public Enrollment(CourseId courseId, Set<EmployeeId> employeeIds) {
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID is required.");
        }
        if (employeeIds == null) {
            throw new IllegalArgumentException("Employee IDs are required.");
        }

        this.courseId = courseId;
        this.employeeIds = new LinkedHashSet<>(employeeIds);
    }

    public CourseId courseId() {
        return courseId;
    }

    public Set<EmployeeId> employeeIds() {
        return Collections.unmodifiableSet(employeeIds);
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }

    public void enrollAll(Set<EmployeeId> newEmployees) {
        if (newEmployees == null) {
            throw new IllegalArgumentException("Employee IDs are required.");
        }
        newEmployees.forEach(this::enroll);
        domainEvents.add(new EmployeesEnrolledEvent(courseId, Set.copyOf(newEmployees)));
    }

    public void cancelAll(Set<EmployeeId> employeesToRemove) {
        if (employeesToRemove == null) {
            throw new IllegalArgumentException("Employee IDs are required.");
        }
        employeesToRemove.forEach(this::cancel);
        domainEvents.add(new EmployeesCancelledEvent(courseId, Set.copyOf(employeesToRemove)));
    }

    private void enroll(EmployeeId employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        if (!employeeIds.add(employeeId)) {
            throw new DuplicateEnrollmentException(courseId.value(), employeeId.value());
        }
    }

    private void cancel(EmployeeId employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        if (!employeeIds.remove(employeeId)) {
            throw new EnrollmentNotFoundException(courseId.value(), employeeId.value());
        }
    }
}
