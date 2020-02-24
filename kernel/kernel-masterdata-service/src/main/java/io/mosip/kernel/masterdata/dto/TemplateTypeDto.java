package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data

public class TemplateTypeDto {

	/**
	 * Field for code
	 */
	@NotBlank
	@Size(min = 1, max = 36)
	@ApiModelProperty(value = "code", required = true, dataType = "java.lang.String")
	private String code;

	/**
	 * Field for language code
	 */
	// @NotBlank
	// @Size(min = 1, max = 3)
	@ValidLangCode(message = "Language Code is Invalid")
	@ApiModelProperty(value = "Language Code", required = true, dataType = "java.lang.String")
	private String langCode;

	/**
	 * Field for description
	 */
	@Size(min = 0, max = 256)
	@ApiModelProperty(value = "Biometric Attribute desc", required = false, dataType = "java.lang.String")
	private String description;

	/**
	 * Field for the status of data.
	 */
	@ApiModelProperty(value = "BiometricAttribute isActive status", required = true, dataType = "java.lang.Boolean")
	@NotNull
	private Boolean isActive;

}
