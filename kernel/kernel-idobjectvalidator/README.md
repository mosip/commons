# Kernel Idobjectvalidator

## Overview
This api provides funtions related to valition of Id Object.

## Usage

1. Maven Dependency

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idobjectvalidator</artifactId>
			<version>${project.version}</version>
	</dependency>

```

2.  Sample Usage

Example1:-

```
		@Autowired
		@Qualifier("composite")
		IdObjectValidator idValidator;
		
 idValidator.validateIdObject(identityObject);    // true or false

```
