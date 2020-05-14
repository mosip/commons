package io.mosip.kernel.packetmanager.dto;

import io.mosip.kernel.core.http.ResponseWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SchemaResponseDto extends ResponseWrapper<SchemaResponse> {
}
