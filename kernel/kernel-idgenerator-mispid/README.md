# Kernel Idgenerator MispID

## Overview
This api provides funtions related to generation of Misp ID.

## Usage

1. Maven Dependency

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idgenerator-mispid</artifactId>
			<version>${project.version}</version>
	</dependency>

```

2. Autowire interface TspIdGenerator and call the method generateId().

For example-

```
@Autowired
TspIdGenerator <String> mispIdGenerator;

String mispId = mispIdGenerator.generateId();

```
 
Response

```
GENERATED MISPID = 100
``` 