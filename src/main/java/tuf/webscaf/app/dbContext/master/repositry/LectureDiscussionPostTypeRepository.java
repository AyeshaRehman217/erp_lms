package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPostTypeEntity;

import java.util.UUID;

@Repository
public interface LectureDiscussionPostTypeRepository extends ReactiveCrudRepository<LectureDiscussionPostTypeEntity, Long> {
    Mono<LectureDiscussionPostTypeEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<LectureDiscussionPostTypeEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<LectureDiscussionPostTypeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<LectureDiscussionPostTypeEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);
}
