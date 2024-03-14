package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveQuizCategoryEntity;

import java.util.UUID;

@Repository
public interface SlaveQuizCategoryRepository extends ReactiveCrudRepository<SlaveQuizCategoryEntity, Long> {
    Flux<SlaveQuizCategoryEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveQuizCategoryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<SlaveQuizCategoryEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<SlaveQuizCategoryEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveQuizCategoryEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2);

}
