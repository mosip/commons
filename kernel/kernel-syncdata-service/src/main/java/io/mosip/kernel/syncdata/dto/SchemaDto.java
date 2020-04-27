package io.mosip.kernel.syncdata.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class SchemaDto {
	
	@NotBlank
	@Pattern(regexp = "^[a-zA-Z]+$")
	@Size(min = 2, max = 20)
	private String id;
	
	private String description;
	
	@NotBlank
	private String labelName;
	
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
}

@Data
class ValidatorDto {
	
	@NotBlank
	private String type;
	
	@NotBlank
	private String validator;
	private List<String> arguments;
}
