package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * DTO class for document type.
 * 
 * @author Uday Kumar
 * @author Ritesh Sinha
 * @author Neha Sinha
 * 
 * @since 1.0.0
 *
 */
@Data
//@ApiModel(value = "DocumentCType", description = "DocumentType resource representation")
public class DocumentTypeDto {

//	@NotBlank
	@Size(max = 36)
//	@ApiModelProperty(value = "code", required = true, dataType = "java.lang.String")
	private String code;

	@NotBlank
	@Size(min = 1, max = 64)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;

	@Size(max = 128)
	@ApiModelProperty(value = "Application description", required = false, dataType = "java.lang.String")
	private String description;

	// @NotBlank
	// @Size(min = 1, max = 3)
	@ValidLangCode(message = "Language Code is Invalid")
	@ApiModelProperty(value = "Language Code", required = true, dataType = "java.lang.String")
	private String langCode;

	@NotNull
	@ApiModelProperty(value = "Application isActive Status", required = true, dataType = "java.lang.Boolean")
	private Boolean isActive;

}
