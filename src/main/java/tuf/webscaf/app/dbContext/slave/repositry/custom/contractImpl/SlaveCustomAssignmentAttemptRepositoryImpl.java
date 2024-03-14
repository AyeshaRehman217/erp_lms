package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttachmentDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttemptDto;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAssignmentAttemptRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomAssignmentAttemptMapper;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomAssignmentDtoMapper;

import java.util.UUID;


public class SlaveCustomAssignmentAttemptRepositoryImpl implements SlaveCustomAssignmentAttemptRepository {
    private DatabaseClient client;
    private SlaveAssignmentAttemptDto slaveAssignmentAttemptDto;

    @Autowired
    public SlaveCustomAssignmentAttemptRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> indexWithoutStatus(String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*, assignments.name as assignmentName ,assignments.total_mark as totalMarks,\n" +
                " assignment_attempt_marks.obtained_marks as obtainedMarks,assignment_attempt_documents.document_uuid as documentUUID,assignment_attempt_documents.assignment_attempt_uuid as docAssignmentUUID \n" +
                " from lms.assignment_attempts \n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
                " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
                " where assignment_attempts.deleted_at IS NULL\n" +
                " AND assignments.deleted_at IS NULL\n" +
                " AND assignment_attempt_documents.deleted_at IS NULL\n" +
                " AND assignment_attempt_marks.deleted_at IS NULL\n" +
                " AND (assignments.name ILIKE '%" + assignmentName + "%')\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> indexWithStatus(Boolean status, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*, assignments.name as assignmentName ,assignments.total_mark as totalMarks,\n" +
                " assignment_attempt_marks.obtained_marks as obtainedMarks,assignment_attempt_documents.document_uuid as documentUUID,assignment_attempt_documents.assignment_attempt_uuid as docAssignmentUUID \n" +
                " from lms.assignment_attempts \n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
                " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
                " where assignment_attempts.deleted_at IS NULL\n" +
                " AND assignments.deleted_at IS NULL\n" +
                " AND assignment_attempt_documents.deleted_at IS NULL\n" +
                " AND assignment_attempt_marks.deleted_at IS NULL\n" +
                "  and  assignment_attempts.status = " + status +
                " AND (assignments.name ILIKE '%" + assignmentName + "%')\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> indexAgainstAttemptedByWithoutStatus(UUID attemptedByUUID, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*, assignments.name as assignmentName , assignments.total_mark as totalMarks," +
                "assignment_attempt_marks.obtained_marks as obtainedMarks, assignment_attempt_documents.document_uuid as documentUUID,assignment_attempt_documents.assignment_attempt_uuid as docAssignmentUUID \n" +
                " from lms.assignment_attempts \n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
                " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
                " where assignment_attempts.deleted_at IS NULL\n" +
                " AND assignments.deleted_at IS NULL\n" +
                " AND assignment_attempt_marks.deleted_at IS NULL\n" +
                " AND assignment_attempt_documents.deleted_at IS NULL\n" +
                " and  assignment_attempts.attempted_by = '" + attemptedByUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%')\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> indexAgainstAttemptedByWithStatus(UUID attemptedByUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*, assignments.name as assignmentName , assignments.total_mark as totalMarks," +
                "assignment_attempt_marks.obtained_marks as obtainedMarks, assignment_attempt_documents.document_uuid as documentUUID,assignment_attempt_documents.assignment_attempt_uuid as docAssignmentUUID \n" +
                " from lms.assignment_attempts \n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
                " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
                " where assignment_attempts.deleted_at IS NULL\n" +
                " AND assignments.deleted_at IS NULL\n" +
                " AND assignment_attempt_marks.deleted_at IS NULL\n" +
                " AND assignment_attempt_documents.deleted_at IS NULL\n" +
                " and  assignment_attempts.status = " + status +
                " and  assignment_attempts.attempted_by = '" + attemptedByUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%')\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> mappedAssignmentAgainstAttemptedByWithoutStatus(UUID attemptedByUUID, String assignmentName, String comment, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*,stdAssignments.stdUUID,stdAssignments.assignmentName as assignmentName from \n" +
                " (select assignments.name as assignmentName,students.uuid as stdUUID,assignments.uuid as assignmentUUID\n" +
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
                " and enrollments.student_uuid= '" + attemptedByUUID +
                "' ) as stdAssignments\n" +
                " join lms.assignment_attempts on stdAssignments.assignmentUUID=assignment_attempts.assignment_uuid \n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " and stdAssignments.stdUUID=assignment_attempts.attempted_by \n" +
                " where assignment_attempts.attempted_by = '" + attemptedByUUID +
                "' and assignment_attempts.deleted_at is null \n" +
                " and assignment_attempt_documents.deleted_at is null \n" +
                " AND (stdAssignments.assignmentName ILIKE '%" + assignmentName + "%' or assignment_attempts.comment ILIKE '%" + comment + "%' )\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> mappedAssignmentAgainstAttemptedByWithStatus(UUID attemptedByUUID, Boolean status, String assignmentName, String comment, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*,stdAssignments.stdUUID,stdAssignments.assignmentName as assignmentName from \n" +
                " (select assignments.name as assignmentName,students.uuid as stdUUID,assignments.uuid as assignmentUUID \n" +
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
                " and enrollments.student_uuid= '" + attemptedByUUID +
                "' ) as stdAssignments\n" +
                " join lms.assignment_attempts on stdAssignments.assignmentUUID=assignment_attempts.assignment_uuid\n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " and stdAssignments.stdUUID=assignment_attempts.attempted_by\n" +
                " where assignment_attempts.attempted_by = '" + attemptedByUUID +
                "' and assignment_attempts.deleted_at is null \n" +
                " and assignment_attempt_documents.deleted_at is null \n" +
                " and assignment_attempts.status = " + status +
                " AND (stdAssignments.assignmentName ILIKE '%" + assignmentName + "%' or assignment_attempts.comment ILIKE '%" + comment + "%' )\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> showAssignmentAttemptRecordAgainstUUID(UUID assignmentAttemptUUID) {
        String query = "select lms.assignment_attempts.* , assignments.name as assignmentName, assignments.total_mark as totalMarks,\n" +
                " assignment_attempt_marks.obtained_marks as obtainedMarks, " +
                "assignment_attempt_documents.document_uuid as documentUUID," +
                " assignment_attempt_documents.assignment_attempt_uuid as docAssignmentUUID \n" +
                "from lms.assignment_attempts \n" +
                "left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
                " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
                "where assignment_attempts.deleted_at is null \n" +
                "and assignment_attempt_documents.deleted_at is null \n" +
                " AND assignment_attempt_marks.deleted_at IS NULL\n" +
                "and assignments.deleted_at is null \n" +
                " and assignment_attempts.uuid='" + assignmentAttemptUUID + "' ";

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> indexAgainstAttemptedByAndAssignmentWithoutStatus(UUID attemptedByUUID, UUID assignmentUUID, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*, assignments.name as assignmentName , assignments.total_mark as totalMarks," +
                "assignment_attempt_marks.obtained_marks as obtainedMarks, assignment_attempt_documents.document_uuid as documentUUID,assignment_attempt_documents.assignment_attempt_uuid as docAssignmentUUID \n" +
                " from lms.assignment_attempts \n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
                " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
                " where assignment_attempts.deleted_at IS NULL\n" +
                " AND assignments.deleted_at IS NULL\n" +
                " AND assignment_attempt_marks.deleted_at IS NULL\n" +
                " AND assignment_attempt_documents.deleted_at IS NULL\n" +
                " and  assignment_attempts.attempted_by = '" + attemptedByUUID +
                "' and  assignments.uuid = '" + assignmentUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%')\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> indexAgainstAttemptedByAndAssignmentWithStatus(UUID attemptedByUUID, UUID assignmentUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*, assignments.name as assignmentName , assignments.total_mark as totalMarks," +
                "assignment_attempt_marks.obtained_marks as obtainedMarks, assignment_attempt_documents.document_uuid as documentUUID,assignment_attempt_documents.assignment_attempt_uuid as docAssignmentUUID \n" +
                " from lms.assignment_attempts \n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
                " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
                " where assignment_attempts.deleted_at IS NULL\n" +
                " AND assignments.deleted_at IS NULL\n" +
                " AND assignment_attempt_marks.deleted_at IS NULL\n" +
                " AND assignment_attempt_documents.deleted_at IS NULL\n" +
                " and  assignment_attempts.status = " + status +
                " and  assignment_attempts.attempted_by = '" + attemptedByUUID +
                "' and  assignments.uuid = '" + assignmentUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%')\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> indexAgainstAssignmentWithoutStatus(UUID assignmentUUID, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*, assignments.name as assignmentName , assignments.total_mark as totalMarks," +
                "assignment_attempt_marks.obtained_marks as obtainedMarks, assignment_attempt_documents.document_uuid as documentUUID,assignment_attempt_documents.assignment_attempt_uuid as docAssignmentUUID \n" +
                " from lms.assignment_attempts \n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
                " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
                " where assignment_attempts.deleted_at IS NULL\n" +
                " AND assignments.deleted_at IS NULL\n" +
                " AND assignment_attempt_marks.deleted_at IS NULL\n" +
                " AND assignment_attempt_documents.deleted_at IS NULL\n" +
                " and  assignments.uuid = '" + assignmentUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%')\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptDto> indexAgainstAssignmentWithStatus(UUID assignmentUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select assignment_attempts.*, assignments.name as assignmentName , assignments.total_mark as totalMarks," +
                "assignment_attempt_marks.obtained_marks as obtainedMarks, assignment_attempt_documents.document_uuid as documentUUID,assignment_attempt_documents.assignment_attempt_uuid as docAssignmentUUID \n" +
                " from lms.assignment_attempts \n" +
                " left join lms.assignment_attempt_documents on assignment_attempts.uuid=assignment_attempt_documents.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid = assignments.uuid \n" +
                " left join lms.assignment_attempt_marks on assignment_attempts.uuid = assignment_attempt_marks.assignment_attempt_uuid \n" +
                " where assignment_attempts.deleted_at IS NULL\n" +
                " AND assignments.deleted_at IS NULL\n" +
                " AND assignment_attempt_marks.deleted_at IS NULL\n" +
                " AND assignment_attempt_documents.deleted_at IS NULL\n" +
                " and  assignment_attempts.status = " + status +
                " and  assignments.uuid = '" + assignmentUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%')\n" +
                " ORDER BY assignment_attempts." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMapper mapper = new SlaveCustomAssignmentAttemptMapper();

        Flux<SlaveAssignmentAttemptDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptDto))
                .all();

        return result;
    }

}
