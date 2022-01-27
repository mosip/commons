# Kernel Idgenerator Machineid

## Overview
This api provides funtions related to generation of Machine ID.

## Usage

1. Maven Dependency

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idgenerator-machineid</artifactId>
			<version>${project.version}</version>
	</dependency>

```

2. Autowire interface MachineIdGenerator and call the method generateMachineId().

For example-

```
@Autowired
MachineIdGenerator <String> machineIdGenerator;

String machineId = machineIdGenerator.generateMachineId();

```
 
Response

```
GENERATED MID = 1000
```