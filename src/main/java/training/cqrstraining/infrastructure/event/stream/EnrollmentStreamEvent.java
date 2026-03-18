package training.cqrstraining.infrastructure.event.stream;

import java.util.List;

public interface EnrollmentStreamEvent {

    Long courseId();

    List<Long> employeeIds();
}

