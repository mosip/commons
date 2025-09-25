package io.mosip.kernel.websub.api.test.subscriber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import io.mosip.kernel.websub.api.verifier.AuthenticatedContentVerifier;
import io.mosip.kernel.websub.api.verifier.IntentVerifier;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.response.DefaultResponseCreator;
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

/**
 * Unit tests for {@link SubscriptionClient} and {@link SubscriptionExtendedClient} to verify WebSub subscriber functionality.
 * <p>
 * This test class validates the behavior of WebSub subscriber operations, including subscription, unsubscription, and failed content retrieval,
 * as per the <a href="https://www.w3.org/TR/websub/">W3C WebSub specification</a>. It uses {@link MockRestServiceServer} to simulate HTTP
 * interactions with a WebSub hub and tests both successful and error scenarios, including input validation. The test context is configured
 * with {@link WebClientApiTestBootApplication} to provide a lightweight Spring environment. Optimizations include:
 * <ul>
 *   <li>Mocking dependencies ({@link RestTemplateHelper}, {@link AuthenticatedContentVerifier}, {@link IntentVerifier}) for isolation.</li>
 *   <li>Using a helper method to reduce repetitive mock setup code.</li>
 *   <li>Testing edge cases like null/empty inputs.</li>
 *   <li>Restoring and optimizing the failed content retrieval test.</li>
 *   <li>Logging setup and test execution for debugging.</li>
 * </ul>
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 * @see SubscriptionClient
 * @see SubscriptionExtendedClient
 * @see WebClientApiTestBootApplication
 */
@SpringBootTest(classes = { WebClientApiTestBootApplication.class })
@RunWith(SpringRunner.class)
public class SubscriberClientApiTest {

	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberClientApiTest.class);

	/**
	 * Mocked helper for providing {@link RestTemplate}.
	 */
	@MockBean
	private RestTemplateHelper restTemplateHelper;

	/**
	 * Mocked content verifier for WebSub payload validation.
	 */
	@MockBean
	private AuthenticatedContentVerifier contentVerifier;

	/**
	 * Mocked intent verifier for WebSub subscribe/unsubscribe validation.
	 */
	@MockBean
	private IntentVerifier intentVerifier;

	private RestTemplate restTemplate;

	private MockRestServiceServer mockServer;

	/**
	 * Subscription client for WebSub operations.
	 */
	@Autowired
	private SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse> subscriptionClient;

	/**
	 * Extended subscription client for failed content retrieval.
	 */
	@Autowired
	private SubscriptionExtendedClient<FailedContentResponse, FailedContentRequest> subscriptionExtendedClient;

	private SubscriptionChangeRequest subscriptionRequest;
	
	private UnsubscriptionRequest unsubscriptionRequest;

	private FailedContentRequest failedContentRequest;

	private String expectedFailedContentURL = "http://localhost:9191/hub?topic=demo&callback=aHR0cDovL2xvY2FsaG9zdDo4MDgwL2NhbGxiYWNr&timestamp=2021-08-30T12:39:23.511446100Z&pageindex=0&messageCount=10";

	/**
	 * Object mapper for JSON serialization/deserialization.
	 */
	@Autowired
	private ObjectMapper objectMapper;

	private static final String UTC_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	@Before
	public void init() {
		LOGGER.debug("Initializing test setup for SubscriberClientApiTest");
		restTemplate = new RestTemplate();
		when(restTemplateHelper.getRestTemplate()).thenReturn(restTemplate);
		when(contentVerifier.verifyAuthorizedContentVerified(any(), any())).thenReturn(true);
		when(intentVerifier.isIntentVerified(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
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
		String timestamp = DateUtils.getUTCCurrentDateTimeString(UTC_DATETIME_PATTERN);
		failedContentRequest.setTimestamp(timestamp);

		expectedFailedContentURL = "http://localhost:9191/hub?topic=demo&callback=aHR0cDovL2xvY2FsaG9zdDo4MDgwL2NhbGxiYWNr&timestamp="
				+ timestamp + "&pageindex=0&messageCount=10";
		LOGGER.debug("Test setup completed with expectedFailedContentURL: {}", expectedFailedContentURL);
	}

	/**
	 * Configures a mock HTTP response for a WebSub hub request.
	 *
	 * @param uri     the expected request URI
	 * @param status  the HTTP status to return
	 * @param hubMode the hub.mode response (e.g., "accepted", "denied")
	 * @return the configured response creator
	 */
	private DefaultResponseCreator mockHubResponse(String uri, HttpStatus status, String hubMode) {
		return withStatus(status)
				.contentType(MediaType.APPLICATION_JSON)
				.body(hubMode != null ? "hub.mode=" + hubMode : "");
	}

	/**
	 * Tests subscription failure with hub.mode=denied.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test(expected = WebSubClientException.class)
	public void subscribeKafkaHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=denied"));
		subscriptionClient.subscribe(subscriptionRequest);
	}

	/**
	 * Tests subscription failure with HTTP BAD_REQUEST.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test(expected = WebSubClientException.class)
	public void subscribeHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON));
		subscriptionClient.subscribe(subscriptionRequest);
	}

	/**
	 * Tests subscription failure with HTTP NO_CONTENT.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test(expected = WebSubClientException.class)
	public void subscribeHubNoContentRespExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NO_CONTENT));
		subscriptionClient.subscribe(subscriptionRequest);
	}

	/**
	 * Tests subscription failure with null callback URL.
	 */
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

	/**
	 * Tests subscription failure with null topic.
	 */
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

	/**
	 * Tests subscription failure with null secret.
	 */
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

	/**
	 * Tests subscription failure with null hub URL.
	 */
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

	/**
	 * Tests subscription failure with empty callback URL.
	 */
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

	/**
	 * Tests subscription failure with empty topic.
	 */
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

	/**
	 * Tests subscription failure with empty secret.
	 */
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

	/**
	 * Tests subscription failure with empty hub URL.
	 */
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

	/**
	 * Tests successful subscription with HTTP OK and hub.mode=accepted.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test
	public void subscribeOKTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=accepted"));
		SubscriptionChangeResponse subscriptionChangeResponse=subscriptionClient.subscribe(subscriptionRequest);
		assertThat(subscriptionChangeResponse.getTopic(),is("demo"));	
	}

	/**
	 * Tests successful subscription with HTTP ACCEPTED.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test
	public void subscribeAcceptedTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.ACCEPTED).contentType(MediaType.APPLICATION_JSON));
		SubscriptionChangeResponse subscriptionChangeResponse=subscriptionClient.subscribe(subscriptionRequest);
		assertThat(subscriptionChangeResponse.getTopic(),is("demo"));	
	}

	/**
	 * Tests successful subscription with DB version client behavior enabled.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test
	public void subscribeOKPreviousVerisonTest() throws URISyntaxException {
		ReflectionTestUtils.setField(subscriptionClient, "isWebsubDbVersionClientBehaviourEnable", true);
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=accepted"));
		SubscriptionChangeResponse subscriptionChangeResponse=subscriptionClient.subscribe(subscriptionRequest);
		assertThat(subscriptionChangeResponse.getTopic(),is("demo"));	
		ReflectionTestUtils.setField(subscriptionClient, "isWebsubDbVersionClientBehaviourEnable", false);
	}

	/**
	 * Tests unsubscription failure with hub.mode=denied.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test(expected = WebSubClientException.class)
	public void unSubscribeKafkaHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=denied"));
		subscriptionClient.unSubscribe(unsubscriptionRequest);
	}

	/**
	 * Tests unsubscription failure with HTTP BAD_REQUEST.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON));
		subscriptionClient.unSubscribe(unsubscriptionRequest);
	}

	/**
	 * Tests unsubscription failure with HTTP NO_CONTENT.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubNoContentRespExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NO_CONTENT));
		subscriptionClient.unSubscribe(unsubscriptionRequest);
	}

	/**
	 * Tests unsubscription failure with null callback URL.
	 */
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubNullCallBackExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL(null);
		subscriptionClient.unSubscribe(changeRequest);
	}

	/**
	 * Tests unsubscription failure with null topic.
	 */
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubNullTopicExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic(null);
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		subscriptionClient.unSubscribe(changeRequest);
	}

	/**
	 * Tests unsubscription failure with null hub URL.
	 */
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubNullHubURLExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL(null);
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		subscriptionClient.unSubscribe(changeRequest);
	}

	/**
	 * Tests unsubscription failure with empty callback URL.
	 */
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubEmptyCallBackExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("");
		subscriptionClient.unSubscribe(changeRequest);
	}

	/**
	 * Tests unsubscription failure with empty topic.
	 */
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubEmptyTopicExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("http://localhost:9191/hub");
		changeRequest.setTopic("");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		subscriptionClient.unSubscribe(changeRequest);
	}

	/**
	 * Tests unsubscription failure with empty hub URL.
	 */
	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubEmptyHubURLExceptionTest() throws URISyntaxException {
		UnsubscriptionRequest changeRequest = new UnsubscriptionRequest();
		changeRequest.setHubURL("");
		changeRequest.setTopic("demo");
		changeRequest.setCallbackURL("http://localhost:8080/callback");
		subscriptionClient.unSubscribe(changeRequest);
	}

	/**
	 * Tests successful unsubscription with HTTP OK and hub.mode=accepted.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test
	public void unSubscribeOKTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=accepted"));
		SubscriptionChangeResponse subscriptionChangeResponse=subscriptionClient.unSubscribe(unsubscriptionRequest);
		assertThat(subscriptionChangeResponse.getTopic(),is("demo"));	
	}

	/**
	 * Tests successful unsubscription with DB version client behavior enabled.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
	@Test
	public void unSubscribeOKPreviousTest() throws URISyntaxException {
		ReflectionTestUtils.setField(subscriptionClient, "isWebsubDbVersionClientBehaviourEnable", true);
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=accepted"));
		SubscriptionChangeResponse subscriptionChangeResponse=subscriptionClient.unSubscribe(unsubscriptionRequest);
		assertThat(subscriptionChangeResponse.getTopic(),is("demo"));
		ReflectionTestUtils.setField(subscriptionClient, "isWebsubDbVersionClientBehaviourEnable", false);
	}

	/**
	 * Tests successful unsubscription with HTTP ACCEPTED.
	 *
	 * @throws URISyntaxException if the hub URL is invalid
	 */
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
	 * failedcontent.setTimestamp(DateUtils.getUTCCurrentDateTimeString(UTC_DATETIME_PATTERN););
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