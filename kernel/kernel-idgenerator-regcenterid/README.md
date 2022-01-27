# Kernel Idgenerator RegcenterID

## Overview
This api provides funtions related to generation of RegcenterID.

## Usage

1. Maven Dependency

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idgenerator-regcenterid</artifactId>
			<version>${project.version}</version>
	</dependency>

```

2. Autowire interface RegistrationCenterIdGenerator and call the method generateRegistrationCenterId().

For example-

```
@Autowired
RegistrationCenterIdGenerator <String> registrationCenterIdGenerator;

String regCenterId = registrationCenterIdGenerator.generateRegistrationCenterId();

```
 
Response

```
GENERATED RCID = 1000
``` 