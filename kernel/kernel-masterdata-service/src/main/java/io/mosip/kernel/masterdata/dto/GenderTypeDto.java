package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GenderType Dto for request
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing a Request")
public class GenderTypeDto {

	
	@NotNull
	@StringFormatter(min = 1, max = 16)
	@ApiModelProperty(notes = "Code of gender ", example = "GC001", required = true)
	private String code;



	@NotNull
	@StringFormatter(min = 1, max = 64)
	@ApiModelProperty(notes = "Name of the Gender", example = "Male", required = true)
	private String genderName;


	@ApiModelProperty(notes = "Language Code", example = "ENG", required = true)
    @ValidLangCode(message = "Language Code is Invalid")
    private String langCode;

	@ApiModelProperty(notes = "Row is active or not", required = true)
	@NotNull
	private Boolean isActive;

}
