package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.QuizCategoryEntity;
import tuf.webscaf.app.dbContext.master.entity.QuizEntity;

import java.util.UUID;

@Repository
public interface QuizRepository extends ReactiveCrudRepository<QuizEntity, Long> {
    Mono<QuizEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<QuizEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<QuizEntity> findFirstByQuizCategoryUUIDAndDeletedAtIsNull(UUID quizCategoryUUID);

    Mono<QuizEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<QuizEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
