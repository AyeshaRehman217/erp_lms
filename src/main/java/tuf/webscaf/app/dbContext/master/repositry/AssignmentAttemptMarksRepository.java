package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptEntity;
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptMarksEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentAttemptMarksRepository extends ReactiveCrudRepository<AssignmentAttemptMarksEntity, Long> {
    Mono<AssignmentAttemptMarksEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<AssignmentAttemptMarksEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<AssignmentAttemptMarksEntity> findFirstByAssignmentAttemptUUIDAndDeletedAtIsNull(UUID assignmentAttemptUUID);

    Mono<AssignmentAttemptMarksEntity> findFirstByAssignmentAttemptUUIDAndDeletedAtIsNullAndUuidIsNot(UUID assignmentAttemptUUID, UUID uuid);

    Flux<AssignmentAttemptMarksEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuids);
}
