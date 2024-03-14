package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.QuizCategoryEntity;

import java.util.UUID;

@Repository
public interface QuizCategoryRepository extends ReactiveCrudRepository<QuizCategoryEntity, Long> {
    Mono<QuizCategoryEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<QuizCategoryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<QuizCategoryEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<QuizCategoryEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
