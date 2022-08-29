# Kernel Idvalidator VID

## Overview
This library provides functions related to valition of VID.

## Usage
1. Maven dependency
 
```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idvalidator-vid</artifactId>
			<version>${project.version}</version>
		</dependency>

```
 
2. Usage sample

Autowired interface IdValidator

```
	@Autowired
	private VidValidator<String> vidValidatorImpl;
```

Call the method to validate Id

Valid VID example:
 
```
	boolean return = vidValidatorImpl.validateId("537184361359820"); //return true

```
	
Invalid VID Example:
	
```
	boolean isValid = vidValidatorImpl.validateId("037184361359820"); //Throws Exception "VID should not contain Zero or One as first digit."
 
```


 
