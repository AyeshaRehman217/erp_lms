package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPollOptionEntity;

import java.util.UUID;

@Repository
public interface LectureDiscussionPollOptionRepository extends ReactiveCrudRepository<LectureDiscussionPollOptionEntity, Long> {
    Mono<LectureDiscussionPollOptionEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<LectureDiscussionPollOptionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<LectureDiscussionPollOptionEntity> findFirstByTitleIgnoreCaseAndDeletedAtIsNull(String title);

    Mono<LectureDiscussionPollOptionEntity> findFirstByTitleIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String title, UUID uuid);
}
