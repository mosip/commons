# Kernel Idgenerator PRID

## Overview
This library provides funtions related to generation of PRID.

## Description
Logic behind generating PRID
1. The PRID should not be generated sequentially.
2. Cannot not have repeated numbers, cannot contain any repeating numbers for configured number of digit or more than configured number of digits in property file.
3. Cannot have repeated block of numbers for configured number of digits in property file.
4. Cannot contain any sequential number for configured number of digits or more than configured number of digits in property file and cannot contain alphanumeric values.
5. The last digit of the generated id should be reserved for checksum.
6. The number should not contain '0' or '1' as the first digit.

## Usage
 
1. Maven dependency

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idgenerator-prid</artifactId>
			<version>${project.version}</version>
	</dependency>

```

2. Sample usage

```  
      //Autowire the interface class PridGenerator
	  @Autowired
	  private PridGenerator<String> pridGeneratorImpl;
	
     //Call generateId from autowired PridGenerator instance to generateId.
	  String generatedPrid = pridGeneratorImpl.generateId());
```	  
	  
Response

```	  
Generated Prid: 58361782748604
```	
   
   








