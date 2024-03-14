package tuf.webscaf.springDocImpl;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StoreAssignmentDocumentDocImpl {

    @Column("status")
    private Boolean status;

    @Column("name")
    @Schema(required = true)
    private String name;

    @Column("instruction")
    private String instruction;

    @Column("from_date")
    @Schema(required = true)
    private LocalDateTime fromDate;

    @Column("to_date")
    @Schema(required = true)
    private LocalDateTime toDate;

    @Column("extended_date")
    private LocalDateTime extendedDate;

    @Column("total_mark")
    @Schema(required = true)
    private Double totalMark;

    @Column("academic_session_uuid")
    @Schema(required = true)
    private UUID academicSessionUUID;

    @Column("course_subject_uuid")
    @Schema(required = true)
    private UUID courseSubjectUUID;

    //Get Document UUIDs
    @Schema(required = true)
    private List<UUID> documentUUID;
}
