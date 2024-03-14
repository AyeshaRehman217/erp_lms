package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPostEntity;

import java.util.UUID;

@Repository
public interface LectureDiscussionPostRepository extends ReactiveCrudRepository<LectureDiscussionPostEntity, Long> {
    Mono<LectureDiscussionPostEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<LectureDiscussionPostEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<LectureDiscussionPostEntity> findFirstBySubjectIgnoreCaseAndDeletedAtIsNull(String title);

    Mono<LectureDiscussionPostEntity> findFirstBySubjectIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String title, UUID uuid);
}
