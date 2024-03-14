package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomEnrolledStudentsAssignmentAttemptMarksFacadeRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomEnrolledStudentAssignmentAttemptMarksFacadeMapper;

import java.util.UUID;


public class SlaveCustomEnrolledStudentsAssignmentAttemptMarksFacadeRepositoryImpl implements SlaveCustomEnrolledStudentsAssignmentAttemptMarksFacadeRepository {
    private DatabaseClient client;
    private SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto slaveEnrolledStudentsAssignmentAndAttemptAndMarksDto;

    @Autowired
    public SlaveCustomEnrolledStudentsAssignmentAttemptMarksFacadeRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto> indexAllRecords(UUID assignmentUUID, String stdId, String firstName, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "SELECT registrations.student_uuid as studentUUID,\n" +
                "students.student_id as studentCode,\n" +
                "std_profiles.first_name as stdFirstName,\n" +
                "std_profiles.last_name as stdLastName,\n" +
                "registrations.registration_no as registrationNo,\n" +
                "courses.name as courseName,\n" +
                "enrollments.subject_offered_uuid as subjectOfferedUUID,\n" +
                "course_subject.subject_uuid as subjectUUID,\n" +
                "course_subject.course_uuid as courseUUID,\n" +
                "subjects.name as subjectName,\n" +
                "assignments.uuid as assignmentUUID,\n" +
                "assignments.name as assignmentName,\n" +
                "assignments.lock as assignmentLockStatus,\n" +
                "assignments.total_mark as totalMarks,\n" +
                "assignment_attempts.uuid as assignmentAttemptUUID,\n" +
                "assignment_attempts.attempted_by as attemptedBy,\n" +
                "assignment_attempt_marks.obtained_marks as obtainedMarks,\n" +
                "assignment_attempt_marks.uuid as assignmentAttemptMarksUUID\n" +
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
                "WHERE assignments.uuid = '" + assignmentUUID +
                "' AND registrations.deleted_at IS NULL\n" +
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
                "AND (assignments.name ILIKE '%" + assignmentName + "%' \n" +
                " or students.student_id ILIKE '%" + stdId + "%' \n" +
                " or std_profiles.first_name ILIKE '%" + firstName + "%') \n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomEnrolledStudentAssignmentAttemptMarksFacadeMapper mapper = new SlaveCustomEnrolledStudentAssignmentAttemptMarksFacadeMapper();

        Flux<SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveEnrolledStudentsAssignmentAndAttemptAndMarksDto))
                .all();

        return result;
    }
}
