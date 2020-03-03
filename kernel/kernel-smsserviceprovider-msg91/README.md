## kernel-smsserviceprovider-msg91

 [Background & Design]()
 

 [API Documentation ]
 
 ```
 mvn javadoc:javadoc

 ```
 
**Properties to be added in Spring application environment using this component**

[application-dev.properties](../../config/application-dev.properties)

 ```
 #-----------------------------VID Properties--------------------------------------
mosip.kernel.sms.enabled=true
mosip.kernel.sms.country.code=91
mosip.kernel.sms.number.length=10


#----------msg91 gateway---------------
mosip.kernel.sms.api=http://api.msg91.com/api/v2/sendsms
mosip.kernel.sms.authkey=<authkey>
mosip.kernel.sms.route=4
mosip.kernel.sms.sender=MOSMSG

auth.server.admin.validate.url=<auth server validate url>

 ```
 
 **Maven Dependency**
 
 ```
 	<dependency>
			<groupId>io.mosip.kernel</groupId>
			<artifactId>kernel-smsserviceprovider-msg91</artifactId>
			<version>${version}</version>
		</dependency>

 ```
 



**Usage Sample:**

Autowired interface 

```
	@Autowired
	private VidValidator<String> vidValidatorImpl;
```
Call the method 

Example:
 
 ```
	 smsServiceProvider.sendSms(contactNumber, contentMessage);

```
	

 
