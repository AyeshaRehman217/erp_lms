package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentDocumentEntity;

import java.util.UUID;

@Repository
public interface SlaveAssignmentDocumentRepository extends ReactiveCrudRepository<SlaveAssignmentDocumentEntity, Long> {
    Mono<SlaveAssignmentDocumentEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<SlaveAssignmentDocumentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Flux<SlaveAssignmentDocumentEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveAssignmentDocumentEntity> findFirstByDocumentUUIDAndDeletedAtIsNull(UUID documentUUID);

    Flux<SlaveAssignmentDocumentEntity> findAllByAssignmentUUIDAndDeletedAtIsNull(UUID assignmentUUID);

    //Getting All Documents That exist in Drive Module
    @Query("SELECT \n" +
            "CASE\n" +
            "WHEN string_agg(assignment_documents.document_uuid::text, ',') is not null\n" +
            "THEN string_agg(assignment_documents.document_uuid::text, ',') \n" +
            "ELSE ''\n" +
            "END as documentIds \n" +
            "FROM lms.assignment_documents \n" +
            "WHERE assignment_documents.deleted_at IS NULL \n" +
            "AND assignment_documents.Assignment_uuid =:assignmentUUID")
    Mono<String> getAllDocumentListAgainstAssignment(UUID assignmentUUID);
}
