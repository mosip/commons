package io.mosip.kernel.syncdata.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"code", "langCode"})
public class DynamicFieldValueDto {

	
	private String code;	
	private String value;	
	private boolean isActive;	
	private String langCode;
}
