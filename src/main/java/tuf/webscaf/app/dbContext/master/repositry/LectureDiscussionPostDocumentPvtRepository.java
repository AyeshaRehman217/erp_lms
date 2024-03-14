package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPostDocumentPvtEntity;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPostEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface LectureDiscussionPostDocumentPvtRepository extends ReactiveCrudRepository<LectureDiscussionPostDocumentPvtEntity, Long> {
    Mono<LectureDiscussionPostDocumentPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<LectureDiscussionPostDocumentPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<LectureDiscussionPostDocumentPvtEntity> findAllByLectureDiscussionPostUUIDAndDocumentUUIDInAndDeletedAtIsNull(UUID lectureDiscussionPostUUID, List<UUID> documentUUID);

    Mono<LectureDiscussionPostDocumentPvtEntity> findFirstByLectureDiscussionPostUUIDAndDocumentUUIDAndDeletedAtIsNull(UUID lectureDiscussionPostUUID, UUID documentUUID);
}
