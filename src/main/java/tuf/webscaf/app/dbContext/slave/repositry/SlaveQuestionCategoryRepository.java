//package tuf.webscaf.app.dbContext.slave.repositry;
//
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveQuestionCategoryEntity;
//
//import java.util.UUID;
//
//@Repository
//public interface SlaveQuestionCategoryRepository extends ReactiveCrudRepository<SlaveQuestionCategoryEntity, Long> {
//    Flux<SlaveQuestionCategoryEntity> findAllByDeletedAtIsNull(Pageable pageable);
//
//    Mono<SlaveQuestionCategoryEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
//
//    Mono<Long> countByDeletedAtIsNull();
//
//    Mono<SlaveQuestionCategoryEntity> findByIdAndDeletedAtIsNull(Long id);
//
//    Flux<SlaveQuestionCategoryEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);
//
//    Flux<SlaveQuestionCategoryEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status2);
//
//    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
//
//    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2);
//
//}
