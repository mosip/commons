package io.mosip.kernel.websub.api.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.websub.spi.PublisherClient;
import io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper;
import io.mosip.kernel.websub.api.constants.HubMode;
import io.mosip.kernel.websub.api.constants.WebSubClientConstants;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.model.HubResponse;
import io.mosip.kernel.websub.api.util.ParseUtil;

/**
 * A high-performance WebSub publisher client implementation for topic registration, unregistration, and content publishing.
 * <p>
 * This class implements the {@link PublisherClient} interface to interact with a WebSub hub for registering/unregistering
 * topics and publishing updates or notifications. It uses Spring's {@link RestTemplate} (via {@link RestTemplateHelper})
 * to make HTTP POST requests to the hub, adhering to the WebSub protocol (RFC 5988). The implementation is optimized for
 * low latency and scalability by:
 * <ul>
 *   <li>Reusing common HTTP exchange and response handling logic</li>
 *   <li>Minimizing string operations with parameterized logging</li>
 *   <li>Using efficient URI construction with {@link UriComponentsBuilder}</li>
 *   <li>Failing fast on invalid responses or HTTP errors</li>
 * </ul>
 * The class is thread-safe due to Spring's singleton scope and immutable dependencies. It throws
 * {@link WebSubClientException} for hub errors, with specific error codes for each operation (register, unregister,
 * publish, notify). Logging is performed using SLF4J with minimal overhead.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * <pre>
 * {@code
 * PublisherClientImpl<String> publisher = new PublisherClientImpl<>();
 * publisher.registerTopic("my-topic", "http://hub.example.com");
 * publisher.publishUpdate("my-topic", "Hello, WebSub!", "text/plain", new HttpHeaders(), "http://hub.example.com");
 * }
 * </pre>
 *
 * @author Urvil Joshi
 * @param <P> the type of payload for publishing updates
 * @since 1.0.0
 * @see PublisherClient
 * @see RestTemplateHelper
 * @see WebSubClientException
 */
public class PublisherClientImpl<P> implements PublisherClient<String, P, HttpHeaders> {

	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(PublisherClientImpl.class);

	/**
	 * Helper for selecting the appropriate RestTemplate (authenticated or unauthenticated).
	 */
	@Autowired
	private RestTemplateHelper restTemplateHelper;

	/**
	 * Registers a topic with the WebSub hub.
	 * <p>
	 * Sends a POST request to the hub with `hub.mode=register` and `hub.topic=topic` in a form-encoded body.
	 * Expects an HTTP 202 (Accepted) or 200 (OK with `hub.result=accepted`) response. Throws a
	 * {@link WebSubClientException} for client/server errors or unexpected responses.
	 * </p>
	 *
	 * @param topic  the topic to register
	 * @param hubURL the hub URL to send the request
	 * @throws WebSubClientException if registration fails or the hub responds with an error
	 */
	@Override
	public void registerTopic(String topic, String hubURL) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.REGISTER.getHubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, topic);
		HttpEntity<MultiValueMap<String, String>> entity = createFormEntity(map);
		exchangeAndProcessResponse(hubURL, entity, topic, "register", WebSubClientErrorCode.REGISTER_ERROR);
	}

	/**
	 * Unregisters a topic from the WebSub hub.
	 * <p>
	 * Sends a POST request to the hub with `hub.mode=unregister` and `hub.topic=topic` in a form-encoded body.
	 * Expects an HTTP 202 (Accepted) or 200 (OK with `hub.result=accepted`) response. Throws a
	 * {@link WebSubClientException} for client/server errors or unexpected responses.
	 * </p>
	 *
	 * @param topic  the topic to unregister
	 * @param hubURL the hub URL to send the request
	 * @throws WebSubClientException if unregistration fails or the hub responds with an error
	 */
	@Override
	public void unregisterTopic(String topic, String hubURL) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.UNREGISTER.getHubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, topic);
		HttpEntity<MultiValueMap<String, String>> entity = createFormEntity(map);
		exchangeAndProcessResponse(hubURL, entity, topic, "unregister", WebSubClientErrorCode.UNREGISTER_ERROR);
	}

	/**
	 * Publishes an update to a topic with a payload and content type.
	 * <p>
	 * Sends a POST request to the hub with `hub.mode=publish` and `hub.topic=topic` as query parameters,
	 * and the payload in the body with the specified content type. Headers are set or created if null.
	 * Expects an HTTP 202 (Accepted) or 200 (OK with `hub.result=accepted`) response. Throws a
	 * {@link WebSubClientException} for client/server errors or unexpected responses.
	 * </p>
	 *
	 * @param topic      the topic to publish to
	 * @param payload    the content to publish
	 * @param contentType the media type of the payload (e.g., "application/json")
	 * @param headers    custom headers (or null to use defaults)
	 * @param hubURL     the hub URL to send the request
	 * @throws WebSubClientException if publishing fails or the hub responds with an error
	 */
	@Override
	public void publishUpdate(String topic, P payload, String contentType, HttpHeaders headers, String hubURL) {
		headers = headers != null ? headers : new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(contentType));
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hubURL)
				.queryParam(WebSubClientConstants.HUB_MODE, HubMode.PUBLISH.getHubModeValue())
				.queryParam(WebSubClientConstants.HUB_TOPIC, topic);
		HttpEntity<P> entity = new HttpEntity<>(payload, headers);
		exchangeAndProcessResponse(builder.toUriString(), entity, topic, "publish", WebSubClientErrorCode.PUBLISH_ERROR);
	}

	/**
	 * Notifies the hub of an update to a topic without a payload.
	 * <p>
	 * Sends a POST request to the hub with `hub.mode=publish` and `hub.topic=topic` as query parameters,
	 * using provided headers (or empty headers if null). Expects an HTTP 202 (Accepted) or 200 (OK with
	 * `hub.result=accepted`) response. Throws a {@link WebSubClientException} for client/server errors
	 * or unexpected responses.
	 * </p>
	 *
	 * @param topic   the topic to notify
	 * @param headers custom headers (or null to use defaults)
	 * @param hubURL  the hub URL to send the request
	 * @throws WebSubClientException if notification fails or the hub responds with an error
	 */
	@Override
	public void notifyUpdate(String topic, HttpHeaders headers, String hubURL) {
		headers = headers != null ? headers : new HttpHeaders();
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hubURL)
				.queryParam(WebSubClientConstants.HUB_MODE, HubMode.PUBLISH.getHubModeValue())
				.queryParam(WebSubClientConstants.HUB_TOPIC, topic);
		HttpEntity<P> entity = new HttpEntity<>(headers);
		exchangeAndProcessResponse(builder.toUriString(), entity, topic, "notify", WebSubClientErrorCode.NOTIFY_UPDATE_ERROR);
	}

	/**
	 * Creates a form-encoded HTTP entity for topic registration/unregistration.
	 * <p>
	 * Builds an {@link HttpEntity} with a form-encoded body (`application/x-www-form-urlencoded`) and
	 * the provided form data. Optimized for minimal object creation and reuse of headers.
	 * </p>
	 *
	 * @param formData the form data to include in the body
	 * @return the HTTP entity with form-encoded content
	 */
	private HttpEntity<MultiValueMap<String, String>> createFormEntity(MultiValueMap<String, String> formData) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		return new HttpEntity<>(formData, headers);
	}

	/**
	 * Executes an HTTP exchange and processes the response for WebSub operations.
	 * <p>
	 * Sends a POST request using the provided entity and URL, handling HTTP 202 (Accepted) or 200 (OK)
	 * responses. For 200, parses the response body with {@link ParseUtil#parseHubResponse} and checks
	 * for `hub.result=accepted`. Throws a {@link WebSubClientException} for client/server errors,
	 * invalid responses, or non-accepted results. Optimized for minimal logging overhead and
	 * centralized error handling.
	 * </p>
	 *
	 * @param url        the target URL (or URI with query params)
	 * @param entity     the HTTP entity (body and headers)
	 * @param topic      the topic for logging context
	 * @param operation  the operation name (e.g., "register", "publish") for logging
	 * @param errorCode  the error code for exceptions
	 * @throws WebSubClientException if the request fails or the response is invalid
	 */
	private void exchangeAndProcessResponse(String url, HttpEntity<?> entity, String topic, String operation,
											WebSubClientErrorCode errorCode) {
		ResponseEntity<String> response;
		try {
			response = restTemplateHelper.getRestTemplate().exchange(url, HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			LOGGER.error("Failed to {} topic {}: {}", operation, topic, e.getResponseBodyAsString());
			throw new WebSubClientException(errorCode.getErrorCode(),
					errorCode.getErrorMessage() + ": " + e.getResponseBodyAsString());
		}

		if (response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("Successfully {} topic {} at hub", operation, topic);
		} else if (response.getStatusCode() == HttpStatus.OK) {
			if (response.getBody() == null) {
				LOGGER.error("Null response body for {} topic {}", operation, topic);
				throw new WebSubClientException(errorCode.getErrorCode(), errorCode.getErrorMessage() + ": Null response");
			}

			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if ("accepted".equals(hubResponse.getHubResult())) {
				LOGGER.info("Successfully {} topic {} at hub", operation, topic);
			} else {
				LOGGER.error("{} failed for topic {}: {}", operation, topic, response.getBody());
				throw new WebSubClientException(errorCode.getErrorCode(),
						errorCode.getErrorMessage() + ": " + hubResponse.getErrorReason());
			}
		} else {
			LOGGER.error("{} failed for topic {}: {}", operation, topic, response.getBody());
			throw new WebSubClientException(errorCode.getErrorCode(),
					errorCode.getErrorMessage() + ": " + response.getBody());
		}
	}
}