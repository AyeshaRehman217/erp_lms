//package tuf.webscaf.app.dbContext.slave.repositry;
//
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveQuestionBankEntity;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveQuestionEntity;
//
//import java.util.UUID;
//
//@Repository
//public interface SlaveQuestionRepository extends ReactiveCrudRepository<SlaveQuestionEntity, Long> {
//    Flux<SlaveQuestionEntity> findAllByDeletedAtIsNull(Pageable pageable);
//
//    Mono<SlaveQuestionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
//
//    Mono<Long> countByDeletedAtIsNull();
//
//    Mono<SlaveQuestionEntity> findByIdAndDeletedAtIsNull(Long id);
//
//    Flux<SlaveQuestionEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);
//
//    Flux<SlaveQuestionEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status2);
//
//    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
//
//    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2);
//
//}
