package tuf.webscaf.app.dbContext.slave.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SlaveLmsModuleDto {
    String moduleId;
    UUID moduleUUID;
    String baseUrl;
    String infoUrl;
    String hostAddress;
}
