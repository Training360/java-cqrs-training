package training.cqrstraining.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import training.cqrstraining.infrastructure.event.stream.EmployeesEnrollmentsUpdatedStreamEvent;
import training.cqrstraining.infrastructure.event.stream.EventType;

@Component
public class EnrollmentEventsMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventsMessageHandler.class);

    private final EnrollmentCountProjectionUpdater projectionUpdater;
    private final EnrollmentChangeSseService enrollmentChangeSseService;

    public EnrollmentEventsMessageHandler(EnrollmentCountProjectionUpdater projectionUpdater,
                                          EnrollmentChangeSseService enrollmentChangeSseService) {
        this.projectionUpdater = projectionUpdater;
        this.enrollmentChangeSseService = enrollmentChangeSseService;
    }

    @Transactional
    public void handle(Message<EmployeesEnrollmentsUpdatedStreamEvent> message) {
        EmployeesEnrollmentsUpdatedStreamEvent event = message.getPayload();
        boolean changed;

        if (event.eventType() == EventType.ENROLLED) {
            changed = projectionUpdater.onEmployeesEnrolled(event.courseId(), event.totalEnrollmentCount(), event.version());
        } else if (event.eventType() == EventType.CANCELLED) {
            changed = projectionUpdater.onEmployeesCancelled(event.courseId(), event.totalEnrollmentCount(), event.version());
        } else {
            log.warn("Skipping unsupported event type: {}", event.eventType());
            changed = false;
        }

        if (changed) {
            enrollmentChangeSseService.publish(event.courseId(), event.version());
        }
    }
}

