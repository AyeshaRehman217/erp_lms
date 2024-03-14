package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptEntity;
import tuf.webscaf.app.dbContext.master.entity.QuizAttemptEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends ReactiveCrudRepository<QuizAttemptEntity, Long> {
    Mono<QuizAttemptEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<QuizAttemptEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<QuizAttemptEntity> findFirstByQuizUUIDAndDeletedAtIsNull(UUID quizUUID);

    Flux<QuizAttemptEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuids);
}
