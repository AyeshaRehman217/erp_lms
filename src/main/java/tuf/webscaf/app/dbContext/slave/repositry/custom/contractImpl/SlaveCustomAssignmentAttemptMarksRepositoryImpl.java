package tuf.webscaf.app.dbContext.slave.repositry.custom.contractImpl;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttemptDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttemptMarksDto;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAssignmentAttemptMarksRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.contract.SlaveCustomAssignmentAttemptRepository;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomAssignmentAttemptMapper;
import tuf.webscaf.app.dbContext.slave.repositry.custom.mapper.SlaveCustomAssignmentAttemptMarksMapper;

import java.util.UUID;


public class SlaveCustomAssignmentAttemptMarksRepositoryImpl implements SlaveCustomAssignmentAttemptMarksRepository {
    private DatabaseClient client;
    private SlaveAssignmentAttemptMarksDto slaveAssignmentAttemptMarksDto;

    @Autowired
    public SlaveCustomAssignmentAttemptMarksRepositoryImpl(@Qualifier("slave") ConnectionFactory cf) {
        this.client = DatabaseClient.create(cf);
    }

    @Override
    public Flux<SlaveAssignmentAttemptMarksDto> indexWithoutStatus(String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignment_attempt_marks.*,assignments.name as assignmentName \n" +
                " from lms.assignment_attempt_marks\n" +
                " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid\n" +
                " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid\n" +
                " where assignment_attempt_marks.deleted_at is null\n" +
                " and assignment_attempts.deleted_at is null \n" +
                " and assignments.deleted_at is null \n" +
                " AND (assignments.name ILIKE '%" + assignmentName + "%') \n" +
                " ORDER BY assignment_attempt_marks." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMarksMapper mapper = new SlaveCustomAssignmentAttemptMarksMapper();

        Flux<SlaveAssignmentAttemptMarksDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptMarksDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptMarksDto> indexWithStatus(Boolean status, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignment_attempt_marks.*,assignments.name as assignmentName \n" +
                " from lms.assignment_attempt_marks \n" +
                " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
                " where assignment_attempt_marks.status= " + status +
                " and assignment_attempt_marks.deleted_at is null \n" +
                " and assignment_attempts.deleted_at is null \n" +
                " and assignments.deleted_at is null \n" +
                " AND (assignments.name ILIKE '%" + assignmentName + "%') \n" +
                " ORDER BY assignment_attempt_marks." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMarksMapper mapper = new SlaveCustomAssignmentAttemptMarksMapper();

        Flux<SlaveAssignmentAttemptMarksDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptMarksDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptMarksDto> indexAgainstStudentAndAssignmentAttemptWithoutStatus(UUID studentUUID, UUID assignmentAttemptUUID, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignment_attempt_marks.*,assignments.name as assignmentName \n" +
                " from lms.assignment_attempt_marks\n" +
                " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid\n" +
                " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid\n" +
                " where assignment_attempt_marks.deleted_at is null\n" +
                " and assignment_attempts.deleted_at is null \n" +
                " and assignments.deleted_at is null \n" +
                " and assignment_attempts.attempted_by= '" + studentUUID +
                "' and assignment_attempt_marks.assignment_attempt_uuid = '" + assignmentAttemptUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%' ) \n" +
                " ORDER BY assignment_attempt_marks." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMarksMapper mapper = new SlaveCustomAssignmentAttemptMarksMapper();

        Flux<SlaveAssignmentAttemptMarksDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptMarksDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptMarksDto> indexAgainstStudentAndAssignmentAttemptWithStatus(UUID studentUUID, UUID assignmentAttemptUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignment_attempt_marks.*,assignments.name as assignmentName \n" +
                " from lms.assignment_attempt_marks\n" +
                " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid\n" +
                " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid\n" +
                " where assignment_attempt_marks.status= " + status +
                " and assignment_attempt_marks.deleted_at is null\n" +
                " and assignment_attempts.deleted_at is null \n" +
                " and assignments.deleted_at is null \n" +
                " and assignment_attempts.attempted_by= '" + studentUUID +
                "' and assignment_attempt_marks.assignment_attempt_uuid = '" + assignmentAttemptUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%') \n" +
                " ORDER BY assignment_attempt_marks." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMarksMapper mapper = new SlaveCustomAssignmentAttemptMarksMapper();

        Flux<SlaveAssignmentAttemptMarksDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptMarksDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptMarksDto> indexAgainstStudentWithoutStatus(UUID studentUUID, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignment_attempt_marks.*,assignments.name as assignmentName \n" +
                " from lms.assignment_attempt_marks\n" +
                " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid\n" +
                " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid\n" +
                " where assignment_attempt_marks.deleted_at is null\n" +
                " and assignment_attempts.deleted_at is null \n" +
                " and assignments.deleted_at is null \n" +
                " and assignment_attempts.attempted_by= '" + studentUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%') \n" +
                " ORDER BY assignment_attempt_marks." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMarksMapper mapper = new SlaveCustomAssignmentAttemptMarksMapper();

        Flux<SlaveAssignmentAttemptMarksDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptMarksDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptMarksDto> indexAgainstStudentWithStatus(UUID studentUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignment_attempt_marks.*,assignments.name as assignmentName \n" +
                " from lms.assignment_attempt_marks\n" +
                " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid\n" +
                " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid\n" +
                " where assignment_attempt_marks.status= " + status +
                " and assignment_attempt_marks.deleted_at is null\n" +
                " and assignment_attempts.deleted_at is null \n" +
                " and assignments.deleted_at is null \n" +
                " and assignment_attempts.attempted_by= '" + studentUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%') \n" +
                " ORDER BY assignment_attempt_marks." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMarksMapper mapper = new SlaveCustomAssignmentAttemptMarksMapper();

        Flux<SlaveAssignmentAttemptMarksDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptMarksDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptMarksDto> indexAgainstAssignmentAttemptWithoutStatus(UUID assignmentAttemptUUID, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignment_attempt_marks.*,assignments.name as assignmentName \n" +
                " from lms.assignment_attempt_marks\n" +
                " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid\n" +
                " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid\n" +
                " where assignment_attempt_marks.deleted_at is null\n" +
                " and assignment_attempts.deleted_at is null \n" +
                " and assignments.deleted_at is null \n" +
                " and assignment_attempt_marks.assignment_attempt_uuid = '" + assignmentAttemptUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%') \n" +
                " ORDER BY assignment_attempt_marks." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMarksMapper mapper = new SlaveCustomAssignmentAttemptMarksMapper();

        Flux<SlaveAssignmentAttemptMarksDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptMarksDto))
                .all();

        return result;
    }

    @Override
    public Flux<SlaveAssignmentAttemptMarksDto> indexAgainstAssignmentAttemptWithStatus(UUID assignmentAttemptUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page) {
        String query = "select lms.assignment_attempt_marks.*,assignments.name as assignmentName \n" +
                " from lms.assignment_attempt_marks \n" +
                " join lms.assignment_attempts on assignment_attempts.uuid=assignment_attempt_marks.assignment_attempt_uuid \n" +
                " join lms.assignments on assignment_attempts.assignment_uuid=assignments.uuid \n" +
                " where assignment_attempt_marks.status= " + status +
                " and assignment_attempt_marks.deleted_at is null \n" +
                " and assignment_attempts.deleted_at is null \n" +
                " and assignments.deleted_at is null \n" +
                " and assignment_attempt_marks.assignment_attempt_uuid = '" + assignmentAttemptUUID +
                "' AND (assignments.name ILIKE '%" + assignmentName + "%') \n" +
                " ORDER BY assignment_attempt_marks." + dp + " " + d +
                " LIMIT " + size + " OFFSET " + page;

        SlaveCustomAssignmentAttemptMarksMapper mapper = new SlaveCustomAssignmentAttemptMarksMapper();

        Flux<SlaveAssignmentAttemptMarksDto> result = client.sql(query)
                .map(row -> mapper.apply(row, slaveAssignmentAttemptMarksDto))
                .all();

        return result;
    }
}
