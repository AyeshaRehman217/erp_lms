package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveQuizAttemptEntity;

import java.util.UUID;

@Repository
public interface SlaveQuizAttemptRepository extends ReactiveCrudRepository<SlaveQuizAttemptEntity, Long> {
    Flux<SlaveQuizAttemptEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Flux<SlaveQuizAttemptEntity> findAllByDeletedAtIsNullAndStatus(Pageable pageable, Boolean status);

    Mono<SlaveQuizAttemptEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<Long> countByDeletedAtIsNullAndStatus(Boolean status);

}
