package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionEntity;

import java.util.UUID;

@Repository
public interface SlaveLectureDiscussionRepository extends ReactiveCrudRepository<SlaveLectureDiscussionEntity, Long> {
    Flux<SlaveLectureDiscussionEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveLectureDiscussionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

//    Mono<SlaveLectureDiscussionEntity> findFirstByAttachmentUUIDAndDeletedAtIsNull(UUID attachmentUUID);

    Mono<SlaveLectureDiscussionEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<SlaveLectureDiscussionEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);

    Flux<SlaveLectureDiscussionEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status2);

    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);

    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2);

}
