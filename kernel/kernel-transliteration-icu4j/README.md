# Kernel Transliteration Icu4j

## Overview
This api providesbasic funtion for transliteration. This implementation is a wrapper on icu4j api.

## Usage  
 
1. Maven Dependency
 
 ```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-transliteration-icutext</artifactId>
			<version>${project.version}</version>
		</dependency>

 ```
 
2. Usage Sample:

- Autowired interface Transliteration

```
	@Autowired
	private Transliteration<String> transliterateImpl;
	
```

Call the method transliterate

Valid transliteration Example:

```
		String frenchToArabic = transliterateImpl.transliterate("fra","ara", "Bienvenue");
		
		System.out.println("ARABIC="+frenchToArabic);
 
 ```
 
Output:ARABIC= بِِنڤِنُِ
 
- Invalid transliteration Example:
 
 ```
	transliterateImpl.transliterate("dnjksd", "ara", "Bienvenue");
	
 ```
 
Output: 
 
 InvalidTransliterationException:
 
 Language code not supported








