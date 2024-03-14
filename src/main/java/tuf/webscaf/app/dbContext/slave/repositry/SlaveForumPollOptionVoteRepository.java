package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionPollOptionVoteEntity;

import java.util.UUID;

@Repository
public interface SlaveForumPollOptionVoteRepository extends ReactiveCrudRepository<SlaveLectureDiscussionPollOptionVoteEntity, Long> {
    Flux<SlaveLectureDiscussionPollOptionVoteEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Flux<SlaveLectureDiscussionPollOptionVoteEntity> findAllByDeletedAtIsNullAndStatus(Pageable pageable, Boolean status);

    Mono<SlaveLectureDiscussionPollOptionVoteEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<Long> countByDeletedAtIsNullAndStatus(Boolean status);

    Mono<SlaveLectureDiscussionPollOptionVoteEntity> findByIdAndDeletedAtIsNull(Long id);

}
