# Kernel Idvalidator RID

## Overview
This library provides funtions related to valition of RID.

## Usage

1. Maven dependency
 
 ```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idvalidator-rid</artifactId>
			<version>${project.version}</version>
	</dependency>

 ```
 
2. Usage sample:

Autowired interface RidValidator

```
	@Autowired
	private RidValidator<String> ridValidatorImpl;
```

Call the method validate Id

Valid RID Example:

```
	String centerId = "27847";

	String machineId = "65736";
	
	String rid ="27847657360002520181208183050";
	
	int centerIdLength = 5;
	
	int machineIdLength = 5;
	
	int sequenceLength=5;
	
	int timeStampLength = 14;
	
	boolean return = ridValidatorImpl.validateId(rid,centerId,machineId); //return true
	boolean return = ridValidatorImpl.validateId(rid,centerId,machineId,centerIdLength,machineIdLength,sequenceLength,timeStampLength); //return true
	boolean return = ridValidatorImpl.validateId(rid); //return true
	boolean return = ridValidatorImpl.validateId(rid,,centerIdLength,machineIdLength,sequenceLength,timeStampLength); //return true
 
 ```
 
 
 Invalid RID Example:
 
 ```
	String centerId = "27847";

	String machineId = "65736";
	
	String rid ="27847657360002520181208183070";
	
	boolean return = ridValidatorImpl.validateId(rid,centerId,machineId); //Throws Exception "Invalid Time Stamp Found"
	
 ```







