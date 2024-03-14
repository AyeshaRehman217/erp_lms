package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentAttemptEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAssignmentAttemptRepository;

import java.util.UUID;

@Repository
public interface SlaveAssignmentAttemptRepository extends ReactiveCrudRepository<SlaveAssignmentAttemptEntity, Long>, SlaveCustomAssignmentAttemptRepository {
    Mono<SlaveAssignmentAttemptEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<Long> countByDeletedAtIsNullAndStatus(Boolean status);

    /**
     * Count All Records With and Without Status Filter
     **/
    @Query("select count(*)  \n" +
            "from lms.assignment_attempts \n" +
            " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid " +
            " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
            " where assignment_attempts.deleted_at IS NULL\n" +
            " and assignments.deleted_at IS NULL\n" +
            " and assignment_attempt_documents.deleted_at IS NULL\n" +
            " AND assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countIndexRecordsWithoutStatusFilter(String name);

    @Query("select count(*) \n" +
            "from lms.assignment_attempts \n" +
            " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid " +
            " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
            " where assignment_attempts.deleted_at IS NULL \n" +
            " and assignment_attempts.status =:status " +
            " and assignments.deleted_at IS NULL\n" +
            " and assignment_attempt_documents.deleted_at IS NULL\n" +
            " AND assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countIndexRecordsWithStatusFilter(Boolean status, String name);

    /**
     * Count All Records With and Without Status Filter && Attempted By Filter
     **/
    @Query("select count(*)  \n" +
            "from lms.assignment_attempts \n" +
            " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid " +
            " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
            " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
            " where assignment_attempts.deleted_at IS NULL\n" +
            " AND assignments.deleted_at IS NULL\n" +
            " AND assignment_attempt_marks.deleted_at IS NULL\n" +
            " AND assignment_attempt_documents.deleted_at IS NULL\n" +
            " and assignment_attempts.attempted_by =:attemptedBy \n" +
            " AND assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countIndexRecordsAgainstAttemptedByWithoutStatusFilter(UUID attemptedBy, String name);

    /**
     * Count All Records With and Without Status Filter && Attempted By Filter
     **/
    @Query("select count(*)  \n" +
            "from lms.assignment_attempts \n" +
            " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid " +
            " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
            " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
            " where assignment_attempts.deleted_at IS NULL\n" +
            " AND assignments.deleted_at IS NULL\n" +
            " AND assignment_attempt_marks.deleted_at IS NULL\n" +
            " AND assignment_attempt_documents.deleted_at IS NULL\n" +
            " and assignment_attempts.status =:status \n" +
            " and assignment_attempts.attempted_by =:attemptedBy \n" +
            " AND assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countIndexRecordsAgainstAttemptedByWithStatusFilter(UUID attemptedBy, Boolean status, String name);

    /**
     * Count All Records With and Without Status Filter && Assignment Filter
     **/
    @Query("select count(*)  \n" +
            "from lms.assignment_attempts \n" +
            " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid " +
            " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
            " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
            " where assignment_attempts.deleted_at IS NULL\n" +
            " AND assignments.deleted_at IS NULL\n" +
            " AND assignment_attempt_marks.deleted_at IS NULL\n" +
            " AND assignment_attempt_documents.deleted_at IS NULL\n" +
            " and assignments.uuid =:assignmentUUID \n" +
            " AND assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countIndexRecordsAgainstAssignmentWithoutStatusFilter(UUID assignmentUUID, String name);

    /**
     * Count All Records With and Without Status Filter && Attempted By Filter
     **/
    @Query("select count(*)  \n" +
            "from lms.assignment_attempts \n" +
            " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid " +
            " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
            " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
            " where assignment_attempts.deleted_at IS NULL\n" +
            " AND assignments.deleted_at IS NULL\n" +
            " AND assignment_attempt_marks.deleted_at IS NULL\n" +
            " AND assignment_attempt_documents.deleted_at IS NULL\n" +
            " and assignment_attempts.status =:status \n" +
            " and assignments.uuid =:assignmentUUID \n" +
            " AND assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countIndexRecordsAgainstAssignmentWithStatusFilter(UUID assignmentUUID, Boolean status, String name);


    /**
     * Count All Records With and Without Status Filter && Attempted By Filter
     **/
    @Query("select count(*)  \n" +
            "from lms.assignment_attempts \n" +
            " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid " +
            " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
            " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
            " where assignment_attempts.deleted_at IS NULL\n" +
            " AND assignments.deleted_at IS NULL\n" +
            " AND assignment_attempt_marks.deleted_at IS NULL\n" +
            " AND assignment_attempt_documents.deleted_at IS NULL\n" +
            " and assignment_attempts.status =:status \n" +
            " and assignment_attempts.attempted_by =:attemptedBy \n" +
            " and assignments.uuid =:assignmentUUID \n" +
            " AND assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countIndexRecordsAgainstAttemptedByAndAssignmentWithStatusFilter(UUID attemptedBy, UUID assignmentUUID, Boolean status, String name);

    @Query("select count(*)  \n" +
            "from lms.assignment_attempts \n" +
            " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid " +
            " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
            " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
            " where assignment_attempts.deleted_at IS NULL\n" +
            " AND assignments.deleted_at IS NULL\n" +
            " AND assignment_attempt_marks.deleted_at IS NULL\n" +
            " AND assignment_attempt_documents.deleted_at IS NULL\n" +
            " and assignment_attempts.attempted_by =:attemptedBy \n" +
            " and assignments.uuid =:assignmentUUID \n" +
            " AND assignments.name ILIKE concat('%',:name,'%')")
    Mono<Long> countIndexRecordsAgainstAttemptedByAndAssignmentWithOutStatusFilter(UUID attemptedBy, UUID assignmentUUID, String name);


    /**
     * Count Mapped Assignment Attempts Against Student UUID fetch students that are mapped against Assignments and that are enrolled in that course subject and registered against course subject
     **/
    @Query("select count(*) from\n" +
            "(select assignments.name as assignmentName,students.uuid as stdUUID,assignments.uuid as assignmentUUID\n" +
            "from lms.assignments\n" +
            "join course_subject on assignments.course_subject_uuid=course_subject.uuid\n" +
            "join courses on course_subject.course_uuid=courses.uuid\n" +
            "join campus_course on campus_course.course_uuid=course_subject.course_uuid\n" +
            "join registrations on registrations.campus_course_uuid=campus_course.uuid\n" +
            "join students on registrations.student_uuid=students.uuid\n" +
            "join subject_offered on subject_offered.course_subject_uuid=course_subject.uuid\n" +
            "join enrollments on enrollments.subject_offered_uuid=subject_offered.uuid\n" +
            "join academic_sessions on academic_sessions.uuid=enrollments.academic_session_uuid\n" +
            "join teacher_subjects on teacher_subjects.course_subject_uuid=course_subject.uuid\n" +
            "where assignments.deleted_at is null\n" +
            "and course_subject.deleted_at is null\n" +
            "and campus_course.deleted_at is null\n" +
            "and courses.deleted_at is null\n" +
            "and registrations.deleted_at is null\n" +
            "and students.deleted_at is null\n" +
            "and enrollments.deleted_at is null\n" +
            "and academic_sessions.deleted_at is null\n" +
            "and teacher_subjects.deleted_at is null\n" +
            "and subject_offered.deleted_at is null\n" +
            "and enrollments.student_uuid =:studentUUID \n" +
            ") as stdAssignments\n" +
            "join lms.assignment_attempts on stdAssignments.assignmentUUID=assignment_attempts.assignment_uuid \n" +
            "and stdAssignments.stdUUID=assignment_attempts.attempted_by\n" +
            "where assignment_attempts.attempted_by =:studentUUID \n" +
            "and assignment_attempts.deleted_at is null \n" +
            " AND (stdAssignments.assignmentName ILIKE concat('%',:assignmentName,'%') or assignment_attempts.comment ILIKE concat('%',:comment,'%') )")
    Mono<Long> countMappedRecordsAgainstAttemptedByWithoutStatusFilter(UUID studentUUID, String assignmentName, String comment);

    @Query("select count(*) from\n" +
            "(select assignments.name as assignmentName,students.uuid as stdUUID,assignments.uuid as assignmentUUID\n" +
            " from lms.assignments\n" +
            " join course_subject on assignments.course_subject_uuid=course_subject.uuid\n" +
            " join courses on course_subject.course_uuid=courses.uuid\n" +
            " join campus_course on campus_course.course_uuid=course_subject.course_uuid\n" +
            " join registrations on registrations.campus_course_uuid=campus_course.uuid\n" +
            " join students on registrations.student_uuid=students.uuid\n" +
            " join subject_offered on subject_offered.course_subject_uuid=course_subject.uuid\n" +
            " join enrollments on enrollments.subject_offered_uuid=subject_offered.uuid\n" +
            " join academic_sessions on academic_sessions.uuid=enrollments.academic_session_uuid\n" +
            " join teacher_subjects on teacher_subjects.course_subject_uuid=course_subject.uuid\n" +
            " where assignments.deleted_at is null\n" +
            " and course_subject.deleted_at is null\n" +
            " and campus_course.deleted_at is null\n" +
            " and courses.deleted_at is null\n" +
            " and registrations.deleted_at is null\n" +
            " and students.deleted_at is null\n" +
            " and enrollments.deleted_at is null\n" +
            " and academic_sessions.deleted_at is null\n" +
            " and teacher_subjects.deleted_at is null\n" +
            " and subject_offered.deleted_at is null\n" +
            " and enrollments.student_uuid =:studentUUID \n" +
            ") as stdAssignments\n" +
            " join lms.assignment_attempts on stdAssignments.assignmentUUID=assignment_attempts.assignment_uuid\n" +
            " and stdAssignments.stdUUID=assignment_attempts.attempted_by\n" +
            " where assignment_attempts.attempted_by =:studentUUID \n" +
            " and assignment_attempts.deleted_at is null \n" +
            " and assignment_attempts.status =:status " +
            " AND (stdAssignments.assignmentName ILIKE concat('%',:assignmentName,'%') " +
            " or assignment_attempts.comment ILIKE concat('%',:comment,'%') )")
    Mono<Long> countMappedRecordsAgainstAttemptedByWithStatusFilter(UUID studentUUID, Boolean status, String assignmentName, String comment);
}
