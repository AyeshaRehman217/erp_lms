package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AssignmentDocumentEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentDocumentRepository extends ReactiveCrudRepository<AssignmentDocumentEntity, Long> {

    Mono<AssignmentDocumentEntity> findFirstByAssignmentUUIDAndDeletedAtIsNull(UUID assignmentUUID);

    Flux<AssignmentDocumentEntity> findByAssignmentUUIDAndDeletedAtIsNull(UUID assignmentUUID);

    Flux<AssignmentDocumentEntity> findAllByAssignmentUUIDAndDeletedAtIsNull(UUID assignmentUUID);

    Mono<AssignmentDocumentEntity> findFirstByAssignmentUUIDAndDocumentUUIDAndDeletedAtIsNull(UUID assignmentUUID, UUID documentUUID);

    Mono<AssignmentDocumentEntity> findFirstByDocumentUUIDAndDeletedAtIsNull(UUID documentUUID);

    Flux<AssignmentDocumentEntity> findAllByAssignmentUUIDAndDocumentUUIDInAndDeletedAtIsNull(UUID assignmentUUID, List<UUID> documentUUID);

    Flux<AssignmentDocumentEntity> findAllByAssignmentUUIDAndDocumentUUIDNotInAndDeletedAtIsNull(UUID assignmentUUID, List<UUID> documentUUID);

}
