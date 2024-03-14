//package tuf.webscaf.app.dbContext.master.repositry;
//
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.master.entity.QuestionEntity;
//import tuf.webscaf.app.dbContext.master.entity.QuizEntity;
//
//import java.util.UUID;
//
//@Repository
//public interface QuestionRepository extends ReactiveCrudRepository<QuestionEntity, Long> {
//    Mono<QuestionEntity> findByIdAndDeletedAtIsNull(Long id);
//
//    Mono<QuestionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
//
//    Mono<QuestionEntity> findFirstByQuestionCategoryUUIDAndDeletedAtIsNull(UUID questionCategoryUUID);
//
//    Mono<QuestionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);
//
//    Mono<QuestionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
//}
