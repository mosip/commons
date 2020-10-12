package io.mosip.kernel.masterdata.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Range;

import io.mosip.kernel.masterdata.validator.StringFormatter;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Response Dto for Template details
 * 
 * @author Neha
 * @since 1.0.0
 */
@Data
//@ApiModel(value = "Template", description = "Template resource representation")
public class TemplateDto {

	
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "id", required = true, dataType = "java.lang.String")
	private String id;

	@NotNull
	@StringFormatter(min = 1, max = 128)
	@ApiModelProperty(value = "name", required = true, dataType = "java.lang.String")
	private String name;

	@Size(min = 0, max = 256)
	@ApiModelProperty(value = "Template description", required = false, dataType = "java.lang.String")
	private String description;

	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "Template file format code", required = true, dataType = "java.lang.String")
	private String fileFormatCode;

	@Size(min = 0, max = 128)
	@ApiModelProperty(value = "model", required = false, dataType = "java.lang.String")
	private String model;

	@Size(min = 0, max = 4086)
	@ApiModelProperty(value = "file text", required = false, dataType = "java.lang.String")
	private String fileText;

	@Size(min = 0, max = 36)
	@ApiModelProperty(value = "module Id", required = false, dataType = "java.lang.String")
	private String moduleId;

	@Size(min = 0, max = 128)
	@ApiModelProperty(value = "module name", required = false, dataType = "java.lang.String")
	private String moduleName;

	@NotNull
	@StringFormatter(min = 1, max = 36)
	@ApiModelProperty(value = "Template file format code", required = true, dataType = "java.lang.String")
	private String templateTypeCode;


	@ValidLangCode(message = "Language Code is Invalid")
	@ApiModelProperty(value = "Language code", required = true, dataType = "java.lang.String")
	private String langCode;

	@NotNull
	@ApiModelProperty(value = "Template isActive status", required = true, dataType = "java.lang.Boolean")
	private Boolean isActive;
}
