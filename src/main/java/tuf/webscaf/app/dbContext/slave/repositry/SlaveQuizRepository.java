package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveQuizCategoryEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveQuizEntity;

import java.util.UUID;

@Repository
public interface SlaveQuizRepository extends ReactiveCrudRepository<SlaveQuizEntity, Long> {
    Flux<SlaveQuizEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveQuizEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<SlaveQuizEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveQuizEntity> findFirstByDocumentUUIDAndDeletedAtIsNull(UUID documentUUID);

    Flux<SlaveQuizEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveQuizEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2);

}
