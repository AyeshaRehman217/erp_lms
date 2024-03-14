//package tuf.webscaf.app.dbContext.slave.repositry;
//
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveGradeEntity;
//
//import java.util.UUID;
//
//@Repository
//public interface SlaveGradeRepository extends ReactiveCrudRepository<SlaveGradeEntity, Long> {
//    Flux<SlaveGradeEntity> findAllByDeletedAtIsNull(Pageable pageable);
//
//    Mono<SlaveGradeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
//
//    Mono<Long> countByDeletedAtIsNull();
//
//    Mono<SlaveGradeEntity> findByIdAndDeletedAtIsNull(Long id);
//
//    Flux<SlaveGradeEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);
//
//    Flux<SlaveGradeEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status2);
//
//    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
//
//    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2);
//
//}
