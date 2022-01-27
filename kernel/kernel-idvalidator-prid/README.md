# Kernel Idvalidator PRID

## Overview
This library provides funtions related to valition of PRID.

## Usage
 
1.Maven dependency
 
 ```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idvalidator-prid</artifactId>
			<version>${project.version}</version>
		</dependency>

 ```
 
2. Usage sample

Autowired interface PridValidator 

```
   @Autowired
	private PridValidator<String> pridValidatorImpl;

```
  Call the method to validate Id

  Valid PRID Example:
 
```
	boolean return = pridValidatorImpl.validateId("537184361359820"); //return true
	
	String id="537184361359820";
	
	int pridLength=14;
	
	int sequenceLimit=3;
	
	int repeatingLimit=3;
	
	int blockLimit=2;
	
	boolean return = pridValidatorImpl.validateId(id,pridLength,sequenceLimit,repeatingLimit,blockLimit)//return true

```
 
  Invalid PRID Example:
 
```
 	boolean isValid = pridValidatorImpl.validateId("037184361359820"); //Throws Exception "PRID should not contain Zero or One as first digit."
 	
```
