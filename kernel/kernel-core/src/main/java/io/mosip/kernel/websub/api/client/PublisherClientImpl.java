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
 * HTTP implementation of {@link PublisherClient} against a WebSub hub
 * (<a href="https://www.w3.org/TR/websub/">W3C WebSub</a>).
 * <p>
 * Register, unregister, publish, and notify are {@code POST}s. Form-encoded hub
 * operations use {@code HttpEntity<MultiValueMap<String, String>>}. Header-only notify
 * uses {@code HttpEntity<Void>}. Do not pass {@link HttpHeaders} as the entity body: the
 * single-arg {@code HttpEntity(T)} constructor treats that object as payload.
 * </p>
 * <p>
 * Hub replies that the topic is already registered are treated as success so service
 * startup can register topics idempotently. HTTP {@code 202 Accepted} and {@code 200 OK}
 * with {@code hub.mode=accepted} are success; other statuses raise
 * {@link WebSubClientException}.
 * </p>
 *
 * @author Urvil Joshi
 * @param <P> payload type sent on {@link #publishUpdate}
 * @see PublisherClient
 * @see io.mosip.kernel.websub.api.config.publisher.WebSubPublisherClientConfig
 */
public class PublisherClientImpl<P> implements PublisherClient<String, P, HttpHeaders> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PublisherClientImpl.class);

	@Autowired
	private RestTemplateHelper restTemplateHelper;

	/**
	 * Registers {@code topic} at the hub ({@code hub.mode=register}).
	 * <p>
	 * If the hub reports the topic is already registered (HTTP error body or denied
	 * reason containing {@code already registered}), this method returns without throwing.
	 * When the hub reports that the publisher is not authorized, an extra error log
	 * points at IAM client credentials and hub ACL.
	 * </p>
	 *
	 * @param topic  topic URL to register; must be accepted by the hub
	 * @param hubURL hub HTTP endpoint used for register / unregister
	 * @throws WebSubClientException if the hub rejects the register (unless already registered)
	 */
	@Override
	public void registerTopic(String topic, String hubURL) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.REGISTER.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response;
		try {
			response = restTemplateHelper.getRestTemplate().exchange(hubURL, HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			String responseBody = exception.getResponseBodyAsString();
			if (isTopicAlreadyRegistered(responseBody)) {
				LOGGER.debug("WebSub topic already registered: topic={}, hubUrl={}", topic, hubURL);
				return;
			}
			logRegisterFailure(topic, hubURL, responseBody, exception.getStatusCode().value());
			throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + responseBody);
		}
		if (response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("WebSub topic registered: topic={}, hubUrl={}", topic, hubURL);
		} else if (response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("WebSub topic registered: topic={}, hubUrl={}", topic, hubURL);
			} else {
				String denialReason = hubResponse.getErrorReason();
				if (isTopicAlreadyRegistered(denialReason) || isTopicAlreadyRegistered(response.getBody())) {
					LOGGER.debug("WebSub topic already registered: topic={}, hubUrl={}", topic, hubURL);
					return;
				}
				logRegisterFailure(topic, hubURL, denialReason != null ? denialReason : response.getBody(),
						response.getStatusCode().value());
				throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
						WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + denialReason);
			}
		} else {
			logRegisterFailure(topic, hubURL, response.getBody(), response.getStatusCode().value());
			throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + response.getBody());
		}
	}

	/**
	 * Unregisters {@code topic} at the hub ({@code hub.mode=unregister}).
	 *
	 * @param topic  topic URL to unregister
	 * @param hubURL hub HTTP endpoint used for register / unregister
	 * @throws WebSubClientException if the hub returns a non-success status or {@code hub.mode} is not accepted
	 */
	@Override
	public void unregisterTopic(String topic, String hubURL) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.UNREGISTER.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response;
		try {
			response = restTemplateHelper.getRestTemplate().exchange(hubURL, HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.UNREGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.UNREGISTER_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("topic {} unregistered at hub", topic);
		} else if (response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("topic {} unregistered at hub", topic);
			} else {
				LOGGER.error(WebSubClientErrorCode.UNREGISTER_ERROR.getErrorMessage() + response.getBody());
				throw new WebSubClientException(WebSubClientErrorCode.UNREGISTER_ERROR.getErrorCode(),
						WebSubClientErrorCode.UNREGISTER_ERROR.getErrorMessage() + hubResponse.getErrorReason());
			}
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.UNREGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.UNREGISTER_ERROR.getErrorMessage() + response.getBody());
		}
	}

	/**
	 * Publishes {@code payload} as a content distribution for {@code topic}
	 * ({@code hub.mode=publish} query parameters on {@code hubURL}).
	 * <p>
	 * Caller headers are copied into a new {@link HttpHeaders} instance so the caller's
	 * map is not mutated. {@code Content-Type} is set from {@code contentType}.
	 * </p>
	 *
	 * @param topic       topic URL being updated
	 * @param payload     body sent to the hub
	 * @param contentType media type of {@code payload} (parsed by {@link MediaType#parseMediaType(String)})
	 * @param headers     extra request headers, or {@code null} for none besides content type
	 * @param hubURL      hub publish URL (query {@code hub.mode} and {@code hub.topic} are appended)
	 * @throws WebSubClientException if the hub returns a non-success status or {@code hub.mode} is not accepted
	 */
	@Override
	public void publishUpdate(String topic, P payload, String contentType, HttpHeaders headers, String hubURL) {
		HttpHeaders requestHeaders = headers != null ? headers : new HttpHeaders();
		requestHeaders.setContentType(MediaType.parseMediaType(contentType));

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(hubURL)
				.queryParam(WebSubClientConstants.HUB_MODE, HubMode.PUBLISH.gethubModeValue())
				.queryParam(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<P> entity = new HttpEntity<>(payload, requestHeaders);
		ResponseEntity<String> response;
		try {
			response = restTemplateHelper.getRestTemplate().exchange(builder.toUriString(), HttpMethod.POST, entity,
					String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.PUBLISH_ERROR.getErrorCode(),
					WebSubClientErrorCode.PUBLISH_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("published topic {} update at hub", topic);
		} else if (response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("published topic {} update at hub", topic);
			} else {
				LOGGER.error(WebSubClientErrorCode.PUBLISH_ERROR.getErrorMessage() + response.getBody());
				throw new WebSubClientException(WebSubClientErrorCode.PUBLISH_ERROR.getErrorCode(),
						WebSubClientErrorCode.PUBLISH_ERROR.getErrorMessage() + hubResponse.getErrorReason());
			}
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.PUBLISH_ERROR.getErrorCode(),
					WebSubClientErrorCode.PUBLISH_ERROR.getErrorMessage() + response.getBody());
		}
	}

	/**
	 * Notifies the hub that an update is available without sending a body
	 * ({@code hub.mode=publish}), for hubs that fetch content separately.
	 *
	 * @param topic   topic URL to notify
	 * @param headers request headers (may be {@code null}); used as the entity header map
	 * @param hubURL  hub publish URL (query {@code hub.mode} and {@code hub.topic} are appended)
	 * @throws WebSubClientException if the hub returns a non-success status or {@code hub.mode} is not accepted
	 */
	@Override
	public void notifyUpdate(String topic, HttpHeaders headers, String hubURL) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(hubURL)
				.queryParam(WebSubClientConstants.HUB_MODE, HubMode.PUBLISH.gethubModeValue())
				.queryParam(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<Void> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response;
		try {
			response = restTemplateHelper.getRestTemplate().exchange(builder.toUriString(), HttpMethod.POST, entity,
					String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorCode(),
					WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("notify topic {} update at hub", topic);
		} else if (response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("notify topic {} update at hub", topic);
			} else {
				LOGGER.error(WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorMessage() + response.getBody());
				throw new WebSubClientException(WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorCode(),
						WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorMessage() + hubResponse.getErrorReason());
			}
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorCode(),
					WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorMessage() + response.getBody());
		}
	}

	/**
	 * Whether a hub error body or denial reason indicates the topic is already registered.
	 *
	 * @param hubResponse hub body or {@code hub.reason} value; {@code null} is treated as not registered
	 * @return {@code true} if {@code hubResponse} contains {@code already registered} (case-insensitive)
	 */
	private static boolean isTopicAlreadyRegistered(String hubResponse) {
		return hubResponse != null && hubResponse.toLowerCase().contains("already registered");
	}

	/**
	 * Logs a failed register, including a dedicated message when the publisher is not authorized.
	 *
	 * @param topic       topic that failed to register
	 * @param hubURL      hub URL used for the request
	 * @param hubResponse hub body or denial reason; {@code null} is logged as {@code <empty hub response>}
	 * @param httpStatus  HTTP status from the hub
	 */
	private static void logRegisterFailure(String topic, String hubURL, String hubResponse, int httpStatus) {
		String reason = hubResponse != null ? hubResponse : "<empty hub response>";
		LOGGER.error("WebSub topic registration denied: topic={}, hubUrl={}, httpStatus={}, hubResponse={}",
				topic, hubURL, httpStatus, reason);
		if (isPublisherNotAuthorized(reason)) {
			LOGGER.error(
					"WebSub publisher not authorized for topic={}. Verify websub.publish.url, IAM client credentials "
							+ "(mosip.iam.adapter.clientid / clientsecret), and hub publisher ACL",
					topic);
		}
	}

	/**
	 * Whether a hub message indicates the publisher is not authorized for the topic.
	 *
	 * @param hubResponse hub body or logged reason; {@code null} is treated as authorized / unknown
	 * @return {@code true} if {@code hubResponse} contains {@code publisher is not authorized} (case-insensitive)
	 */
	private static boolean isPublisherNotAuthorized(String hubResponse) {
		return hubResponse != null && hubResponse.toLowerCase().contains("publisher is not authorized");
	}

}
