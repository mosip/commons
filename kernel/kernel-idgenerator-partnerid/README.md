# Kernel Idgenerator PartnerID

## Overview
This api provides funtions related to generation of Partner ID.

## Usage

1. Maven Dependency

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idgenerator-partnerid</artifactId>
			<version>${project.version}</version>
	</dependency>

```

2. Autowire interface PartnerIdGenerator and call the method generateId().

For example-

```
@Autowired
PartnerIdGenerator<String> service;;

String partnerId = service.generateId();

```
 
Response

```
GENERATED PARTNERID = 1000
``` 