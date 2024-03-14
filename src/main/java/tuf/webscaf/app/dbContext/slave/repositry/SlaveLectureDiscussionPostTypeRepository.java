package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionPostTypeEntity;

import java.util.UUID;

@Repository
public interface SlaveLectureDiscussionPostTypeRepository extends ReactiveCrudRepository<SlaveLectureDiscussionPostTypeEntity, Long> {
    Flux<SlaveLectureDiscussionPostTypeEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveLectureDiscussionPostTypeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<SlaveLectureDiscussionPostTypeEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<SlaveLectureDiscussionPostTypeEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveLectureDiscussionPostTypeEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2);

}
