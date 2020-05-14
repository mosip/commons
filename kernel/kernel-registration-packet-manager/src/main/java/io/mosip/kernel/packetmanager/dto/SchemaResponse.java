package io.mosip.kernel.packetmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemaResponse {
    private String id;
    private String idVersion;
    private String schema;
}
