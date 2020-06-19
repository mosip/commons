package io.mosip.kernel.masterdata.dto.getresponse;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.kernel.masterdata.dto.SchemaDto;
import lombok.Data;

@Data
public class IdSchemaResponseDto {

	private String id;	
	private double idVersion;
	private String title;
	private String description;
	private List<SchemaDto> schema;
	private String schemaJson;
	private String status;
	private LocalDateTime effectiveFrom;
	private String createdBy;
	private String updatedBy;
	private LocalDateTime createdOn;
	private LocalDateTime updatedOn;
	
}
