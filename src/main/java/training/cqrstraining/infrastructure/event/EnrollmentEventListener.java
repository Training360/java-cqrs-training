package training.cqrstraining.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import training.cqrstraining.domain.event.DomainEvent;
import training.cqrstraining.domain.event.EmployeesCancelledEvent;
import training.cqrstraining.domain.event.EmployeesEnrolledEvent;

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

    @EventListener
    public void onEmployeesEnrolled(EmployeesEnrolledEvent event) {
        send(event, event.courseId().value());
    }

    @EventListener
    public void onEmployeesCancelled(EmployeesCancelledEvent event) {
        send(event, event.courseId().value());
    }

    private void send(DomainEvent event, Object partitionKey) {
        log.info("Publishing {} for partitionKey {}", event.getClass().getSimpleName(), partitionKey);
        Message<DomainEvent> message = MessageBuilder.withPayload(event)
                .setHeader(HEADER_EVENT_TYPE, event.getClass().getSimpleName())
                .setHeader(HEADER_PARTITION_KEY, partitionKey)
                .build();
        streamBridge.send(EVENTS_BINDING, message);
    }
}
