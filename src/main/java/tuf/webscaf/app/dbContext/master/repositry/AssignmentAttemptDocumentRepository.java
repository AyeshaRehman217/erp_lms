package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptDocumentEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentAttemptDocumentRepository extends ReactiveCrudRepository<AssignmentAttemptDocumentEntity, Long> {

    Mono<AssignmentAttemptDocumentEntity> findFirstByAssignmentAttemptUUIDAndDeletedAtIsNull(UUID assignmentAttemptUUID);

    Flux<AssignmentAttemptDocumentEntity> findByAssignmentAttemptUUIDAndDeletedAtIsNull(UUID assignmentAttemptUUID);

    Flux<AssignmentAttemptDocumentEntity> findAllByAssignmentAttemptUUIDAndDeletedAtIsNull(UUID assignmentAttemptUUID);

    Mono<AssignmentAttemptDocumentEntity> findFirstByAssignmentAttemptUUIDAndDocumentUUIDAndDeletedAtIsNull(UUID assignmentAttemptUUID, UUID documentUUID);

    Mono<AssignmentAttemptDocumentEntity> findFirstByDocumentUUIDAndDeletedAtIsNull(UUID documentUUID);

    Flux<AssignmentAttemptDocumentEntity> findAllByAssignmentAttemptUUIDAndDocumentUUIDInAndDeletedAtIsNull(UUID assignmentAttemptUUID, List<UUID> documentUUID);

    Flux<AssignmentAttemptDocumentEntity> findAllByAssignmentAttemptUUIDAndDocumentUUIDNotInAndDeletedAtIsNull(UUID assignmentAttemptUUID, List<UUID> documentUUID);

}
