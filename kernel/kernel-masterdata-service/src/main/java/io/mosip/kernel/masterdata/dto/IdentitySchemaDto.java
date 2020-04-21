package io.mosip.kernel.masterdata.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "Model representing a ID schema request")
public class IdentitySchemaDto {
	
	@ApiModelProperty(notes = "Schema version, autofilled by service", required = false)
	private double schemaVersion;
	
	@ApiModelProperty(notes = "Schema title", required = false)
	@NotBlank
	private String title;
	
	@ApiModelProperty(notes = "Schema description", required = false)
	@NotBlank
	private String description;
	
	@ApiModelProperty(notes = "schema", required = true)
	@NotEmpty
	private List<SchemaDto> schema;
		
	@NotNull
	@ApiModelProperty(notes = "schema Effective From", required = false)
	private LocalDateTime effectiveFrom;
	
}