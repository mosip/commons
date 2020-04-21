package io.mosip.kernel.syncdata.dto;

import java.util.List;

import lombok.Data;

@Data
public class SchemaDto {
	
	
	private String fieldName;	
	private String description;	
	private String labelName;
	private String type;	
	private int minimum;
	private int maximum;
	private String controlType;
	private String fieldType;
	private String format;
	private String fieldCategory;
	private boolean inputRequired;
	private boolean isRequired;	
	private List<ValidatorDto> validators;
	private List<String> biometricTypes;
}

@Data
class ValidatorDto {
	
	private String type;
	private String validator;
	private List<String> arguments;
}
