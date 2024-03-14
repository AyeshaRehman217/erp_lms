package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttachmentDto;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAssignmentRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomAssignmentDtoMapper;

import java.util.UUID;


public class SlaveCustomAssignmentRepositoryImpl implements SlaveCustomAssignmentRepository {
    private DatabaseClient client;
    private SlaveAssignmentAttachmentDto slaveAssignmentAttachmentDto;

    @Autowired
    public SlaveCustomAssignmentRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstSubjectWithoutStatus(UUID subjectUUID, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.*,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments\n" +
                "join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "join course_subject on assignments.course_subject_uuid=course_subject.uuid \n" +
                "join subjects on subjects.uuid=course_subject.subject_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and course_subject.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null\n" +
                "and subjects.deleted_at is null\n" +
                "and subjects.uuid='" + subjectUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstSubjectWithStatus(UUID subjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.*,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                " from lms.assignments\n" +
                " join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                " join course_subject on assignments.course_subject_uuid=course_subject.uuid\n" +
                " join subjects on subjects.uuid=course_subject.subject_uuid\n" +
                " where assignments.deleted_at is null\n" +
                " and assignments.status = " + status +
                " and course_subject.deleted_at is null\n" +
                "and assignment_documents.deleted_at is null\n" +
                " and subjects.deleted_at is null\n" +
                " and subjects.uuid='" + subjectUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexWithoutStatus(String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments\n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null\n" +
                "and assignment_documents.deleted_at is null\n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexWithStatus(Boolean status, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.*,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                "and assignments.status = " + status +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherAndCourseSubjectWithoutStatus(UUID teacherUUID, UUID courseSubjectUUID, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                "and assignments.teacher_uuid ='" + teacherUUID + "' \n" +
                "and assignments.course_subject_uuid ='" + courseSubjectUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherAndCourseSubjectWithStatus(UUID teacherUUID, UUID courseSubjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.status = " + status +
                " and assignments.teacher_uuid ='" + teacherUUID + "' \n" +
                "and assignments.course_subject_uuid ='" + courseSubjectUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstAcademicSessionAndCourseSubjectWithoutStatus(UUID academicSessionUUID, UUID courseSubjectUUID, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.academic_session_uuid ='" + academicSessionUUID + "' \n" +
                "and assignments.course_subject_uuid ='" + courseSubjectUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstAcademicSessionAndCourseSubjectWithStatus(UUID academicSessionUUID, UUID courseSubjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.status = " + status +
                " and assignments.academic_session_uuid ='" + academicSessionUUID + "' \n" +
                "and assignments.course_subject_uuid ='" + courseSubjectUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherAndCourseSubjectAndAcademicSessionWithoutStatus(UUID teacherUUID, UUID courseSubjectUUID, UUID academicSessionUUID, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.teacher_uuid ='" + teacherUUID + "' \n" +
                "and assignments.course_subject_uuid ='" + courseSubjectUUID + "' \n" +
                "and assignments.academic_session_uuid ='" + academicSessionUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherAndCourseSubjectAndAcademicSessionWithStatus(UUID teacherUUID, UUID courseSubjectUUID, UUID academicSessionUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.status = " + status +
                " and assignments.teacher_uuid ='" + teacherUUID + "' \n" +
                "and assignments.course_subject_uuid ='" + courseSubjectUUID + "' \n" +
                "and assignments.academic_session_uuid ='" + academicSessionUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstCourseSubjectWithoutStatus(UUID courseSubjectUUID, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.course_subject_uuid ='" + courseSubjectUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstCourseSubjectWithStatus(UUID courseSubjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.status = " + status +
                " and assignments.course_subject_uuid ='" + courseSubjectUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstAcademicSessionWithoutStatus(UUID academicSessionUUID, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.academic_session_uuid ='" + academicSessionUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstAcademicSessionWithStatus(UUID academicSessionUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.status = " + status +
                " and assignments.academic_session_uuid ='" + academicSessionUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherWithoutStatus(UUID teacherUUID, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.teacher_uuid ='" + teacherUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherWithStatus(UUID teacherUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.status = " + status +
                " and assignments.teacher_uuid ='" + teacherUUID + "' \n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAssignmentsAgainstStudentCourseSubjectWithoutStatus(UUID studentUUID, UUID courseUUID, UUID subjectUUID, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "join public.course_subject on course_subject.uuid = assignments.course_subject_uuid\n" +
                "join public.subject_offered on (course_subject.uuid = subject_offered.course_subject_uuid and subject_offered.academic_session_uuid=assignments.academic_session_uuid)\n" +
                "join public.enrollments on subject_offered.uuid = enrollments.subject_offered_uuid \n" +
                "join public.registrations on enrollments.student_uuid = registrations.student_uuid\n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid\n" +
                "where course_subject.course_uuid = '" + courseUUID +
                "' and course_subject.subject_uuid = '" + subjectUUID +
                "' and enrollments.student_uuid = '" + studentUUID +
                "' and assignment_documents.deleted_at is null\n" +
                "and registrations.deleted_at is null\n" +
                "and enrollments.deleted_at is null\n" +
                "and subject_offered.deleted_at is null \n" +
                "and assignments.deleted_at is null \n" +
                "and course_subject.deleted_at is null\n" +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> indexAssignmentsAgainstStudentCourseSubjectWithStatus(UUID studentUUID, UUID courseUUID, UUID subjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "join public.course_subject on course_subject.uuid = assignments.course_subject_uuid\n" +
                "join public.subject_offered on (course_subject.uuid = subject_offered.course_subject_uuid and subject_offered.academic_session_uuid=assignments.academic_session_uuid)\n" +
                "join public.enrollments on subject_offered.uuid = enrollments.subject_offered_uuid \n" +
                "join public.registrations on enrollments.student_uuid = registrations.student_uuid\n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid\n" +
                "where course_subject.course_uuid = '" + courseUUID +
                "' and course_subject.subject_uuid = '" + subjectUUID +
                "' and enrollments.student_uuid = '" + studentUUID +
                "' and assignment_documents.deleted_at is null\n" +
                "and registrations.deleted_at is null\n" +
                "and enrollments.deleted_at is null\n" +
                "and subject_offered.deleted_at is null \n" +
                "and assignments.deleted_at is null \n" +
                "and course_subject.deleted_at is null\n" +
                " and assignments.status = " + status +
                " AND (assignments.name ILIKE '%" + name + "%' " +
                " or assignments.instruction ILIKE '%" + instruction + "%')\n" +
                " ORDER BY assignments." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttachmentDto> showAssignmentRecordAgainstUUID(UUID assignmentUUID) {
        String query = "select lms.assignments.* ,assignment_documents.document_uuid as documentUUID,assignment_documents.assignment_uuid as docAssignmentUUID \n" +
                "from lms.assignments \n" +
                "left join lms.assignment_documents on assignments.uuid=assignment_documents.assignment_uuid \n" +
                "where assignments.deleted_at is null \n" +
                "and assignment_documents.deleted_at is null \n" +
                " and assignments.uuid='" + assignmentUUID + "' ";

        SlaveCustomAssignmentDtoMapper mapper = new SlaveCustomAssignmentDtoMapper();

        Flux<SlaveAssignmentAttachmentDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttachmentDto))
                .all();

        return result;
    }

}
