# Kernel Idgenerator RID

## Overview
This api provides funtions related to generation of RID.

## Usage

1. Maven Dependency

```
	<dependencies>
		<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idgenerator-rid</artifactId>
			<version>${project.version}</version>
	</dependency>

```
   
2.Autowired interface RidGenerator and call the method generateId(centerId,machineId).

For example-

```
@Autowired
RidGenerator <String> ridGeneratorImpl;

String rid = ridGeneratorImpl.generateId("34532","67897");

```

Response

```
GENERATED RID = 34532678970000120181122173040
``` 




