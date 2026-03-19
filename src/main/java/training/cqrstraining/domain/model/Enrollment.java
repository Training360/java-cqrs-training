package training.cqrstraining.domain.model;

import training.cqrstraining.domain.event.DomainEvent;
import training.cqrstraining.domain.event.EmployeesCancelledEvent;
import training.cqrstraining.domain.event.EmployeesEnrolledEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Enrollment {

    private final CourseId courseId;
    private final Set<EmployeeId> employeeIds;
    private final Long version;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Enrollment(CourseId courseId) {
        this(courseId, new LinkedHashSet<>(), 0L);
    }

    public Enrollment(CourseId courseId, Set<EmployeeId> employeeIds) {
        this(courseId, employeeIds, 0L);
    }

    public Enrollment(CourseId courseId, Set<EmployeeId> employeeIds, Long version) {
        if (courseId == null) {
            throw new IllegalArgumentException("Course ID is required.");
        }
        if (employeeIds == null) {
            throw new IllegalArgumentException("Employee IDs are required.");
        }
        if (version == null || version < 0L) {
            throw new IllegalArgumentException("Version must be non-negative.");
        }

        this.courseId = courseId;
        this.employeeIds = new LinkedHashSet<>(employeeIds);
        this.version = version;
    }

    public CourseId courseId() {
        return courseId;
    }

    public Set<EmployeeId> employeeIds() {
        return Collections.unmodifiableSet(employeeIds);
    }

    public Long version() {
        return version;
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
        domainEvents.add(new EmployeesEnrolledEvent(courseId, Set.copyOf(newEmployees), (long) employeeIds.size()));
    }

    public void cancelAll(Set<EmployeeId> employeesToRemove) {
        if (employeesToRemove == null) {
            throw new IllegalArgumentException("Employee IDs are required.");
        }
        employeesToRemove.forEach(this::cancel);
        domainEvents.add(new EmployeesCancelledEvent(courseId, Set.copyOf(employeesToRemove), (long) employeeIds.size()));
    }

    private void enroll(EmployeeId employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        employeeIds.add(employeeId);
    }

    private void cancel(EmployeeId employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        employeeIds.remove(employeeId);
    }
}
