package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"code", "langCode"})
public class DynamicFieldValueDto {

	@NotNull
	private String code;
	
	@NotNull
	private String value;
	
	private boolean isActive;
	
	@NotNull
	private String langCode;
}
