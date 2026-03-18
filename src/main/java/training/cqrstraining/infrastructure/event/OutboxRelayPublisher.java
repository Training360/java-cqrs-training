package training.cqrstraining.infrastructure.event;

import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import training.cqrstraining.infrastructure.event.stream.EmployeesEnrollmentsUpdatedStreamEvent;
import training.cqrstraining.infrastructure.persistence.OutboxEventJpaEntity;
import training.cqrstraining.infrastructure.persistence.OutboxEventJpaRepository;
import training.cqrstraining.infrastructure.persistence.OutboxEventStatus;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Component
public class OutboxRelayPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayPublisher.class);

    private static final String EVENTS_BINDING = "events-out-0";
    private static final String HEADER_EVENT_TYPE = "eventType";
    private static final String HEADER_PARTITION_KEY = "partitionKey";
    private static final String HEADER_EVENT_ID = "eventId";

    private static final Set<OutboxEventStatus> RELAYABLE_STATUSES = Set.of(OutboxEventStatus.NEW, OutboxEventStatus.FAILED);

    private final OutboxEventJpaRepository outboxRepository;
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    private final int batchSize;
    private final int maxAttempts;
    private final long initialBackoffMs;
    private final long maxBackoffMs;

    @Autowired
    public OutboxRelayPublisher(OutboxEventJpaRepository outboxRepository,
                                StreamBridge streamBridge,
                                ObjectMapper objectMapper,
                                org.springframework.core.env.Environment environment) {
        this(outboxRepository,
                streamBridge,
                objectMapper,
                Clock.systemUTC(),
                environment.getProperty("outbox.relay.batch-size", Integer.class, 50),
                environment.getProperty("outbox.relay.max-attempts", Integer.class, 8),
                environment.getProperty("outbox.relay.initial-backoff-ms", Long.class, 500L),
                environment.getProperty("outbox.relay.max-backoff-ms", Long.class, 30_000L));
    }

    OutboxRelayPublisher(OutboxEventJpaRepository outboxRepository,
                         StreamBridge streamBridge,
                         ObjectMapper objectMapper,
                         Clock clock,
                         int batchSize,
                         int maxAttempts,
                         long initialBackoffMs,
                         long maxBackoffMs) {
        this.outboxRepository = outboxRepository;
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
        this.initialBackoffMs = initialBackoffMs;
        this.maxBackoffMs = maxBackoffMs;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms:500}")
    public void relay() {
        Instant now = Instant.now(clock);
        List<OutboxEventJpaEntity> batch = outboxRepository.findPublishableBatch(
                RELAYABLE_STATUSES,
                now,
                PageRequest.of(0, batchSize)
        );

        for (OutboxEventJpaEntity outboxEvent : batch) {
            publishSingle(outboxEvent);
        }
    }

    private void publishSingle(OutboxEventJpaEntity outboxEvent) {
        if (outboxEvent.getStatus() == OutboxEventStatus.PUBLISHED || outboxEvent.getStatus() == OutboxEventStatus.DEAD) {
            return;
        }

        outboxEvent.setStatus(OutboxEventStatus.PROCESSING);
        outboxRepository.save(outboxEvent);

        try {
            EmployeesEnrollmentsUpdatedStreamEvent payload = objectMapper.readValue(
                    outboxEvent.getPayload(),
                    EmployeesEnrollmentsUpdatedStreamEvent.class
            );

            Message<EmployeesEnrollmentsUpdatedStreamEvent> message = MessageBuilder.withPayload(payload)
                    .setHeader(HEADER_EVENT_TYPE, payload.getClass().getSimpleName())
                    .setHeader(HEADER_PARTITION_KEY, payload.courseId())
                    .setHeader(HEADER_EVENT_ID, outboxEvent.getEventId().toString())
                    .build();

            boolean published = streamBridge.send(EVENTS_BINDING, message);
            if (!published) {
                throw new IllegalStateException("StreamBridge send returned false.");
            }

            outboxEvent.setStatus(OutboxEventStatus.PUBLISHED);
            outboxEvent.setPublishedAt(Instant.now(clock));
            outboxEvent.setLastError(null);
            outboxRepository.save(outboxEvent);
            log.info("Published outbox event {}", outboxEvent.getEventId());
        } catch (Exception ex) {
            handlePublishFailure(outboxEvent, ex);
        }
    }

    private void handlePublishFailure(OutboxEventJpaEntity outboxEvent, Exception ex) {
        int nextAttempts = outboxEvent.getAttempts() + 1;
        outboxEvent.setAttempts(nextAttempts);
        outboxEvent.setLastError(truncateError(ex));

        if (nextAttempts >= maxAttempts) {
            outboxEvent.setStatus(OutboxEventStatus.DEAD);
            outboxEvent.setNextAttemptAt(Instant.now(clock));
            outboxRepository.save(outboxEvent);
            log.error("Outbox event {} moved to DEAD after {} attempts", outboxEvent.getEventId(), nextAttempts, ex);
            return;
        }

        long delayMs = computeBackoff(nextAttempts);
        outboxEvent.setStatus(OutboxEventStatus.FAILED);
        outboxEvent.setNextAttemptAt(Instant.now(clock).plusMillis(delayMs));
        outboxRepository.save(outboxEvent);
        log.warn("Failed to publish outbox event {}. Retrying in {} ms", outboxEvent.getEventId(), delayMs, ex);
    }

    private long computeBackoff(int attempts) {
        int exponent = Math.min(10, Math.max(0, attempts - 1));
        long nextDelay = initialBackoffMs * (1L << exponent);
        return Math.min(maxBackoffMs, nextDelay);
    }

    private String truncateError(Exception ex) {
        String message;
        if (ex instanceof JacksonException jex) {
            message = "Invalid outbox payload: " + jex.getOriginalMessage();
        } else {
            message = ex.getMessage();
        }

        if (message == null || message.isBlank()) {
            message = ex.getClass().getName();
        }

        return message.length() <= 1024 ? message : message.substring(0, 1024);
    }
}


