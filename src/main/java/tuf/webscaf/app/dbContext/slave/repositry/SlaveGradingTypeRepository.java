//package tuf.webscaf.app.dbContext.slave.repositry;
//
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveGradingTypeEntity;
//
//import java.util.UUID;
//
//@Repository
//public interface SlaveGradingTypeRepository extends ReactiveCrudRepository<SlaveGradingTypeEntity, Long> {
//    Flux<SlaveGradingTypeEntity> findAllByDeletedAtIsNull(Pageable pageable);
//
//    Mono<SlaveGradingTypeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
//
//    Mono<Long> countByDeletedAtIsNull();
//
//    Mono<SlaveGradingTypeEntity> findByIdAndDeletedAtIsNull(Long id);
//
//    Flux<SlaveGradingTypeEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String description);
//
//    Flux<SlaveGradingTypeEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String description, Boolean status2);
//
//    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(String name, String description);
//
//    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String description, Boolean status2);
//
//}
