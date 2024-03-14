//package tuf.webscaf.app.dbContext.master.repositry;
//
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.master.entity.GradingTypeEntity;
//
//import java.util.List;
//import java.util.UUID;
//
//@Repository
//public interface GradingTypeRepository extends ReactiveCrudRepository<GradingTypeEntity, Long> {
//    Mono<GradingTypeEntity> findByIdAndDeletedAtIsNull(Long id);
//
//    Mono<GradingTypeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
//
//    Mono<GradingTypeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);
//
//    Mono<GradingTypeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
//
//    Flux<GradingTypeEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuids);
//}
