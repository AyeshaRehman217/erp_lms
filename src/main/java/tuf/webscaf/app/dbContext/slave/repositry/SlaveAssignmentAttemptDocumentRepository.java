package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentAttemptDocumentEntity;

import java.util.UUID;

@Repository
public interface SlaveAssignmentAttemptDocumentRepository extends ReactiveCrudRepository<SlaveAssignmentAttemptDocumentEntity, Long> {
    Mono<SlaveAssignmentAttemptDocumentEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveAssignmentAttemptDocumentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveAssignmentAttemptDocumentEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveAssignmentAttemptDocumentEntity> findFirstByDocumentUUIDAndDeletedAtIsNull(UUID documentId);

    Flux<SlaveAssignmentAttemptDocumentEntity> findAllByAssignmentAttemptUUIDAndDeletedAtIsNull(UUID assignmentAttemptUUID);

    //Getting All Documents That exist in Drive Module
    @Query("SELECT \n" +
            "CASE\n" +
            "WHEN string_agg(assignment_attempt_documents.document_uuid::text, ',') is not null\n" +
            "THEN string_agg(assignment_attempt_documents.document_uuid::text, ',') \n" +
            "ELSE ''\n" +
            "END as documentIds \n" +
            "FROM lms.assignment_attempt_documents \n" +
            "WHERE assignment_attempt_documents.deleted_at IS NULL \n" +
            "AND assignment_attempt_documents.assignment_attempt_uuid =:assignmentAttemptUUID")
    Mono<String> getAllDocumentListAgainstAssignmentAttempt(UUID assignmentAttemptUUID);
}
