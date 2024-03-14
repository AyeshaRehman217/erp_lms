//package tuf.webscaf.app.dbContext.master.repositry;
//
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.master.entity.QuestionBankEntity;
//import tuf.webscaf.app.dbContext.master.entity.QuizEntity;
//
//import java.util.UUID;
//
//@Repository
//public interface QuestionBankRepository extends ReactiveCrudRepository<QuestionBankEntity, Long> {
//    Mono<QuestionBankEntity> findByIdAndDeletedAtIsNull(Long id);
//
//    Mono<QuestionBankEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
//
//    Mono<QuestionBankEntity> findFirstByQuestionUUIDAndDeletedAtIsNull(UUID questionUUID);
//
//    Mono<QuestionBankEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);
//
//    Mono<QuestionBankEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
//}
