# Kernel Websubclient Api

## Overview
This api provides funtions to connect to websub. For more overview on websub [refer](https://nayakrounak.gitbook.io/mosip-docs/modules/websub)

## Technical functionalities
- Provide funtions for publisher and subscriber as metioned below in usage.
- Automatically creates a intent verification filter for all callbacks
- Automatically takes care of authenticated content distribution.

## Usage

1. Maven dependency
  
 ```
    <dependency>
		<groupId>io.mosip.kernel</groupId>
		<artifactId>kernel-websubclient-api</artifactId>
		<version>${project.version}</version>
	</dependency>
 ```

2 Usage for Publisher:
 
- Register a topic
 
 ```
@Autowired
private PublisherClient<String, DataBody, HttpHeaders> pb; 
	
pb.registerTopic(topic, hubURL);
```
 
- Publish for Update 
 
 ```
@Autowired
private PublisherClient<String, DataBody, HttpHeaders> pb; 
	
pb.publishUpdate(topic, body, MediaType.APPLICATION_JSON_UTF8_VALUE, httpHeaders,  hubURL); 
 ```
 
 
3. Usage for Subscribers:
 
- Subscribe to a topic
 
 Step 1: Create a callback endpoint and annotate it with @PreAuthenticateContentAndVerifyIntent provide value to parameters

```
 @PostMapping(value = "/callback",consumes = "application/json")
@PreAuthenticateContentAndVerifyIntent(secret = "Kslk30SNF2AChs2",callback = "/callback",topic = "http://websubpubtopic.com")
	public void printPost(@RequestBody DataBody body) {
		System.out.println(body.getData());
	}
```
Step 2 : Subscribe
 
```
@Autowired
SubscriptionClient<SubscriptionChangeRequest,UnsubscriptionRequest, SubscriptionChangeResponse> sb; 
		
sb.subscribe(subscriptionRequest);
```
 
 
2. Unsubscribe to a topic(same callback should be used which as at the time of subscribe)
 
```
@Autowired
SubscriptionClient<SubscriptionChangeRequest,UnsubscriptionRequest, SubscriptionChangeResponse> sb; 
		
sb.unsubscribe(unsubscriptionRequest);
```
