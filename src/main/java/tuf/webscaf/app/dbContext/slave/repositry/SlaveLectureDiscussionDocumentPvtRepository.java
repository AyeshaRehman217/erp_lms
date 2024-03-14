package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionDocumentPvtEntity;

import java.util.UUID;

@Repository
public interface SlaveLectureDiscussionDocumentPvtRepository extends ReactiveCrudRepository<SlaveLectureDiscussionDocumentPvtEntity, Long> {

    //This Query Prints All the Document UUIDs that are mapped for LectureDiscussion
    @Query("SELECT string_agg(document_uuid::text, ',') " +
            "as documentUUID FROM lecture_discussion_document_pvt " +
            "WHERE lecture_discussion_document_pvt.deleted_at IS NULL " +
            "AND lecture_discussion_document_pvt.lecture_discussion_uuid = :lecture_discussionUUID")
    Mono<String> getAllMappedDocumentUUIDAgainstLectureDiscussion(UUID lecture_discussionUUID);
}
