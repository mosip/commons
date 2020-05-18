package io.mosip.kernel.syncdata.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.mosip.kernel.syncdata.dto.MultiLingualLabel;
import io.mosip.kernel.syncdata.dto.SchemaDto;
import io.mosip.kernel.syncdata.dto.ValidatorDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SchemaDto {
	
	@NotBlank
	@Pattern(regexp = "^[a-zA-Z]+$")
	@Size(min = 2, max = 20)
	private String id;
	
	private String description;
	
	@NotEmpty
	private List<MultiLingualLabel> label;
	
	@NotBlank
	private String type;
	
	private int minimum;
	private int maximum;
	
	@NotBlank
	private String controlType;
	
	@NotBlank
	private String fieldType;
	private String format;
	
	@NotBlank
	private String fieldCategory;
	private boolean inputRequired;
	private boolean isRequired;	
	private List<ValidatorDto> validators;
	private List<String> bioAttributes;
	private String requiredOn;
	private String subType;
	private String contactType;
	
	@EqualsAndHashCode.Include
	public String caseIgnoredId() {
		return this.id.toLowerCase();
	}
	
	public String getSubType() {
		return this.subType == null ? "none" : this.subType; 
	}
}

@Data
class ValidatorDto {
	
	@NotBlank
	private String type;
	
	@NotBlank
	private String validator;
	
	private List<String> arguments;
}

@Data
class MultiLingualLabel {
	private String value;
	private String language;
}