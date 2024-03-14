package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPollOptionVoteEntity;

import java.util.UUID;

@Repository
public interface ForumPollOptionVoteRepository extends ReactiveCrudRepository<LectureDiscussionPollOptionVoteEntity, Long> {
    Mono<LectureDiscussionPollOptionVoteEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<LectureDiscussionPollOptionVoteEntity> findByUuidAndDeletedAtIsNull(UUID uuid);
}
