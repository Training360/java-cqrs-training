package training.cqrstraining.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import training.cqrstraining.infrastructure.event.stream.EmployeesEnrollmentsUpdatedStreamEvent;
import training.cqrstraining.infrastructure.event.stream.EventType;

import java.time.Instant;

@Component
public class EnrollmentEventsMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventsMessageHandler.class);
    private static final String HEADER_EVENT_ID = "eventId";

    private final EnrollmentCountProjectionUpdater projectionUpdater;

    public EnrollmentEventsMessageHandler(EnrollmentCountProjectionUpdater projectionUpdater) {
        this.projectionUpdater = projectionUpdater;
    }

    @Transactional
    public void handle(Message<EmployeesEnrollmentsUpdatedStreamEvent> message) {
        EmployeesEnrollmentsUpdatedStreamEvent event = message.getPayload();

        if (event.eventType() == EventType.ENROLLED) {
            projectionUpdater.onEmployeesEnrolled(event.courseId(), event.totalEnrollmentCount());
        } else if (event.eventType() == EventType.CANCELLED) {
            projectionUpdater.onEmployeesCancelled(event.courseId(), event.totalEnrollmentCount());
        } else {
            log.warn("Skipping unsupported event type: {}", event.eventType());
            return;
        }
    }
}

