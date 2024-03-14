package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionEntity;

import java.util.UUID;

@Repository
public interface LectureDiscussionRepository extends ReactiveCrudRepository<LectureDiscussionEntity, Long> {
    Mono<LectureDiscussionEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<LectureDiscussionEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<LectureDiscussionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<LectureDiscussionEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
