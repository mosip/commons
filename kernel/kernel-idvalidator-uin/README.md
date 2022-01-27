# Kernel Idvalidator UIN

## Overview
This library provides functions related to valition of UIN.

## Usage

1. Maven dependency
 
```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idvalidator-uin</artifactId>
			<version>${project.version}</version>
		</dependency>

```

2. Usage sample

Autowired interface IdValidator

```
	@Autowired
	private UinValidator<String> uinValidatorImpl;
```

Call the method validate Id

Valid UIN example:
 
```
	boolean isValid = uinValidatorImpl.validateId("426789089018"); //return true
	
```

Invalid UIN Example

```
	boolean isValid = uinValidatorImpl.validateId("026789089018"); //throw Exception "UIN should not contain Zero or One as first digit."

 
```
