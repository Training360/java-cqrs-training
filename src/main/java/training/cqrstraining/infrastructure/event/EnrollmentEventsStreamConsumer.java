package training.cqrstraining.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import training.cqrstraining.infrastructure.event.stream.EmployeesEnrollmentsUpdatedStreamEvent;

import java.util.function.Consumer;

@Configuration
public class EnrollmentEventsStreamConsumer {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentEventsStreamConsumer.class);

    private final EnrollmentEventsMessageHandler messageHandler;

    public EnrollmentEventsStreamConsumer(EnrollmentEventsMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Bean
    public Consumer<Message<EmployeesEnrollmentsUpdatedStreamEvent>> enrollmentEventsConsumer() {
        return message -> {
            log.debug("Received stream event for course {}", message.getPayload().courseId());
            messageHandler.handle(message);
        };
    }
}
