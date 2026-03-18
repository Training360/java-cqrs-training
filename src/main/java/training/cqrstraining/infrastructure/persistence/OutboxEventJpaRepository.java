package training.cqrstraining.infrastructure.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {

    @Query("""
            select e
            from OutboxEventJpaEntity e
            where e.status in :statuses
              and e.nextAttemptAt <= :now
            order by e.createdAt asc
            """)
    List<OutboxEventJpaEntity> findPublishableBatch(@Param("statuses") Collection<OutboxEventStatus> statuses,
                                                     @Param("now") Instant now,
                                                     Pageable pageable);

    long countByStatus(OutboxEventStatus status);
}

