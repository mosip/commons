# Kernel Idvalidator UIN

## Overview
This api provides funtions related to valition of UIN.

## Usage

1. Maven Dependency
 
```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idvalidator-uin</artifactId>
			<version>${project.version}</version>
		</dependency>

```

2. Usage Sample:

Autowired interface IdValidator

```
	@Autowired
	private UinValidator<String> uinValidatorImpl;
```

Call the method validate Id

 
Valid UIN  Example:
 
```
	boolean isValid = uinValidatorImpl.validateId("426789089018"); //return true
	
```

Invalid UIN Example

```
	boolean isValid = uinValidatorImpl.validateId("026789089018"); //throw Exception "UIN should not contain Zero or One as first digit."

 
```
