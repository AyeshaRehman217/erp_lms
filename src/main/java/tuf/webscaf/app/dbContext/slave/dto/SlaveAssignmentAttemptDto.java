package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.*;
;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveAssignmentAttemptDto {

    private Long id;

    private Long version;

    private UUID uuid;

    private Boolean status;

    private String assignmentName;

    private Double totalMarks;

    private Double obtainedMarks;

    private Boolean submissionStatus;

    private String comment;

    private UUID attemptedBy;

    private UUID assignmentUUID;

    private List<SlaveAttachmentDto> documents;

    @CreatedBy
    private UUID createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @CreatedBy
    private UUID updatedBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private UUID deletedBy;

    private LocalDateTime deletedAt;

    private UUID reqCompanyUUID;

    private UUID reqBranchUUID;

    private String reqCreatedBrowser;

    private String reqCreatedIP;

    private String reqCreatedPort;

    private String reqCreatedOS;

    private String reqCreatedDevice;

    private String reqCreatedReferer;

    private String reqUpdatedBrowser;

    private String reqUpdatedIP;

    private String reqUpdatedPort;

    private String reqUpdatedOS;

    private String reqUpdatedDevice;

    private String reqUpdatedReferer;

    private String reqDeletedBrowser;

    private String reqDeletedIP;

    private String reqDeletedPort;

    private String reqDeletedOS;

    private String reqDeletedDevice;

    private String reqDeletedReferer;

    private Boolean editable;

    private Boolean deletable;

    private Boolean archived;
}
