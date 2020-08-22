## kernel-websubclient-api

**Api Documentation**

[API Documentation <TBA>](TBA)

```
mvn javadoc:javadoc
```

**Maven dependency**
  
 ```
    <dependency>
		<groupId>io.mosip.kernel</groupId>
		<artifactId>kernel-websubclient-api</artifactId>
		<version>${project.version}</version>
	</dependency>
 ```

**Usage Sample**



  
*Usage for Publisher:*
 
 Step 1 Register a topic
 
 ```
@Autowired
private PublisherClient<String, DataBody, HttpHeaders> pb; 
	
pb.registerTopic(topic, hubURL);
```
 
 Step 2 Publish for Update 
 
 ```
@Autowired
private PublisherClient<String, DataBody, HttpHeaders> pb; 
	
pb.publishUpdate(topic, body, MediaType.APPLICATION_JSON_UTF8_VALUE, httpHeaders,  hubURL); 
 ```
 
 Other Operation
  
1. Unregister a topic
 
 ```
@Autowired
private PublisherClient<String, DataBody, HttpHeaders> pb; 
	
pb.unregisterTopic(topic, hubURL);
```
 2. Notify an update

 ```
@Autowired
private PublisherClient<String, DataBody, HttpHeaders> pb; 
	
pb.notifyUpdate(topic, httpHeaders,  hubURL); 
```
 
 
*Usage for Subscribers:*
 
 1 Subscribe to a topic
 
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
 

NOTE: We should create one callback endpoint per subscriptions