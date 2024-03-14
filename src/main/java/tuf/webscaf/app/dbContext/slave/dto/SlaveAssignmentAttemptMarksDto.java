package tuf.webscaf.app.dbContext.slave.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonSerialize
public class SlaveAssignmentAttemptMarksDto {

    private Long id;

    private Long version;

    private UUID uuid;

    private Boolean status;

    private String key;

    private String comments;

    private UUID assignmentAttemptUUID;

    private Double obtainedMarks;

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
