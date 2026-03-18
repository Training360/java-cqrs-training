package training.cqrstraining.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import training.cqrstraining.infrastructure.event.stream.EmployeesEnrollmentsUpdatedStreamEvent;
import training.cqrstraining.infrastructure.event.stream.EventType;

import java.util.List;
import java.util.function.Consumer;

@Configuration
public class EnrollmentEventsStreamConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventsStreamConsumer.class);

    private final EnrollmentCountProjectionUpdater projectionUpdater;

    public EnrollmentEventsStreamConsumer(EnrollmentCountProjectionUpdater projectionUpdater) {
        this.projectionUpdater = projectionUpdater;
    }

    @Bean
    public Consumer<EmployeesEnrollmentsUpdatedStreamEvent> enrollmentEventsConsumer() {
        return event -> {
            if (event.eventType() ==  EventType.ENROLLED) {
                projectionUpdater
                    .onEmployeesEnrolled(event.courseId(), event.employeeIds().size());}
            else if (event.eventType() ==  EventType.CANCELLED) { projectionUpdater.onEmployeesCancelled(event.courseId(), event.employeeIds().size()); }
            else {
                log.warn("Skipping unsupported event type: {}", event.getClass().getSimpleName());
            }
        };
    }



}

