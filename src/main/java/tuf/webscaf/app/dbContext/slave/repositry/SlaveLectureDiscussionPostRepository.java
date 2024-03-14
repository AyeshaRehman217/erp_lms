package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionPostEntity;

import java.util.UUID;

@Repository
public interface SlaveLectureDiscussionPostRepository extends ReactiveCrudRepository<SlaveLectureDiscussionPostEntity, Long> {
    Flux<SlaveLectureDiscussionPostEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveLectureDiscussionPostEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<SlaveLectureDiscussionPostEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<SlaveLectureDiscussionPostEntity> findAllBySubjectContainingIgnoreCaseAndDeletedAtIsNullOrMessageContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String subject, String message);

    Flux<SlaveLectureDiscussionPostEntity> findAllBySubjectContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrMessageContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String subject, Boolean status, String message, Boolean status2);

    Mono<Long> countBySubjectContainingIgnoreCaseAndDeletedAtIsNullOrMessageContainingIgnoreCaseAndDeletedAtIsNull(String subject, String message);

    Mono<Long> countBySubjectContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrMessageContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String subject, Boolean status, String message, Boolean status2);

}
