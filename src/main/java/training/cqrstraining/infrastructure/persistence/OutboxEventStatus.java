package training.cqrstraining.infrastructure.persistence;

public enum OutboxEventStatus {
    NEW,
    PROCESSING,
    FAILED,
    PUBLISHED,
    DEAD
}

