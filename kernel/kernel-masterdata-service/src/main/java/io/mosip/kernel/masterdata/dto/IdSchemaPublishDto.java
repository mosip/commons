package io.mosip.kernel.masterdata.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class IdSchemaPublishDto {
	
	@NotNull
	private String id;
	
	@NotNull
	private LocalDateTime effectiveFrom;

}
