package tuf.webscaf.app.dbContext.master.repositry;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.AssignmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAssignmentRepository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends ReactiveCrudRepository<AssignmentEntity, Long> {
    Mono<AssignmentEntity> findByIdAndDeletedAtIsNull(Long id);

    Mono<AssignmentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<AssignmentEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNull(String name);

    Mono<AssignmentEntity> findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(String name, UUID uuid);

    Flux<AssignmentEntity> findAllByUuidInAndDeletedAtIsNull(List<UUID> uuids);

//    /**
//     * Check if the Teacher and Subject exists in the Course Subject
//     **/
    @Query("select exists(select teacher_subjects.* from \n" +
            "teacher_subjects \n" +
            "where teacher_subjects.teacher_uuid = :teacherUUID \n" +
            "and teacher_subjects.course_subject_uuid =:courseSubjectUUID \n" +
            "and teacher_subjects.deleted_at is null)")
    Mono<Boolean> checkIfEnteredTeacherAndCourseSubjectExists(UUID teacherUUID, UUID courseSubjectUUID);

//    /**
//     * Check if the Course Subject is Offered against the entered Academic Session
//     **/
    @Query("select exists(select subject_offered.* from \n" +
            "subject_offered \n" +
            "where subject_offered.course_subject_uuid =:courseSubjectUUID\n" +
            "and subject_offered.academic_session_uuid =:academicSessionUUID \n" +
            "and subject_offered.deleted_at is null)")
    Mono<Boolean> checkIfEnteredCourseSubjectExistsInSubjectOffered(UUID academicSessionUUID, UUID courseSubjectUUID);

}
