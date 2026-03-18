package training.cqrstraining.infrastructure.event;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import training.cqrstraining.domain.event.DomainEvent;
import training.cqrstraining.domain.event.EmployeesCancelledEvent;
import training.cqrstraining.domain.event.EmployeesEnrolledEvent;
import training.cqrstraining.domain.model.EmployeeId;
import training.cqrstraining.infrastructure.event.stream.EmployeesEnrollmentsUpdatedStreamEvent;
import training.cqrstraining.infrastructure.event.stream.EventType;
import training.cqrstraining.infrastructure.persistence.OutboxEventJpaEntity;
import training.cqrstraining.infrastructure.persistence.OutboxEventJpaRepository;
import training.cqrstraining.infrastructure.persistence.OutboxEventStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxEventStore {

    private static final String AGGREGATE_TYPE = "Enrollment";

    private final OutboxEventJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventStore(OutboxEventJpaRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void store(List<DomainEvent> domainEvents) {
        if (domainEvents == null || domainEvents.isEmpty()) {
            return;
        }

        Instant now = Instant.now();
        List<OutboxEventJpaEntity> entities = new ArrayList<>(domainEvents.size());

        for (DomainEvent domainEvent : domainEvents) {
            EmployeesEnrollmentsUpdatedStreamEvent streamEvent = toStreamEvent(domainEvent);
            String payload = toPayload(streamEvent);
            entities.add(new OutboxEventJpaEntity(
                    UUID.randomUUID(),
                    AGGREGATE_TYPE,
                    streamEvent.courseId(),
                    domainEvent.getClass().getSimpleName(),
                    payload,
                    OutboxEventStatus.NEW,
                    0,
                    now,
                    now
            ));
        }

        outboxRepository.saveAll(entities);
    }

    private EmployeesEnrollmentsUpdatedStreamEvent toStreamEvent(DomainEvent event) {
        if (event instanceof EmployeesEnrolledEvent enrolledEvent) {
            return new EmployeesEnrollmentsUpdatedStreamEvent(
                    EventType.ENROLLED,
                    enrolledEvent.courseId().value(),
                    toEmployeeIds(enrolledEvent.employeeIds())
            );
        }

        if (event instanceof EmployeesCancelledEvent cancelledEvent) {
            return new EmployeesEnrollmentsUpdatedStreamEvent(
                    EventType.CANCELLED,
                    cancelledEvent.courseId().value(),
                    toEmployeeIds(cancelledEvent.employeeIds())
            );
        }

        throw new IllegalArgumentException("Unsupported domain event type: " + event.getClass().getName());
    }

    private String toPayload(EmployeesEnrollmentsUpdatedStreamEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to serialize outbox event payload.", ex);
        }
    }

    private List<Long> toEmployeeIds(java.util.Set<EmployeeId> employeeIds) {
        return employeeIds.stream()
                .map(EmployeeId::value)
                .sorted()
                .toList();
    }
}


