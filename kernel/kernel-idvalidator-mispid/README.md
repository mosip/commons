# Kernel Idobjectvalidator

## Overview
This api provides funtions related to valition of MISP ID.

## Usage 
 
1. Maven Dependency:
 
 ```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idvalidator-mispid</artifactId>
			<version>${project.version}</version>
	</dependency>

 ```
 
2. Usage Sample:

Autowired interface IdValidator and call the method validateId(Id)

 Valid MISPID  Example:
 
 ```
	@Autowired
	private IdValidator<String> mispIdValidatorImpl;
	
	boolean isValid = mispIdValidatorImpl.validateId("100"); //return true
	
```




 






