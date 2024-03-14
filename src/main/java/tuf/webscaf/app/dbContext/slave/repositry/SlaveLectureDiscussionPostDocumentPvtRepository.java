package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionPostPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveLectureDiscussionPostDocumentPvtRepository extends ReactiveCrudRepository<SlaveLectureDiscussionPostPvtEntity, Long> {
    Mono<SlaveLectureDiscussionPostPvtEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveLectureDiscussionPostPvtEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    //This Query Prints All the Document UUIDs that are mapped for Lecture Discussion Post
    @Query("SELECT string_agg(document_uuid::text, ',') " +
            "as documentUUID FROM lecture_discussion_post_document_pvt " +
            "WHERE lecture_discussion_post_document_pvt.deleted_at IS NULL " +
            "AND lecture_discussion_post_document_pvt.lecture_discussion_post_uuid = :lectureDiscussionPostUUID")
    Mono<String> getAllMappedDocumentUUIDAgainstLectureDiscussionPost(UUID lectureDiscussionPostUUID);
}
