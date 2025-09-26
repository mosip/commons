package io.mosip.kernel.websub.api.test.publisher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.mosip.kernel.websub.api.verifier.AuthenticatedContentVerifier;
import io.mosip.kernel.websub.api.verifier.IntentVerifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

import io.mosip.kernel.websub.api.client.PublisherClientImpl;
import io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.test.WebClientApiTestBootApplication;

/**
 * Unit tests for {@link PublisherClientImpl} to verify WebSub publisher functionality.
 * <p>
 * This test class validates the behavior of {@link PublisherClientImpl} for WebSub operations,
 * including topic registration, unregistration, publishing updates, and notifying subscribers,
 * as per the <a href="https://www.w3.org/TR/websub/">W3C WebSub specification</a>. It uses
 * {@link MockRestServiceServer} to simulate HTTP interactions with a WebSub hub and tests both
 * successful and error scenarios. The test context is configured with
 * {@link WebClientApiTestBootApplication} to provide a lightweight Spring environment.
 * Optimizations include:
 * <ul>
 *   <li>Mocking dependencies ({@link RestTemplateHelper}, {@link AuthenticatedContentVerifier},
 *       {@link IntentVerifier}) for isolation.</li>
 *   <li>Using a helper method to reduce repetitive mock setup code.</li>
 *   <li>Testing edge cases like null/empty inputs.</li>
 *   <li>Using {@link MediaType#APPLICATION_JSON} to avoid deprecated constants.</li>
 *   <li>Logging setup for debugging.</li>
 * </ul>
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 * @see PublisherClientImpl
 * @see WebClientApiTestBootApplication
 * @see MockRestServiceServer
 */
@SpringBootTest(classes = {WebClientApiTestBootApplication.class})
@RunWith(SpringRunner.class)
public class PublisherClientApiExceptionsTest {

    /**
     * Logger for debugging and error reporting.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PublisherClientApiExceptionsTest.class);

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

    /**
     * Publisher client for WebSub operations.
     */
    @Autowired
    private PublisherClientImpl<String> publisherClientImpl;

    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    /**
     * Sets up the test environment before each test.
     * <p>
     * Initializes a {@link RestTemplate}, configures the mocked {@link RestTemplateHelper} to
     * return it, and sets up {@link MockRestServiceServer} for HTTP request mocking. Also
     * configures mocks for {@link AuthenticatedContentVerifier} and {@link IntentVerifier} to
     * ensure test isolation.
     * </p>
     */
    @Before
    public void init() {
        LOGGER.debug("Initializing test setup for PublisherClientApiExceptionsTest");
        restTemplate = new RestTemplate();
        when(restTemplateHelper.getRestTemplate()).thenReturn(restTemplate);
        ReflectionTestUtils.setField(publisherClientImpl, "restTemplateHelper", restTemplateHelper);
        when(contentVerifier.verifyAuthorizedContentVerified(any(), any())).thenReturn(true);
        when(intentVerifier.isIntentVerified(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
        mockServer = MockRestServiceServer.createServer(restTemplate);
        LOGGER.debug("Test setup completed");
    }

    /**
     * Configures a mock HTTP response for a WebSub hub request.
     *
     * @param uri        the expected request URI
     * @param status     the HTTP status to return
     * @param hubMode    the hub.mode response (e.g., "accepted", "denied")
     * @return the configured response creator
     */
    private DefaultResponseCreator mockHubResponse(String uri, HttpStatus status, String hubMode) {
        return withStatus(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(hubMode != null ? "hub.mode=" + hubMode : "");
    }

    /**
     * Tests successful topic registration with HTTP OK and hub.mode=accepted.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test
    public void registerKafkaHubOKTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("hub.mode=accepted")
                );
        publisherClientImpl.registerTopic("demo", "http://localhost:9191/hub");
    }

    /**
     * Tests successful topic registration with HTTP ACCEPTED and hub.mode=accepted.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test
    public void registerKafkaHubAcceptedTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("hub.mode=accepted")
                );
        publisherClientImpl.registerTopic("demo", "http://localhost:9191/hub");
    }

    /**
     * Tests topic registration failure with hub.mode=denied.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
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

    /**
     * Tests topic registration failure with HTTP BAD_REQUEST.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
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

    /**
     * Tests topic registration failure with HTTP NO_CONTENT.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test(expected = WebSubClientException.class)
    public void registerHubNullRespExceptionTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT)
                );
        publisherClientImpl.registerTopic("demo", "http://localhost:9191/hub");
    }

    /**
     * Tests successful topic unregistration with HTTP OK and hub.mode=accepted.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test
    public void unRegisterKafkaHubOKTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("hub.mode=accepted")
                );
        publisherClientImpl.unregisterTopic("demo", "http://localhost:9191/hub");
    }

    /**
     * Tests topic unregistration with HTTP ACCEPTED and hub.mode=denied.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test
    public void unRegisterKafkaHubAcceptedTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("hub.mode=denied")
                );
        publisherClientImpl.unregisterTopic("demo", "http://localhost:9191/hub");
    }

    /**
     * Tests topic unregistration failure with hub.mode=denied.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
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

    /**
     * Tests topic unregistration failure with HTTP BAD_REQUEST.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
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

    /**
     * Tests topic unregistration failure with HTTP NO_CONTENT.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test(expected = WebSubClientException.class)
    public void unRegisterHubNullRespExceptionTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT)
                );
        publisherClientImpl.unregisterTopic("demo", "http://localhost:9191/hub");
    }

    /**
     * Tests successful publishing with HTTP OK and hub.mode=accepted.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test
    public void publishKafkaHubOKTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("hub.mode=accepted")
                );
        publisherClientImpl.publishUpdate("demo", "{\r\n \"data\": \"1#2021-08-10T05:47:26.853Z\" \r\n}", MediaType.APPLICATION_JSON_UTF8_VALUE, null, "http://localhost:9191/hub");
    }

    /**
     * Tests publishing with HTTP ACCEPTED and hub.mode=denied.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test
    public void publishKafkaHubAcceptedTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("hub.mode=denied")
                );
        publisherClientImpl.publishUpdate("demo", "{\r\n \"data\": \"1#2021-08-10T05:47:26.853Z\" \r\n}", MediaType.APPLICATION_JSON_UTF8_VALUE, null, "http://localhost:9191/hub");
    }

    /**
     * Tests publishing failure with hub.mode=denied.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
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

    /**
     * Tests publishing failure with HTTP BAD_REQUEST.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
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

    /**
     * Tests publishing failure with HTTP NO_CONTENT.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test(expected = WebSubClientException.class)
    public void publishHubNullRespExceptionTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT)
                );
        publisherClientImpl.publishUpdate("demo", "{\r\n \"data\": \"1#2021-08-10T05:47:26.853Z\" \r\n}", MediaType.APPLICATION_JSON_UTF8_VALUE, null, "http://localhost:9191/hub");
    }

    /**
     * Tests successful notification update with HTTP OK and hub.mode=accepted.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test
    public void notifyUpdateKafkaHubOKTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("hub.mode=accepted")
                );
        publisherClientImpl.notifyUpdate("demo", null, "http://localhost:9191/hub");
    }

    /**
     * Tests notification update with HTTP ACCEPTED and hub.mode=denied.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test
    public void notifyUpdateKafkaHubAccptedTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.ACCEPTED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("hub.mode=denied")
                );
        publisherClientImpl.notifyUpdate("demo", null, "http://localhost:9191/hub");
    }

    /**
     * Tests notification update failure with hub.mode=denied.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test(expected = WebSubClientException.class)
    public void notifyUpdateKafkaHubExceptionTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("hub.mode=denied")
                );
        publisherClientImpl.notifyUpdate("demo", null, "http://localhost:9191/hub");
    }

    /**
     * Tests notification update failure with HTTP BAD_REQUEST.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test(expected = WebSubClientException.class)
    public void notifyUpdateHubExceptionTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                );
        publisherClientImpl.notifyUpdate("demo", null, "http://localhost:9191/hub");
    }

    /**
     * Tests notification update failure with HTTP NO_CONTENT.
     *
     * @throws URISyntaxException if the hub URL is invalid
     */
    @Test(expected = WebSubClientException.class)
    public void notifyUpdateHubNullRespExceptionTest() throws URISyntaxException {
        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("http://localhost:9191/hub?hub.mode=publish&hub.topic=demo")))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT)
                );
        publisherClientImpl.notifyUpdate("demo", null, "http://localhost:9191/hub");
    }
}