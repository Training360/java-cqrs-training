package training.cqrstraining.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import training.cqrstraining.infrastructure.event.stream.EmployeesEnrollmentsUpdatedStreamEvent;
import training.cqrstraining.infrastructure.event.stream.EventType;
import training.cqrstraining.infrastructure.persistence.ProcessedEventJpaEntity;
import training.cqrstraining.infrastructure.persistence.ProcessedEventJpaRepository;

import java.time.Instant;

@Component
public class EnrollmentEventsMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventsMessageHandler.class);
    private static final String HEADER_EVENT_ID = "eventId";

    private final EnrollmentCountProjectionUpdater projectionUpdater;
    private final ProcessedEventJpaRepository processedEventRepository;

    public EnrollmentEventsMessageHandler(EnrollmentCountProjectionUpdater projectionUpdater,
                                          ProcessedEventJpaRepository processedEventRepository) {
        this.projectionUpdater = projectionUpdater;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void handle(Message<EmployeesEnrollmentsUpdatedStreamEvent> message) {
        EmployeesEnrollmentsUpdatedStreamEvent event = message.getPayload();
        String eventId = message.getHeaders().get(HEADER_EVENT_ID, String.class);

        if (eventId != null && processedEventRepository.existsById(eventId)) {
            log.info("Skipping duplicate event {}", eventId);
            return;
        }

        if (event.eventType() == EventType.ENROLLED) {
            projectionUpdater.onEmployeesEnrolled(event.courseId(), event.employeeIds().size());
        } else if (event.eventType() == EventType.CANCELLED) {
            projectionUpdater.onEmployeesCancelled(event.courseId(), event.employeeIds().size());
        } else {
            log.warn("Skipping unsupported event type: {}", event.eventType());
            return;
        }

        if (eventId != null) {
            try {
                processedEventRepository.save(new ProcessedEventJpaEntity(eventId, Instant.now()));
            } catch (DataIntegrityViolationException ex) {
                log.info("Duplicate event {} detected during save", eventId);
            }
        }
    }
}

