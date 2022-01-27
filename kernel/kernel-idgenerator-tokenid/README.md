# Kernel Idgenerator Token ID

## Overview
This library provides funtions related to generation of Token ID.

## Usage  
1. Maven Dependency

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idgenerator-tokenid</artifactId>
			<version>${project.version}</version>
	</dependency>

```

2. Sample request
 
  ```
//Autowire the interface TokenIdGenerator

  @Autowired
	TokenIdGenerator<String> tokenIdGenerator;

 //Call generateId()
  String tokenId = tokenIdGenerator.generateId();
  
```

Response:

```
Generated tokenId: 526900409300563849276960763148952762
```








