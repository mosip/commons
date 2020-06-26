package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
//@ApiModel(value = "Application", description = "Application resource representation")
public class ApplicationDto {


	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "code", required = true, dataType = "java.lang.String")
	private String code;


	@NotNull
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;

	@Size(min = 0, max = 128)
	@ApiModelProperty(value = "Application description", required = false, dataType = "java.lang.String")
	private String description;

	@ValidLangCode(message = "Language Code is Invalid")
	@ApiModelProperty(value = "Language Code", required = true, dataType = "java.lang.String")
	private String langCode;

	@NotNull
	@ApiModelProperty(value = "Application isActive Status", required = true, dataType = "java.lang.Boolean")
	private Boolean isActive;

}
