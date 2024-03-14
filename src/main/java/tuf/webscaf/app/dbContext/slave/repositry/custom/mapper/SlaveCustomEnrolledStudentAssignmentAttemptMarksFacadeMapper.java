package tuf.webscaf.app.dbContext.slave.repositry.custom.mapper;

import io.r2dbc.spi.Row;
import tuf.webscaf.app.dbContext.slave.dto.SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentEntity;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomEnrolledStudentAssignmentAttemptMarksFacadeMapper implements BiFunction<Row, Object, SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto> {
    @Override
    public SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto apply(Row source, Object o) {

        return SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto.builder()
                .studentUUID(source.get("studentUUID", UUID.class))
                .studentCode(source.get("studentCode", String.class))
                .assignmentUUID(source.get("assignmentUUID", UUID.class))
                .attemptedByUUID(source.get("attemptedBy", UUID.class))
                .assignmentAttemptUUID(source.get("assignmentAttemptUUID", UUID.class))
                .assignmentAttemptMarksUUID(source.get("assignmentAttemptMarksUUID", UUID.class))
                .assignmentName(source.get("assignmentName", String.class))
                .assignmentLockStatus(source.get("assignmentLockStatus", Boolean.class))
                .studentCode(source.get("studentCode", String.class))
                .stdFirstName(source.get("stdFirstName", String.class))
                .stdLastName(source.get("stdLastName", String.class))
                .registrationNo(source.get("registrationNo", String.class))
                .courseName(source.get("courseName", String.class))
                .subjectName(source.get("subjectName", String.class))
                .totalMarks(source.get("totalMarks", Double.class))
                .obtainedMarks(source.get("obtainedMarks", Double.class))
                .build();

    }
}
