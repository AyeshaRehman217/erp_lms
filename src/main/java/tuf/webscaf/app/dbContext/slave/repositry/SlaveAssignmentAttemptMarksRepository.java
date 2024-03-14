package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentAttemptMarksEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAssignmentAttemptMarksRepository;

import java.util.UUID;

@Repository
public interface SlaveAssignmentAttemptMarksRepository extends ReactiveCrudRepository<SlaveAssignmentAttemptMarksEntity, Long>, SlaveCustomAssignmentAttemptMarksRepository {
    Flux<SlaveAssignmentAttemptMarksEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Flux<SlaveAssignmentAttemptMarksEntity> findAllByDeletedAtIsNullAndStatus(Pageable pageable, Boolean status);

    Mono<SlaveAssignmentAttemptMarksEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    /**
     * Count All Records with StudentUUID && AssignmentAttempt
     **/
    @Query("select count(*) " +
            " from lms.assignment_attempt_marks \n" +
            " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
            " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
            " where assignment_attempt_marks.deleted_at is null \n" +
            " and assignment_attempts.deleted_at is null  \n" +
            " and assignments.deleted_at is null\n" +
            " and assignment_attempts.attempted_by =:studentUUID " +
            " and assignment_attempt_marks.assignment_attempt_uuid =:assignmentAttemptUUID " +
            " and assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMarksAgainstStudentAndAttemptWithoutStatusFilter(UUID studentUUID, UUID assignmentAttemptUUID, String name);

    /**
     * Count All Records with StudentUUID && AssignmentAttempt and Status Filter
     **/
    @Query("select count(*) " +
            " from lms.assignment_attempt_marks \n" +
            " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
            " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
            " where assignment_attempt_marks.status =:status " +
            " and assignment_attempt_marks.deleted_at is null \n" +
            " and assignment_attempts.deleted_at is null  \n" +
            " and assignments.deleted_at is null \n" +
            " and assignment_attempts.attempted_by =:studentUUID " +
            " and assignment_attempt_marks.assignment_attempt_uuid =:assignmentAttemptUUID " +
            " and assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMarksAgainstStudentAndAttemptWithStatusFilter(Boolean status, UUID studentUUID, UUID assignmentAttemptUUID, String name);


    /**
     * Count All Records with StudentUUID and Status Filter
     **/
    @Query("select count(*) " +
            " from lms.assignment_attempt_marks \n" +
            " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
            " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
            " where assignment_attempt_marks.status =:status " +
            " and assignment_attempt_marks.deleted_at is null \n" +
            " and assignment_attempts.deleted_at is null  \n" +
            " and assignments.deleted_at is null \n" +
            " and assignment_attempts.attempted_by =:studentUUID " +
            " and assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMarksAgainstStudentWithStatusFilter(Boolean status, UUID studentUUID, String name);

    /**
     * Count All Records with StudentUUID and Status Filter
     **/
    @Query("select count(*) " +
            " from lms.assignment_attempt_marks \n" +
            " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
            " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
            " where assignment_attempt_marks.deleted_at is null \n" +
            " and assignment_attempts.deleted_at is null  \n" +
            " and assignments.deleted_at is null \n" +
            " and assignment_attempts.attempted_by =:studentUUID " +
            " and assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMarksAgainstStudentWithoutStatusFilter(UUID studentUUID, String name);


    /**
     * Count All Records with assignmentAttemptUUID and Status Filter
     **/
    @Query("select count(*) " +
            " from lms.assignment_attempt_marks \n" +
            " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
            " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
            " where assignment_attempt_marks.status =:status " +
            " and assignment_attempt_marks.deleted_at is null \n" +
            " and assignment_attempts.deleted_at is null  \n" +
            " and assignments.deleted_at is null \n" +
            " and assignment_attempt_marks.assignment_attempt_uuid =:assignmentAttemptUUID " +
            " and assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMarksAgainstAssignmentAttemptWithStatusFilter(Boolean status, UUID assignmentAttemptUUID, String name);

    /**
     * Count All Records with assignmentAttemptUUID and Status Filter
     **/
    @Query("select count(*) " +
            " from lms.assignment_attempt_marks \n" +
            " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
            " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
            " where assignment_attempt_marks.deleted_at is null \n" +
            " and assignment_attempts.deleted_at is null  \n" +
            " and assignments.deleted_at is null \n" +
            " and assignment_attempt_marks.assignment_attempt_uuid =:assignmentAttemptUUID " +
            " and assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countMarksAgainstAssignmentAttemptWithoutStatusFilter(UUID assignmentAttemptUUID, String name);

    /**
     * Count All Records
     **/
    @Query("select count(*) " +
            " from lms.assignment_attempt_marks \n" +
            " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
            " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
            " where assignment_attempt_marks.deleted_at is null \n" +
            " and assignment_attempts.deleted_at is null  \n" +
            " and assignments.deleted_at is null \n" +
            " and assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countAllMarks(String name);

    /**
     * Count All Records
     **/
    @Query("select count(*) " +
            " from lms.assignment_attempt_marks \n" +
            " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
            " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
            " where assignment_attempt_marks.status =:status \n" +
            " and assignment_attempts.deleted_at is null \n" +
            " and assignment_attempts.deleted_at is null \n" +
            " and assignments.deleted_at is null \n" +
            " and assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countAllMarksWithStatusFilter(Boolean status, String name);
}
