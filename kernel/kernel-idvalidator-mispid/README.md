# Kernel Idvalidator MISP ID

## Overview
This library provides funtions related to valition of MISP ID.

## Usage 
 
1. Maven dependency:
 
 ```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idvalidator-mispid</artifactId>
			<version>${project.version}</version>
	</dependency>

 ```
 
2. Usage sample

Autowired interface IdValidator and call the method validateId(Id)

Valid MISP ID  Example:
 
 ```
	@Autowired
	private IdValidator<String> mispIdValidatorImpl;
	
	boolean isValid = mispIdValidatorImpl.validateId("100"); //return true
	
```




 






