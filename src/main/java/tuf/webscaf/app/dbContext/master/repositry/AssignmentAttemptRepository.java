package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentAttemptRepository extends ReactiveCrudRepository<AssignmentAttemptEntity, Long> {
    Mono<AssignmentAttemptEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<AssignmentAttemptEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<AssignmentAttemptEntity> findFirstByAssignmentUUIDAndDeletedAtIsNull(UUID assignmentUUID);

    Flux<AssignmentAttemptEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuids);

    Mono<AssignmentAttemptEntity> findFirstByAssignmentUUIDAndAttemptedByAndDeletedAtIsNull(UUID assignmentUUID, UUID attemptedByUUID);

    Mono<AssignmentAttemptEntity> findFirstByAssignmentUUIDAndAttemptedByAndDeletedAtIsNullAndUuidIsNot(UUID assignmentUUID, UUID attemptedByUUID, UUID assignmentAttemptUUID);
}
