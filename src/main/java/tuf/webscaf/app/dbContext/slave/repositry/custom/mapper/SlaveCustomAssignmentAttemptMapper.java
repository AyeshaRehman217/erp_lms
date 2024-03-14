package tuf.webscaf.app.dbContext.slave.repositry.custom.mapper;

import io.r2dbc.spi.Row;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttemptDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAttachmentDto;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

public class SlaveCustomAssignmentAttemptMapper implements BiFunction<Row, Object, SlaveAssignmentAttemptDto> {

    MultiValueMap<String, SlaveAttachmentDto> assignAttachDtoMap = new LinkedMultiValueMap<>();

    @Override
    public SlaveAssignmentAttemptDto apply(Row source, Object o) {

        SlaveAttachmentDto attachmentDto = null;

        try {
            attachmentDto = SlaveAttachmentDto.builder()
                    .uuid(source.get("documentUUID", UUID.class))
                    .build();
        } catch (IllegalArgumentException ignored) {
        }

        String documentAssignmentUUID = source.get("docAssignmentUUID", String.class);

        if (documentAssignmentUUID != null) {
            assignAttachDtoMap.add(documentAssignmentUUID, attachmentDto);
        }

        return SlaveAssignmentAttemptDto.builder()
                .id(source.get("id", Long.class))
                .version(source.get("version", Long.class))
                .uuid(source.get("uuid", UUID.class))
                .status(source.get("status", Boolean.class))
                .comment(source.get("comment", String.class))
                .submissionStatus(source.get("submission_status", Boolean.class))
                .assignmentName(source.get("assignmentName", String.class))
                .totalMarks(source.get("totalMarks", Double.class))
                .obtainedMarks(source.get("obtainedMarks", Double.class))
                .documents(assignAttachDtoMap.get(documentAssignmentUUID))
                .attemptedBy(source.get("attempted_by", UUID.class))
                .assignmentUUID(source.get("assignment_uuid", UUID.class))
                .createdBy(source.get("created_by", UUID.class))
                .createdAt(source.get("created_at", LocalDateTime.class))
                .updatedBy(source.get("updated_by", UUID.class))
                .updatedAt(source.get("updated_at", LocalDateTime.class))
                .deletable(source.get("deletable", Boolean.class))
                .archived(source.get("archived", Boolean.class))
                .editable(source.get("editable", Boolean.class))
                .reqCompanyUUID(source.get("req_company_uuid", UUID.class))
                .reqBranchUUID(source.get("req_branch_uuid", UUID.class))
                .reqCreatedIP(source.get("req_created_ip", String.class))
                .reqCreatedPort(source.get("req_created_port", String.class))
                .reqCreatedBrowser(source.get("req_created_browser", String.class))
                .reqCreatedOS(source.get("req_created_os", String.class))
                .reqCreatedDevice(source.get("req_created_device", String.class))
                .reqCreatedReferer(source.get("req_created_referer", String.class))
                .reqUpdatedIP(source.get("req_updated_ip", String.class))
                .reqUpdatedPort(source.get("req_updated_port", String.class))
                .reqUpdatedBrowser(source.get("req_updated_browser", String.class))
                .reqUpdatedOS(source.get("req_updated_os", String.class))
                .reqUpdatedDevice(source.get("req_updated_device", String.class))
                .reqUpdatedReferer(source.get("req_updated_referer", String.class))
                .build();

    }
}
