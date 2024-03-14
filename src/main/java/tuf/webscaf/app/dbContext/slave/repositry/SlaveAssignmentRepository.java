package tuf.webscaf.app.dbContext.slave.repositry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAssignmentRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomEnrolledStudentsAssignmentAttemptMarksFacadeRepository;

import java.util.UUID;

@Repository
public interface SlaveAssignmentRepository extends ReactiveCrudRepository<SlaveAssignmentEntity, Long>, SlaveCustomAssignmentRepository, SlaveCustomEnrolledStudentsAssignmentAttemptMarksFacadeRepository {
    Flux<SlaveAssignmentEntity> findAllByDeletedAtIsNull(Pageable pageable);

    Mono<SlaveAssignmentEntity> findByUuidAndDeletedAtIsNull(UUID uuid);

    Mono<Long> countByDeletedAtIsNull();

    Mono<SlaveAssignmentEntity> findByIdAndDeletedAtIsNull(Long id);

    // return assignments
    Flux<SlaveAssignmentEntity> findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndDeletedAtIsNull(Pageable pageable, String name, String instruction);

    // return assignment based on status
    Flux<SlaveAssignmentEntity> findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(Pageable pageable, String name, Boolean status, String instruction, Boolean status2);

    // return assignment based on teacher and status
    Flux<SlaveAssignmentEntity> findAllByNameContainingIgnoreCaseAndTeacherUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndStatusAndDeletedAtIsNull(Pageable pageable, String name, UUID teacherUUID, Boolean status, String instruction, UUID teacherUUID1, Boolean status2);

//    // return assignment based on courseSubject and status
//    Flux<SlaveAssignmentEntity> findAllByNameContainingIgnoreCaseAndCourseSubjectUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndCourseSubjectUUIDAndStatusAndDeletedAtIsNull(Pageable pageable, String name, UUID courseSubjectUUID, Boolean status, String instruction, UUID courseSubjectUUID1, Boolean status2);
//
//    // return assignment based on courseSubject
//    Flux<SlaveAssignmentEntity> findAllByNameContainingIgnoreCaseAndCourseSubjectUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndCourseSubjectUUIDAndDeletedAtIsNull(Pageable pageable, String name, UUID courseSubjectUUID, String instruction, UUID courseSubjectUUID1);
//
//    // return assignment based on teacher, courseSubject and status
//    Flux<SlaveAssignmentEntity> findAllByNameContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNull(Pageable pageable, String name, UUID teacherUUID, UUID CourseSubjectUUID, Boolean status, String instruction, UUID teacherUUID1, UUID CourseSubjectUUID1, Boolean status1);
//
//    // return assignment based on teacher, courseSubject
//    Flux<SlaveAssignmentEntity> findAllByNameContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndDeletedAtIsNull(Pageable pageable, String name, UUID teacherUUID, UUID CourseSubjectUUID, String instruction, UUID teacherUUID1, UUID CourseSubjectUUID1);

    // return assignment based on teacher
    Flux<SlaveAssignmentEntity> findAllByNameContainingIgnoreCaseAndTeacherUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndDeletedAtIsNull(Pageable pageable, String name, UUID teacherUUID, String instruction, UUID teacherUUID1);

    // count assignment based on teacher and status
    Mono<Long> countByNameContainingIgnoreCaseAndTeacherUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndStatusAndDeletedAtIsNull(String name, UUID teacherUUID, Boolean status, String instruction, UUID teacherUUID1, Boolean status2);

//    // count assignment based on courseSubject and status
//    Mono<Long> countByNameContainingIgnoreCaseAndCourseSubjectUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndCourseSubjectUUIDAndStatusAndDeletedAtIsNull(String name, UUID courseSubjectUUID, Boolean status, String instruction, UUID courseSubjectUUID1, Boolean status2);
//
//    // count assignment based on courseSubject
//    Mono<Long> countByNameContainingIgnoreCaseAndCourseSubjectUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndCourseSubjectUUIDAndDeletedAtIsNull(String name, UUID courseSubjectUUID, String instruction, UUID courseSubjectUUID1);

    // count assignment based on teacher, courseSubject and status
    Mono<Long> countByNameContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNull(String name, UUID teacherUUID, UUID CourseSubjectUUID, Boolean status, String instruction, UUID teacherUUID1, UUID CourseSubjectUUID1, Boolean status1);

    // count assignment based on academic-session, courseSubject and status
    Mono<Long> countByNameContainingIgnoreCaseAndAcademicSessionUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndAcademicSessionUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNull(String name, UUID academicSessionUUID, UUID CourseSubjectUUID, Boolean status, String instruction, UUID academicSessionUUID1, UUID CourseSubjectUUID1, Boolean status1);

    // count assignment based on academic-session, courseSubject and status
    Mono<Long> countByNameContainingIgnoreCaseAndAcademicSessionUUIDAndCourseSubjectUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndAcademicSessionUUIDAndCourseSubjectUUIDAndDeletedAtIsNull(String name, UUID academicSessionUUID, UUID CourseSubjectUUID, String instruction, UUID academicSessionUUID1, UUID CourseSubjectUUID1);


    // count assignment based on teacher, courseSubject,academic-session and status
    Mono<Long> countByNameContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndAcademicSessionUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndAcademicSessionUUIDAndStatusAndDeletedAtIsNull(String name, UUID teacherUUID, UUID CourseSubjectUUID, UUID academicSessionUUID, Boolean status, String instruction, UUID teacherUUID1, UUID CourseSubjectUUID1, UUID academicSessionUUID1, Boolean status1);

    // count assignment based on teacher, courseSubject,academic-session and without status
    Mono<Long> countByNameContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndAcademicSessionUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndAcademicSessionUUIDAndDeletedAtIsNull(String name, UUID teacherUUID, UUID CourseSubjectUUID, UUID academicSessionUUID, String instruction, UUID teacherUUID1, UUID CourseSubjectUUID1, UUID academicSessionUUID1);


    // count assignment based on teacher, courseSubject
    Mono<Long> countByNameContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndDeletedAtIsNull(String name, UUID teacherUUID, UUID CourseSubjectUUID, String instruction, UUID teacherUUID1, UUID CourseSubjectUUID1);

    // count assignment based on teacher
    Mono<Long> countByNameContainingIgnoreCaseAndTeacherUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndDeletedAtIsNull(String name, UUID teacherUUID, String instruction, UUID teacherUUID1);

    // count assignments
    Mono<Long> countByNameContainingIgnoreCaseAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndDeletedAtIsNull(String name, String instruction);

    // count assignment based on status
    Mono<Long> countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(String name, Boolean status, String instruction, Boolean status2);


    /**
     * Count Assignments Against Subject With Status Filter
     **/
    @Query("select count(*) \n" +
            "from lms.assignments\n" +
            "join course_subject on assignments.course_subject_uuid=course_subject.uuid\n" +
            "join subjects on subjects.uuid=course_subject.subject_uuid\n" +
            "where assignments.deleted_at is null\n" +
            "and course_subject.deleted_at is null\n" +
            "and assignments.status=:status \n" +
            "and subjects.deleted_at is null\n" +
            "and subjects.uuid=:subjectUUID " +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstSubjectWithStatus(UUID subjectUUID, Boolean status, String name, String instruction);

    /**
     * Count Assignments Against Subject Without Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "join course_subject on assignments.course_subject_uuid=course_subject.uuid\n" +
            "join subjects on subjects.uuid=course_subject.subject_uuid\n" +
            "where assignments.deleted_at is null\n" +
            "and course_subject.deleted_at is null\n" +
            "and subjects.deleted_at is null\n" +
            "and subjects.uuid=:subjectUUID " +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstSubjectWithoutStatus(UUID subjectUUID, String name, String instruction);


    /**
     * Count Assignments Against Course Subject With Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            "and assignments.status =:status \n" +
            "and assignments.course_subject_uuid=:courseSubjectUUID " +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstCourseSubjectWithStatus(UUID courseSubjectUUID, Boolean status, String name, String instruction);

    /**
     * Count Assignments Against Academic Session Without Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            " and assignments.academic_session_uuid=:academicSessionUUID " +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstAcademicSessionWithoutStatus(UUID academicSessionUUID, String name, String instruction);

    /**
     * Count Assignments Against Academic Session Without Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            " and assignments.academic_session_uuid=:academicSessionUUID " +
            "and assignments.status =:status \n" +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstAcademicSessionWithStatus(UUID academicSessionUUID, String name, String instruction, Boolean status);

    /**
     * Count Assignments Against Course Subject Without Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            " and assignments.course_subject_uuid=:courseSubjectUUID " +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstCourseSubjectWithoutStatus(UUID courseSubjectUUID, String name, String instruction);

    /**
     * Count Assignments Against Teacher With Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            "and assignments.status =:status \n" +
            "and assignments.teacher_uuid=:teacherUUID " +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstTeacherWithStatus(UUID teacherUUID, Boolean status, String name, String instruction);

    /**
     * Count Assignments Against Teacher Without Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            " and assignments.teacher_uuid=:teacherUUID " +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstTeacherWithoutStatus(UUID teacherUUID, String name, String instruction);


    /**
     * Count Assignments Against Teacher & Course Subject With Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            "and assignments.status =:status \n" +
            "and assignments.teacher_uuid=:teacherUUID " +
            "and assignments.course_subject_uuid=:courseSubjectUUID " +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstTeacherAndCourseSubjectWithStatus(UUID teacherUUID, UUID courseSubjectUUID, Boolean status, String name, String instruction);


    /**
     * Count Assignments Against Teacher & Course Subject Without Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            "and assignments.teacher_uuid=:teacherUUID " +
            "and assignments.course_subject_uuid=:courseSubjectUUID " +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstTeacherAndCourseSubjectWithoutStatus(UUID teacherUUID, UUID courseSubjectUUID, String name, String instruction);

    /**
     * Count All Records with and without Status Filter
     **/
    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            "and assignments.status =:status \n" +
            " AND (assignments.name ILIKE concat('%',:name,'%') " +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsWithStatus(Boolean status, String name, String instruction);

    @Query("select count(*)\n" +
            "from lms.assignments\n" +
            "where assignments.deleted_at is null\n" +
            " AND (assignments.name ILIKE concat('%',:name,'%') " +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsWithoutStatus(String name, String instruction);

    /**
     * This Query Count All Student Records that are registered and Enrolled Against the same course subject and student that attempted the assignment and not attempted the assignment
     **/
    @Query("SELECT count(*) \n" +
            "FROM registrations\n" +
            "JOIN enrollments ON enrollments.student_uuid = registrations.student_uuid\n" +
            "JOIN students ON enrollments.student_uuid = students.uuid\n" +
            "JOIN std_profiles ON std_profiles.student_uuid = students.uuid\n" +
            "JOIN campus_course ON registrations.campus_course_uuid = campus_course.uuid\n" +
            "JOIN courses ON campus_course.course_uuid = courses.uuid\n" +
            "JOIN subject_offered\n" +
            "ON (enrollments.subject_offered_uuid = subject_offered.uuid\n" +
            "AND subject_offered.academic_session_uuid = enrollments.academic_session_uuid)\n" +
            "JOIN course_subject ON subject_offered.course_subject_uuid = course_subject.uuid\n" +
            "JOIN subjects ON course_subject.subject_uuid = subjects.uuid\n" +
            "JOIN academic_sessions ON subject_offered.academic_session_uuid = academic_sessions.uuid\n" +
            "JOIN teacher_subjects ON (teacher_subjects.academic_session_uuid = subject_offered.academic_session_uuid\n" +
            "AND teacher_subjects.course_subject_uuid = subject_offered.course_subject_uuid)\n" +
            "LEFT JOIN lms.assignments ON assignments.course_subject_uuid = course_subject.uuid\n" +
            "LEFT JOIN lms.assignment_attempts ON (assignment_attempts.assignment_uuid = assignments.uuid\n" +
            "AND assignment_attempts.attempted_by = students.uuid\n" +
            "AND assignment_attempts.deleted_at IS NULL)\n" +
            "LEFT JOIN lms.assignment_attempt_marks ON (assignment_attempt_marks.assignment_attempt_uuid = assignment_attempts.uuid\n" +
            "AND assignment_attempt_marks.deleted_at IS NULL)\n" +
            "WHERE registrations.deleted_at IS NULL\n" +
            "AND course_subject.uuid = assignments.course_subject_uuid\n" +
            "AND students.deleted_at IS NULL\n" +
            "AND enrollments.deleted_at IS NULL\n" +
            "AND campus_course.deleted_at IS NULL\n" +
            "AND courses.deleted_at IS NULL\n" +
            "AND subject_offered.deleted_at IS NULL\n" +
            "AND academic_sessions.deleted_at IS NULL\n" +
            "AND course_subject.deleted_at IS NULL\n" +
            "AND teacher_subjects.deleted_at IS NULL\n" +
            "AND std_profiles.deleted_at IS NULL\n" +
            "AND subjects.deleted_at IS NULL\n" +
            "AND assignments.deleted_at IS NULL\n" +
            "AND assignments.uuid= :assignmentUUID\n" +
            "AND (assignments.name ILIKE concat('%',:assignmentName,'%') \n" +
            " or students.student_id ILIKE concat('%',:stdId,'%') ) ")
    Mono<Long> countAllEnrolledStudentsRecordAssignmentAttemptMarksFacadeCount(UUID assignmentUUID, String stdId, String assignmentName);

    /**
     * This Query Count All Student Records that are registered and Enrolled Against the same course subject and student that attempted the assignment and not attempted the assignment
     **/
    @Query("SELECT count(*) \n" +
            " FROM registrations\n" +
            " LEFT JOIN " +
            " students ON registrations.student_uuid = students.uuid \n" +
            " LEFT JOIN " +
            " campus_course ON registrations.campus_course_uuid = campus_course.uuid\n" +
            " LEFT JOIN " +
            "course_offered ON campus_course.uuid = course_offered.campus_course_uuid\n" +
            " LEFT JOIN " +
            "courses ON campus_course.course_uuid = courses.uuid\n" +
            " LEFT JOIN " +
            "enrollments ON registrations.student_uuid= enrollments.student_uuid\n" +
            " LEFT JOIN " +
            " subject_offered ON \n" +
            "(enrollments.subject_offered_uuid = subject_offered.uuid\n" +
            "AND course_offered.academic_session_uuid = subject_offered.academic_session_uuid)\n" +
            "LEFT JOIN course_subject ON \n" +
            "(subject_offered.course_subject_uuid = course_subject.uuid\n" +
            "AND courses.uuid = course_subject.course_uuid)\n" +
            "LEFT JOIN subjects on course_subject.subject_uuid = subjects.uuid\n" +
            " LEFT JOIN (SELECT assignments.*, \n" +
            " assignment_attempts.uuid as assignmentAttemptUUID,\n" +
            "assignment_attempts.attempted_by as attemptedBy,\n" +
            "  assignment_attempt_marks.uuid as assignmentAttemptMarks\n" +
            "  FROM lms.assignments\n" +
            "  LEFT JOIN lms.assignment_attempts ON assignments.uuid = assignment_attempts.assignment_uuid\n" +
            "  LEFT JOIN lms.assignment_attempt_marks ON assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid\n" +
            "  WHERE assignments.deleted_at IS NULL\n" +
            "  AND assignment_attempts.deleted_at IS NULL\n" +
            "  AND assignment_attempt_marks.deleted_at IS NULL\n" +
            "   ) AS assignment\n" +
            "     ON (course_subject.uuid = assignment.course_subject_uuid \n" +
            "     AND enrollments.student_uuid = assignment.attemptedBy) \n" +
            "     WHERE subjects.uuid = :subjectUUID\n" +
            "     AND registrations.deleted_at IS NULL \n" +
            "     AND campus_course.deleted_at IS NULL \n" +
            "     AND course_offered.deleted_at IS NULL \n" +
            "     AND courses.deleted_at IS NULL \n" +
            "     AND enrollments.deleted_at IS NULL \n" +
            "     AND subject_offered.deleted_at IS NULL \n" +
            "     AND subjects.deleted_at IS NULL \n" +
            "     AND course_subject.deleted_at IS NULL \n" +
            "     AND students.deleted_at IS NULL \n" +
            "     AND (assignment.name ILIKE concat('%',:assignmentName,'%') \n" +
            "     or students.student_id ILIKE concat('%',:stdId,'%') ) ")
    Mono<Long> countAllEnrolledStudentsRecordAssignmentAttemptMarksFacadeCountWithSubjectFilter(UUID subjectUUID, String stdId, String assignmentName);

    /**
     * Count Assignments Against Student,Course and Subject With Status Filter
     **/
    @Query("select count(*) \n" +
            "from lms.assignments\n" +
            "join public.course_subject on course_subject.uuid = assignments.course_subject_uuid\n" +
            "join public.subject_offered on (course_subject.uuid = subject_offered.course_subject_uuid and subject_offered.academic_session_uuid=assignments.academic_session_uuid)\n" +
            "join public.enrollments on subject_offered.uuid = enrollments.subject_offered_uuid \n" +
            "join public.registrations on enrollments.student_uuid = registrations.student_uuid\n" +
            "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid\n" +
            "where course_subject.course_uuid = :courseUUID\n" +
            "and course_subject.subject_uuid =  :subjectUUID\n" +
            "and enrollments.student_uuid = :studentUUID\n" +
            "and assignment_documents.deleted_at is null\n" +
            "and registrations.deleted_at is null\n" +
            "and enrollments.deleted_at is null\n" +
            "and subject_offered.deleted_at is null \n" +
            "and assignments.deleted_at is null \n" +
            "and course_subject.deleted_at is null\n" +
            "and assignments.status=:status \n" +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstStudentCourseSubjectWithStatus(UUID studentUUID, UUID courseUUID, UUID subjectUUID, Boolean status, String name, String instruction);

    @Query("select count(*) \n" +
            "from lms.assignments\n" +
            "join public.course_subject on course_subject.uuid = assignments.course_subject_uuid\n" +
            "join public.subject_offered on (course_subject.uuid = subject_offered.course_subject_uuid and subject_offered.academic_session_uuid=assignments.academic_session_uuid)\n" +
            "join public.enrollments on subject_offered.uuid = enrollments.subject_offered_uuid \n" +
            "join public.registrations on enrollments.student_uuid = registrations.student_uuid\n" +
            "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid\n" +
            "where course_subject.course_uuid = :courseUUID\n" +
            "and course_subject.subject_uuid =  :subjectUUID\n" +
            "and enrollments.student_uuid = :studentUUID\n" +
            "and assignment_documents.deleted_at is null\n" +
            "and registrations.deleted_at is null\n" +
            "and enrollments.deleted_at is null\n" +
            "and subject_offered.deleted_at is null \n" +
            "and assignments.deleted_at is null \n" +
            "and course_subject.deleted_at is null\n" +
            " AND (assignments.name ILIKE concat('%',:name,'%')" +
            " or assignments.instruction ILIKE concat('%',:instruction,'%'))")
    Mono<Long> countAssignmentsAgainstStudentCourseSubjectWithoutStatus(UUID studentUUID, UUID courseUUID, UUID subjectUUID, String name, String instruction);

}
