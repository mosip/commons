package io.mosip.kernel.websub.api.test.subscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
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
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.core.websub.spi.SubscriptionExtendedClient;
import io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.model.FailedContentRequest;
import io.mosip.kernel.websub.api.model.FailedContentResponse;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.kernel.websub.api.test.WebClientApiTestBootApplication;

@SpringBootTest(classes = { WebClientApiTestBootApplication.class })
@RunWith(SpringRunner.class)
public class SubscriberClientApiTest {

	@MockBean
	private RestTemplateHelper restTemplateHelper;

	private RestTemplate restTemplate;

	private MockRestServiceServer mockServer;

	@Autowired
	private SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscriptionClient;

	@Autowired
	private SubscriptionExtendedClient<FailedContentResponse, FailedContentRequest> subscriptionExtendedClient;

	private SubscriptionChangeRequest subscriptionRequest;
	
	private UnsubscriptionRequest unsubscriptionRequest;

	private FailedContentRequest failedContentRequest;

	private String expectedFailedContentURL = "http://localhost:9191/hub?topic=demo&callback=aHR0cDovL2xvY2FsaG9zdDo4MDgwL2NhbGxiYWNr&timestamp=2021-08-30T12:39:23.511446100Z&pageindex=0&messageCount=10";

	@Autowired
	private ObjectMapper objectMapper;

	@Before
	public void init() {
		restTemplate = new RestTemplate();
		when(restTemplateHelper.getRestTemplate()).thenReturn(restTemplate);
		mockServer = MockRestServiceServer.createServer(restTemplate);
		subscriptionRequest = new SubscriptionChangeRequest();
		subscriptionRequest.setHubURL("http://localhost:9191/hub");
		subscriptionRequest.setTopic("demo");
		subscriptionRequest.setCallbackURL("http://localhost:8080/callback");
		subscriptionRequest.setSecret("OIUAShdasoi");
		subscriptionRequest.setLeaseSeconds(1000);
		unsubscriptionRequest = new UnsubscriptionRequest();
		unsubscriptionRequest.setHubURL("http://localhost:9191/hub");
		unsubscriptionRequest.setTopic("demo");
		unsubscriptionRequest.setCallbackURL("http://localhost:8080/callback");
		failedContentRequest = new FailedContentRequest();
		failedContentRequest.setHubURL("http://localhost:9191/hub");
		failedContentRequest.setSecret("OIUAShdasoi");
		failedContentRequest.setTopic("demo");
		failedContentRequest.setCallbackURL("http://localhost:8080/callback");
		failedContentRequest.setMessageCount(10);
		failedContentRequest.setPaginationIndex(0);
		String timestamp = DateUtils.getUTCCurrentDateTimeString();
		failedContentRequest.setTimestamp(timestamp);
		expectedFailedContentURL = "http://localhost:9191/hub?topic=demo&callback=aHR0cDovL2xvY2FsaG9zdDo4MDgwL2NhbGxiYWNr&timestamp="
				+ timestamp + "&pageindex=0&messageCount=10";
	}

	@Test(expected = WebSubClientException.class)
	public void subscribeKafkaHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=denied"));
		subscriptionClient.subscribe(subscriptionRequest);

	}

	@Test(expected = WebSubClientException.class)
	public void subscribeHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON));
		subscriptionClient.subscribe(subscriptionRequest);
	}


	@Test(expected = WebSubClientException.class)
	public void subscribeHubNoContentRespExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NO_CONTENT));
		subscriptionClient.subscribe(subscriptionRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void subscribeHubNullCallBackExceptionTest() throws URISyntaxException {
		SubscriptionChangeRequest changeRequest = new SubscriptionChangeRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL(null);
		changeRequest.setSecret("OIUAShdasoi");
		changeRequest.setLeaseSeconds(1000);
		subscriptionClient.subscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void subscribeHubNullTopicExceptionTest() throws URISyntaxException {
		SubscriptionChangeRequest changeRequest = new SubscriptionChangeRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic(null);
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		changeRequest.setSecret("OIUAShdasoi");
		changeRequest.setLeaseSeconds(1000);
		subscriptionClient.subscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void subscribeHubNullSecretExceptionTest() throws URISyntaxException {
		SubscriptionChangeRequest changeRequest = new SubscriptionChangeRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		changeRequest.setSecret(null);
		changeRequest.setLeaseSeconds(1000);
		subscriptionClient.subscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void subscribeHubNullHubURLExceptionTest() throws URISyntaxException {
		SubscriptionChangeRequest changeRequest = new SubscriptionChangeRequest();
		changeRequest.setHubURL(null);
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		changeRequest.setSecret("OIUAShdasoi");
		changeRequest.setLeaseSeconds(1000);
		subscriptionClient.subscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void subscribeHubEmptyCallBackExceptionTest() throws URISyntaxException {
		SubscriptionChangeRequest changeRequest = new SubscriptionChangeRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("");
		changeRequest.setSecret("OIUAShdasoi");
		changeRequest.setLeaseSeconds(1000);
		subscriptionClient.subscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void subscribeHubEmptyTopicExceptionTest() throws URISyntaxException {
		SubscriptionChangeRequest changeRequest = new SubscriptionChangeRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		changeRequest.setSecret("OIUAShdasoi");
		changeRequest.setLeaseSeconds(1000);
		subscriptionClient.subscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void subscribeHubEmptySecretExceptionTest() throws URISyntaxException {
		SubscriptionChangeRequest changeRequest = new SubscriptionChangeRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		changeRequest.setSecret("");
		changeRequest.setLeaseSeconds(1000);
		subscriptionClient.subscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void subscribeHubEmptyHubURLExceptionTest() throws URISyntaxException {
		SubscriptionChangeRequest changeRequest = new SubscriptionChangeRequest();
		changeRequest.setHubURL("");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		changeRequest.setSecret("OIUAShdasoi");
		changeRequest.setLeaseSeconds(1000);
		subscriptionClient.subscribe(changeRequest);
	}

	@Test
	public void subscribeOKTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=accepted"));
		SubscriptionChangeResponse subscriptionChangeResponse=subscriptionClient.subscribe(subscriptionRequest);
		assertThat(subscriptionChangeResponse.getTopic(),is("demo"));	
	}
	
	@Test
	public void subscribeAcceptedTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON));
		SubscriptionChangeResponse subscriptionChangeResponse=subscriptionClient.subscribe(subscriptionRequest);
		assertThat(subscriptionChangeResponse.getTopic(),is("demo"));	
	}
	
	
	////////////////////////////
	
	
	@Test(expected = WebSubClientException.class)
	public void unSubscribeKafkaHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=denied"));
		subscriptionClient.unSubscribe(unsubscriptionRequest);

	}

	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON));
		subscriptionClient.unSubscribe(unsubscriptionRequest);
	}


	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubNoContentRespExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NO_CONTENT));
		subscriptionClient.unSubscribe(unsubscriptionRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubNullCallBackExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL(null);
		subscriptionClient.unSubscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubNullTopicExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic(null);
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		subscriptionClient.unSubscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubNullHubURLExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL(null);
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		subscriptionClient.unSubscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubEmptyCallBackExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("");
		subscriptionClient.unSubscribe(changeRequest);
	}
	
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubEmptyTopicExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		subscriptionClient.unSubscribe(changeRequest);
	}
	
	
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubEmptyHubURLExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		subscriptionClient.unSubscribe(changeRequest);
	}

	@Test
	public void unSubscribeOKTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=accepted"));
		SubscriptionChangeResponse subscriptionChangeResponse=subscriptionClient.unSubscribe(unsubscriptionRequest);
		assertThat(subscriptionChangeResponse.getTopic(),is("demo"));	
	}
	
	@Test
	public void unSubscribeAcceptedTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON));
		SubscriptionChangeResponse subscriptionChangeResponse=subscriptionClient.unSubscribe(unsubscriptionRequest);
		assertThat(subscriptionChangeResponse.getTopic(),is("demo"));	
	}


	/*
	 * public void getFailedContentKafkaHubExceptionTest() throws
	 * URISyntaxException, JsonProcessingException { List<Failedcontents>
	 * failedcontents = new ArrayList<>(); Failedcontents failedcontent = new
	 * Failedcontents(); failedcontent.setMessage("msg1");
	 * failedcontent.setTimestamp(DateUtils.getUTCCurrentDateTimeString());
	 * failedcontents.add(failedcontent); FailedContentResponse contentResponse =
	 * new FailedContentResponse();
	 * contentResponse.setFailedcontents(failedcontents); mockServer
	 * .expect(ExpectedCount.once(), requestTo(new URI(expectedFailedContentURL)))
	 * .andExpect(method(HttpMethod.GET)) .andRespond(withStatus(HttpStatus.OK).
	 * contentType(MediaType.APPLICATION_JSON).
	 * body(objectMapper.writeValueAsBytes(contentResponse))); FailedContentResponse
	 * failedContentResponse=subscriptionExtendedClient.getFailedContent(
	 * failedContentRequest);
	 * assertThat(failedContentResponse.getFailedcontents().size(),is(1)); }
	 */

}
