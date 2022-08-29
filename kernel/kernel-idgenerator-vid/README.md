# Kernel Idgenerator VID

## Overview
This library provides funtions related to generation of VID.

## Usage 
1. Maven dependency

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-idgenerator-vid</artifactId>
			<version>${project.version}</version>
	</dependency>

```

2. Sample usage
  
```    
      //Autowire the interface class vidGenerator
	  @Autowired
	  private VidGenerator<String> vidGeneratorImpl;
	
     //Call generateId from autowired vidGenerator instance to generateId.
     
     
	  String generatedVid = vidGeneratorImpl.generateId());
```	  
	 
Response:

```	  
 Generated vid : 5916983045841801  
```  




