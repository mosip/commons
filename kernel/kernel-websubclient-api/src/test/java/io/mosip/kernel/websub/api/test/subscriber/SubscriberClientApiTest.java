package io.mosip.kernel.websub.api.test.subscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.core.websub.spi.SubscriptionExtendedClient;
import io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.model.FailedContentRequest;
import io.mosip.kernel.websub.api.model.FailedContentResponse;
import io.mosip.kernel.websub.api.model.FailedContentResponse.Failedcontents;
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

	private FailedContentRequest failedContentRequest;
	
	private String expectedFailedContentURL="http://localhost:9191/hub?topic=demo&callback=aHR0cDovL2xvY2FsaG9zdDo4MDgwL2NhbGxiYWNr&timestamp=2021-08-30T12:39:23.511446100Z&pageindex=0&messageCount=10";
	
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
		failedContentRequest = new FailedContentRequest();
		failedContentRequest.setHubURL("http://localhost:9191/hub");
		failedContentRequest.setSecret("OIUAShdasoi");
		failedContentRequest.setTopic("demo");
		failedContentRequest.setCallbackURL("http://localhost:8080/callback");
		failedContentRequest.setMessageCount(10);
		failedContentRequest.setPaginationIndex(0);
		String timestamp=DateUtils.getUTCCurrentDateTimeString();
		failedContentRequest.setTimestamp(timestamp);
		expectedFailedContentURL="http://localhost:9191/hub?topic=demo&callback=aHR0cDovL2xvY2FsaG9zdDo4MDgwL2NhbGxiYWNr&timestamp="+timestamp+"&pageindex=0&messageCount=10";
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
	public void subscribeHubNullRespExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NO_CONTENT));
		subscriptionClient.subscribe(subscriptionRequest);
	}

	@Test(expected = WebSubClientException.class)
	public void unSubscribeKafkaHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body("hub.mode=denied"));
		subscriptionClient.subscribe(subscriptionRequest);

	}

	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON));
		subscriptionClient.subscribe(subscriptionRequest);
	}

	@Test(expected = WebSubClientException.class)
	public void unSubscribeHubNullRespExceptionTest() throws URISyntaxException {
		mockServer.expect(ExpectedCount.once(), requestTo(new URI("http://localhost:9191/hub")))
				.andExpect(method(HttpMethod.POST)).andRespond(withStatus(HttpStatus.NO_CONTENT));
		subscriptionClient.subscribe(subscriptionRequest);
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
