package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * DTO class for fetching titles from masterdata
 * 
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
@Data

public class TitleDto {

	@NotBlank
	@Size(min = 1, max = 16)
	private String code;

	@NotBlank
	@Size(min = 1, max = 64)
	private String titleName;

	
	@Size(min = 0, max = 128)
	private String titleDescription;

	@NotNull
	@ApiModelProperty(value = "Application isActive Status", required = true, dataType = "java.lang.Boolean")
	private Boolean isActive;

	@NotBlank
	@ValidLangCode(message = "Language Code is Invalid")
	private String langCode;

}
