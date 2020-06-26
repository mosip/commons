package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Uday Kumar
 * @since 1.0.0
 */
@Data
//@ApiModel(value = "BiometricAttribute", description = "BiometricAttribute resource representation")
public class BiometricAttributeDto {
	/**
	 * Field for code
	 */
	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "code", required = true, dataType = "java.lang.String")
	private String code;
	/**
	 * Field for name
	 */

	@NotNull
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;

	/**
	 * Field for description
	 */
	@Size(min = 0, max = 128)
	@ApiModelProperty(value = "Biometric Attribute desc", required = false, dataType = "java.lang.String")
	private String description;
	/**
	 * Field for biometricTypecode
	 */
	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "Biometric Type code", required = true, dataType = "java.lang.String")
	private String biometricTypeCode;
	/**
	 * Field for language code
	 */
	@ValidLangCode(message = "Language Code is Invalid")
	@ApiModelProperty(value = "Language Code", required = true, dataType = "java.lang.String")
	private String langCode;
	/**
	 * Field for the status of data.
	 */
	@NotNull
	@ApiModelProperty(value = "BiometricAttribute isActive status", required = true, dataType = "java.lang.Boolean")
	private Boolean isActive;

}
