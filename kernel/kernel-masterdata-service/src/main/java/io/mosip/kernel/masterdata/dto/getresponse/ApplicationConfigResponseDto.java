package io.mosip.kernel.masterdata.dto.getresponse;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ApplicationConfigResponseDto {
	

	
	@ApiModelProperty(value = "Language Code", required = true, dataType = "java.lang.String")
	private String primaryLangCode;
	
	
	@ApiModelProperty(value = "Language Code", required = true, dataType = "java.lang.String")
	private String secondaryLangCode;
	
	
	@ApiModelProperty(value = "Current Version", required = true, dataType = "java.lang.String")
	private String version;

	
}
