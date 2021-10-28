package io.mosip.kernel.websub.api.test.publisher;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.websub.api.client.PublisherClientImpl;
import io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.test.WebClientApiTestBootApplication;

@SpringBootTest(classes = { WebClientApiTestBootApplication.class })
@RunWith(SpringRunner.class)
public class PublisherClientApiExceptionsTest {
	
	@MockBean
	private RestTemplateHelper restTemplateHelper;
	
	private RestTemplate restTemplate;
	
	 private MockRestServiceServer mockServer;
	 
	 @Autowired
	 private PublisherClientImpl<String> publisherClientImpl;
	
	@Before
	public void init() {
		restTemplate = new RestTemplate();
		when(restTemplateHelper.getRestTemplate()).thenReturn(restTemplate);
		mockServer = MockRestServiceServer.createServer(restTemplate);
		
		
	}


	@Test(expected = WebSubClientException.class)
	public void registerKafkaHubExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.OK)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body("hub.mode=denied")
		        );    
		publisherClientImpl.registerTopic("demo", "http://localhost:9191/hub");
	}
	
	@Test(expected = WebSubClientException.class)
	public void registerHubExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.BAD_REQUEST)
		          .contentType(MediaType.APPLICATION_JSON)
		        );    
		publisherClientImpl.registerTopic("demo", "http://localhost:9191/hub");
	}
	
	@Test(expected = WebSubClientException.class)
	public void registerHubNullRespExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.NO_CONTENT)
		        );    
		publisherClientImpl.registerTopic("demo", "http://localhost:9191/hub");
	}
	
	@Test(expected = WebSubClientException.class)
	public void unRegisterKafkaHubExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.OK)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body("hub.mode=denied")
		        );    
		publisherClientImpl.unregisterTopic("demo", "http://localhost:9191/hub");
	}
	
	@Test(expected = WebSubClientException.class)
	public void unRegisterHubExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.BAD_REQUEST)
		          .contentType(MediaType.APPLICATION_JSON)
		        );    
		publisherClientImpl.unregisterTopic("demo", "http://localhost:9191/hub");
	}
	
	@Test(expected = WebSubClientException.class)
	public void unRegisterHubNullRespExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.NO_CONTENT)
		        );    
		publisherClientImpl.unregisterTopic("demo", "http://localhost:9191/hub");
	}
	
	
	@Test(expected = WebSubClientException.class)
	public void publishKafkaHubExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.OK)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body("hub.mode=denied")
		        );
		 publisherClientImpl.publishUpdate("demo", "{\r\n \"data\": \"1#2021-08-10T05:47:26.853Z\" \r\n}", MediaType.APPLICATION_JSON_UTF8_VALUE, null, "http://localhost:9191/hub");
	}
	
	@Test(expected = WebSubClientException.class)
	public void publishHubExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.BAD_REQUEST)
		          .contentType(MediaType.APPLICATION_JSON)
		        );    
		 publisherClientImpl.publishUpdate("demo", "{\r\n \"data\": \"1#2021-08-10T05:47:26.853Z\" \r\n}", MediaType.APPLICATION_JSON_UTF8_VALUE, null, "http://localhost:9191/hub");
	}
	
	@Test(expected = WebSubClientException.class)
	public void publishHubNullRespExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.NO_CONTENT)
		        );    
		 publisherClientImpl.publishUpdate("demo", "{\r\n \"data\": \"1#2021-08-10T05:47:26.853Z\" \r\n}", MediaType.APPLICATION_JSON_UTF8_VALUE, null, "http://localhost:9191/hub");
	}
	
	
	@Test(expected = WebSubClientException.class)
	public void notifyUpdateKafkaHubExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.OK)
		          .contentType(MediaType.APPLICATION_JSON)
		          .body("hub.mode=denied")
		        );    
		publisherClientImpl.notifyUpdate("demo", null,"http://localhost:9191/hub");
	}
	
	@Test(expected = WebSubClientException.class)
	public void notifyUpdateHubExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.BAD_REQUEST)
		          .contentType(MediaType.APPLICATION_JSON)
		        );    
		publisherClientImpl.notifyUpdate("demo",null, "http://localhost:9191/hub");
	}
	
	@Test(expected = WebSubClientException.class)
	public void notifyUpdateHubNullRespExceptionTest() throws URISyntaxException {
		 mockServer.expect(ExpectedCount.once(), 
		          requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
		          .andExpect(method(HttpMethod.POST))
		          .andRespond(withStatus(HttpStatus.NO_CONTENT)
		        );    
		publisherClientImpl.notifyUpdate("demo",null, "http://localhost:9191/hub");
	}
}
