# Kernel Pinvalidator

## Overview
This api contains functions related to PIN validation.

1. Maven Dependency 
 
 ```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-pinvalidator</artifactId>
			<version>${project.version}</version>
		</dependency>

 ```
 
2. Autowired interface PinValidator 

```
   @Autowired
	private PinValidator<String> PinValidatorImpl;

```

  Call the method to validate Id

  Valid PRID Example:
 
```
	boolean isValid = PinValidatorImpl.validatePin("537180"); //return true
	
```
 
3. Invalid PRID Example:
 
```
 	boolean isValid =  PinValidatorImpl.validatePin("53C18A"); //Throws Exception "Static PIN length must be numeric."
 	boolean isValid = PinValidatorImpl.validatePin("5334"); //Throws Exception " Static PIN length Must be {length}."
 	
```
