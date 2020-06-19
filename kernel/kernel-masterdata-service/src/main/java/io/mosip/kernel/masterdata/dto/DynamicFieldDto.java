package io.mosip.kernel.masterdata.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 
 * @author anusha
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Model representing a dynamic field Request")
public class DynamicFieldDto {
	
	@ApiModelProperty(notes = "Field name", example = "any name", required = true)
	@NotBlank
	@Pattern(regexp = "^[a-zA-Z]+$")
	@Size(min = 2, max = 20)
	private String name;
	
	@ApiModelProperty(notes = "Language Code", example = "eng", required = true)
	@ValidLangCode(message = "Language Code is Invalid")
	private String langCode;
	
	@NotBlank
	@ApiModelProperty(notes = "Data Type", example = "string", required = true)
	@Size(min = 3, max = 20)
	private String dataType;
	
	@NotBlank
	private String description;
	
	private List<DynamicFieldValueDto> fieldVal;
	
	private boolean isActive;
}
