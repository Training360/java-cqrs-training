package training.cqrstraining.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import training.cqrstraining.domain.event.EmployeesCancelledEvent;
import training.cqrstraining.domain.event.EmployeesEnrolledEvent;
import training.cqrstraining.domain.model.EmployeeId;
import training.cqrstraining.infrastructure.event.stream.EmployeesEnrollmentsUpdatedStreamEvent;
import training.cqrstraining.infrastructure.event.stream.EnrollmentStreamEvent;
import training.cqrstraining.infrastructure.event.stream.EventType;

import java.util.List;

@Component
public class EnrollmentEventListener {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventListener.class);

    private static final String EVENTS_BINDING = "events-out-0";
    private static final String HEADER_EVENT_TYPE = "eventType";
    private static final String HEADER_PARTITION_KEY = "partitionKey";

    private final StreamBridge streamBridge;

    public EnrollmentEventListener(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmployeesEnrolled(EmployeesEnrolledEvent event) {
        send(new EmployeesEnrollmentsUpdatedStreamEvent(EventType.ENROLLED, event.courseId().value(), toEmployeeIds(event.employeeIds()), event.totalEnrollmentCount()));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmployeesCancelled(EmployeesCancelledEvent event) {
        send(new EmployeesEnrollmentsUpdatedStreamEvent(EventType.CANCELLED, event.courseId().value(), toEmployeeIds(event.employeeIds()), event.totalEnrollmentCount()));
    }

    private void send(EnrollmentStreamEvent event) {
        log.info("Publishing {} for partitionKey {}", event.getClass().getSimpleName(), event.courseId());
        Message<EnrollmentStreamEvent> message = MessageBuilder.withPayload(event)
                .setHeader(HEADER_EVENT_TYPE, event.getClass().getSimpleName())
                .setHeader(HEADER_PARTITION_KEY, event.courseId())
                .build();
        streamBridge.send(EVENTS_BINDING, message);
    }

    private List<Long> toEmployeeIds(java.util.Set<EmployeeId> employeeIds) {
        return employeeIds.stream()
                .map(EmployeeId::value)
                .sorted()
                .toList();
    }
}
