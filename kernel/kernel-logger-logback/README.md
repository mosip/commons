# Kernel Logger Logback

## Overview
This library provides basic functions related to logging in MOSIP. All the modules uses and follow method and rules created by this api for logging in MOSIP.

## Usage 

1. Maven dependency
  
```
    <dependency>
		<groupId>io.mosip.kernel</groupId>
		<artifactId>kernel-logger-logback</artifactId>
		<version>${project.version}</</version>
	</dependency>
```

2. Exceptions to be handled while using this functionality:**
1. ClassNameNotFoundException
2. EmptyPatternException
3. FileNameNotProvided
4. ImplementationNotFound
5. XMLConfigurationParseException
6. PatternSyntaxException
7. IllegalStateException
8. IllegalArgumentException

3.Usage Samples

- Usage 1:

1. Create an appender's object and provide configuration 
2. Pass that object and class name in *Logfactory* to get logger instance.
 
```
RollingFileAppender rollingFileAppender = new RollingFileAppender();
       rollingFileAppender.setAppenderName("kernelrollingfileappender");
		rollingFileAppender.setAppend(true);
		rollingFileAppender.setFileName("/kernel-logs.log");
		rollingFileAppender.setImmediateFlush(true);
		rollingFileAppender.setPrudent(false);
		rollingFileAppender.setFileNamePattern("/kernel-logs-%d{ss}-%i.log");
		rollingFileAppender.setMaxHistory(5);
		rollingFileAppender.setTotalCap("100KB");
		rollingFileAppender.setMaxFileSize("10kb");
		
Logger logger=Logfactory.getDefaultRollingFileLogger(rollingFileAppender, Kernel.class);
       
       logger.error(sessionId,idType,id,description);
       logger.debug(sessionId,idType,id,description);
       logger.warn(sessionId,idType,id,description);
       logger.info(sessionId,idType,id,description);
       logger.trace(sessionId,idType,id,description); 		

```
 
Response
 
```
2018-11-23T17:20:05+05:30 - [Kernel] - ERROR  - sessionid - idType - id - description
2018-11-23T17:20:05+05:30 - [Kernel] - INFO - sessionid - idType - id - description
2018-11-23T17:20:05+05:30 - [Kernel] - WARN - sessionid - idType - id - description
2018-11-23T17:20:05+05:30 - [Kernel] - DEBUG - sessionid - idType - id - description
2018-11-23T17:20:05+05:30 - [Kernel] - TRACE  - sessionid - idType - id - description
```

- Usage 2:
 
1. Create an XML file and provide configuration 
2. Pass that file and class name in *Logfactory* to get logger instance. 
 
```
 <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<rollingFileAppender
	appenderName="fileappenderRollingFile">
	<append>true</append>
	<fileName>/kernel-logs</fileName>
	<immediateFlush>true</immediateFlush>
	<prudent>false</prudent>
	<fileNamePattern>/kernel-logs-%d{ss}-%i.log</fileNamePattern>
	<maxFileSize>1kb</maxFileSize>
	<maxHistory>3</maxHistory>
	<totalCap>10mb</totalCap>
</rollingFileAppender>
```
 

 
```
Logger logger= Logfactory.getDefaultRollingFileLogger(rollingFileAppenderXMLFile,Kernel.class); 
       logger.error(sessionId,idType,id,description);
       logger.debug(sessionId,idType,id,description);
       logger.warn(sessionId,idType,id,description);
       logger.info(sessionId,idType,id,description);
       logger.trace(sessionId,idType,id,description); 		
    
```
 
Response
 
 ```
2018-11-23T17:20:05+05:30 - [Kernel] - ERROR  - sessionid - idType - id - description
2018-11-23T17:20:05+05:30 - [Kernel] - INFO - sessionid - idType - id - description
2018-11-23T17:20:05+05:30 - [Kernel] - WARN - sessionid - idType - id - description
2018-11-23T17:20:05+05:30 - [Kernel] - DEBUG - sessionid - idType - id - description
2018-11-23T17:20:05+05:30 - [Kernel] - TRACE  - sessionid - idType - id - description
 ```

Usage 3: To set a particular log level to a logger

1. Create an appender's object and provide configuration with log level 
2. Pass that object and class name in *Logfactory* to get logger instance.
 
```
RollingFileAppender rollingFileAppender = new RollingFileAppender();
       rollingFileAppender.setAppenderName("kernelrollingfileappender");
		rollingFileAppender.setAppend(true);
		rollingFileAppender.setFileName("/kernel-logs.log");
		rollingFileAppender.setImmediateFlush(true);
		rollingFileAppender.setPrudent(false);
		rollingFileAppender.setFileNamePattern("/kernel-logs-%d{ss}-%i.log");
		rollingFileAppender.setMaxHistory(5);
		rollingFileAppender.setTotalCap("100KB");
		rollingFileAppender.setMaxFileSize("10kb");
		
Logger logger=Logfactory.getDefaultRollingFileLogger(rollingFileAppender, Kernel.class,LogLevel.INFO);
       
       logger.error(sessionId,idType,id,description);
       logger.debug(sessionId,idType,id,description);
       logger.warn(sessionId,idType,id,description);
       logger.info(sessionId,idType,id,description);
       logger.trace(sessionId,idType,id,description); 		
 
```
 
Response
 
```
2018-11-23T17:20:05+05:30 - [Kernel] - ERROR  - sessionid - idType - id - description
2018-11-23T17:20:05+05:30 - [Kernel] - INFO - sessionid - idType - id - description
```

