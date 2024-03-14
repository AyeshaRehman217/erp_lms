//package tuf.webscaf.app.dbContext.master.repositry;
//
//import org.springframework.data.repository.reactive.ReactiveCrudRepository;
//import org.springframework.stereotype.Repository;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.master.entity.GradeEntity;
//import tuf.webscaf.app.dbContext.master.entity.QuizEntity;
//
//import java.util.UUID;
//
//@Repository
//public interface GradeRepository extends ReactiveCrudRepository<GradeEntity, Long> {
//    Mono<GradeEntity> findByIdAndDeletedAtIsNull(Long id);
//
//    Mono<GradeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
//
//    Mono<GradeEntity> findFirstByGradingTypeUUIDAndDeletedAtIsNull(UUID gradingTypeUUID);
//
//    Mono<GradeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);
//
//    Mono<GradeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
//}
