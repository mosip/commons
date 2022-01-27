# Kernel Licensekeygenerator MISP

## Overview
This API provides funtions related to generation of licensekey.

## Usage
1. Dependency 

```
	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-licensekeygenerator-misp</artifactId>
			<version>${project.version}</version>
	</dependency>

```
  
2. Sample usage
  
```
	  Autowire the interface MISPLicenseGenerator
	  @Autowired
	  private MISPLicenseGenerator<String> mispLicenseGenerator;
```


```
	  Call generateLicense() from autowired mispLicenseGenerator instance to generate license key.     
	  String generatedLicense = mispLicenseGenerator.generateLicense());
```
	  
Response:

```
 Generated License: u7y6thye
 
```   
   








