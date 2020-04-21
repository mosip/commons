package io.mosip.kernel.masterdata.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SchemaDto {
	
	@NotNull
	private String fieldName;
	
	private String description;
	
	@NotNull
	private String labelName;
	
	@NotNull
	private String type;
	
	private int minimum;
	private int maximum;
	
	@NotNull
	private String controlType;
	
	@NotNull
	private String fieldType;
	
	private String format;
	
	@NotNull
	private String fieldCategory;
	
	private boolean inputRequired;
	private boolean isRequired;	
	private List<ValidatorDto> validators;
	private List<String> bioAttributes;
}

@Data
class ValidatorDto {
	
	@NotNull
	private String type;
	
	@NotNull
	private String validator;
	
	private List<String> arguments;
}
