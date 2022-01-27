# Kernel Applicanttype Api

## Overview
This api provides core functions related to applicant type.

## Technical features
- Return a applicant type based on configurations and  given attributes and values.

## Usage
1. To use this api, add this to dependency list:

```
		<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-applicanttype-api</artifactId>
			<version>1.2.0</version>
		</dependency>
```

2. Inputs to be provided:

We need to provide the Map<String,Object> and the key, value pairs are as follows :
```
individualTypeCode: mandatory
dateofbirth: mandatory
genderCode: mandatory
biometricAvailable: optional
```

Valid values for above keys are as follows : 
```
individualTypeCode: FR,NFR
dateofbirth: must be in this pattern yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
genderCode: MLE,FLE
biometricAvailable: true,false
```

3. Exceptions to be handled while using this functionality

- InvalidApplicantArgumentException ("KER-MSD-147", "Invalid query passed for applicant type")
- InvalidApplicantArgumentException ("KER-MSD-148", "Date string can not be parsed");

4. Usage Sample
 
 ```
@Autowired
	private ApplicantType applicantType;
	
	String applicantType=applicantType.getApplicantType(mapOfAttributesAndValues);

 ```
