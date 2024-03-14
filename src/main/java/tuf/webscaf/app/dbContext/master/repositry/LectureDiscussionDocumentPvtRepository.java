package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionDocumentPvtEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface LectureDiscussionDocumentPvtRepository extends ReactiveCrudRepository<LectureDiscussionDocumentPvtEntity, Long> {
    Mono<LectureDiscussionDocumentPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<LectureDiscussionDocumentPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<LectureDiscussionDocumentPvtEntity> findFirstByDocumentUUIDAndDeletedAtIsNull(UUID documentUUID);

    Flux<LectureDiscussionDocumentPvtEntity> findAllByLectureDiscussionUUIDAndDocumentUUIDInAndDeletedAtIsNull(UUID lectureDiscussionUUID, List<UUID> documentUUID);

    Flux<LectureDiscussionDocumentPvtEntity> findAllByLectureDiscussionUUIDAndDeletedAtIsNull(UUID lectureDiscussionUUID);

    Mono<LectureDiscussionDocumentPvtEntity> findFirstByLectureDiscussionUUIDAndDocumentUUIDAndDeletedAtIsNull(UUID lectureDiscussionUUID, UUID documentUUID);

    Mono<LectureDiscussionDocumentPvtEntity> findFirstByLectureDiscussionUUIDAndDeletedAtIsNull(UUID lectureDiscussionUUID);

}
