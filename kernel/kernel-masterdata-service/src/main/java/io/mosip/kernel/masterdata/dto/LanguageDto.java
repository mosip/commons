package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Data transfer object class for Language.
 * 
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
@Data

//@ApiModel(value = "Language", description = "Language resource representation")
public class LanguageDto {

	/**
	 * Field for language code
	 */
	// @NotBlank
	@ValidLangCode(message = "Language Code is Invalid")
	// @Size(min = 1, max = 3)
	@ApiModelProperty(value = "Language Code", required = true, dataType = "java.lang.String")
	private String code;

	/**
	 * Field for language name
	 */
	@NotBlank
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "Language Name", required = true, dataType = "java.lang.String")
	private String name;

	/**
	 * Field for language family
	 */
	@Size(min = 0, max = 64)
	@ApiModelProperty(value = "Language Family", dataType = "java.lang.String")
	private String family;

	/**
	 * Field for language native name
	 */
	@Size(min = 0, max = 64)
	@ApiModelProperty(value = "Language Native Name", dataType = "java.lang.String")
	private String nativeName;

	/**
	 * Field for the status of data.
	 */
	@NotNull
	@ApiModelProperty(value = "Language isActive status", required = true, dataType = "java.lang.Boolean")
	private Boolean isActive;

}
