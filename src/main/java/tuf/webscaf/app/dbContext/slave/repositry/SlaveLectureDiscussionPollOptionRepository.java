package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionPollOptionEntity;

import java.util.UUID;

@Repository
public interface SlaveLectureDiscussionPollOptionRepository extends ReactiveCrudRepository<SlaveLectureDiscussionPollOptionEntity, Long> {
    Flux<SlaveLectureDiscussionPollOptionEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveLectureDiscussionPollOptionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<SlaveLectureDiscussionPollOptionEntity> findByIdAndDeletedAtIsNull(Long id);

    Flux<SlaveLectureDiscussionPollOptionEntity> findAllByTitleContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name);

    Flux<SlaveLectureDiscussionPollOptionEntity> findAllByTitleContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status);

    Mono<Long> countByTitleContainingIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<Long> countByTitleContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status);

}
