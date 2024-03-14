package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto {
    /** This Dto is used to show All students who are enrolled in course subject
     * and who are assigned the Assignment and who attempted the assignment with marks**/

    private UUID studentUUID;

    private UUID assignmentUUID;

    private UUID attemptedByUUID;

    private UUID assignmentAttemptUUID;

    private UUID assignmentAttemptMarksUUID;

    private String assignmentName;

    private Boolean assignmentLockStatus;

    private String studentCode;

    private String stdFirstName;

    private String stdLastName;

    private String registrationNo;

    private String courseName;

    private String subjectName;

    private Double totalMarks;

    private Double obtainedMarks;
}
