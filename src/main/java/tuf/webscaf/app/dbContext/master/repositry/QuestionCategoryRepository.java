//package tuf.webscaf.app.dbContext.master.repositry;
//
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.master.entity.QuestionCategoryEntity;
//
//import java.util.UUID;
//
//@Repository
//public interface QuestionCategoryRepository extends ReactiveCrudRepository<QuestionCategoryEntity, Long> {
//    Mono<QuestionCategoryEntity> findByIdAndDeletedAtIsNull(Long id);
//
//    Mono<QuestionCategoryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
//
//    Mono<QuestionCategoryEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);
//
//    Mono<QuestionCategoryEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
//}
