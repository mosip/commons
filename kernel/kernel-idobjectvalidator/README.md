# Kernel Idobjectvalidator

## Overview
This library provides funtions related to valition of Id Object.

## Usage
1. Maven dependency

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idobjectvalidator</artifactId>
			<version>${project.version}</version>
	</dependency>

```

2.  Sample usage

Example:

```
		@Autowired
		@Qualifier("composite")
		IdObjectValidator idValidator;
		
 idValidator.validateIdObject(identityObject);    // true or false

```
