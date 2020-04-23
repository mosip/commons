package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"code", "langCode"})
public class DynamicFieldValueDto {

	@NotBlank
	private String code;
	
	@NotBlank
	private String value;
	
	private boolean isActive;
	
	@NotBlank
	private String langCode;
}
