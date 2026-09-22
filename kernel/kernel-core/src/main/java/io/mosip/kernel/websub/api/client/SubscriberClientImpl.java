package io.mosip.kernel.websub.api.client;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.core.websub.spi.SubscriptionExtendedClient;
import io.mosip.kernel.websub.api.annotation.Generated;
import io.mosip.kernel.websub.api.config.publisher.RestTemplateHelper;
import io.mosip.kernel.websub.api.constants.HubMode;
import io.mosip.kernel.websub.api.constants.WebSubClientConstants;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.model.FailedContentRequest;
import io.mosip.kernel.websub.api.model.FailedContentResponse;
import io.mosip.kernel.websub.api.model.HubResponse;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;
import io.mosip.kernel.websub.api.util.ParseUtil;

/**
 * HTTP implementation of {@link SubscriptionClient} and {@link SubscriptionExtendedClient}
 * against a WebSub hub (<a href="https://www.w3.org/TR/websub/">W3C WebSub</a>).
 * <p>
 * Subscribe and unsubscribe are form {@code POST}s using
 * {@code HttpEntity<MultiValueMap<String, String>>}. Failed-content pull is a header-only
 * {@code GET} using {@code HttpEntity<Void>}. Do not pass {@link HttpHeaders} as the entity
 * body.
 * </p>
 * <p>
 * When {@code mosip.kernel.websub-db-version-client-behaviour-enable} is {@code true},
 * the callback URL is appended with {@code ?intentMode=} so the hub's GET verification
 * can distinguish subscribe versus unsubscribe. Lease seconds are sent only when greater
 * than zero.
 * </p>
 *
 * @author Urvil Joshi
 * @see SubscriptionClient
 * @see SubscriptionExtendedClient
 * @see io.mosip.kernel.websub.api.config.WebSubClientConfig
 */
public class SubscriberClientImpl
		implements SubscriptionClient<SubscriptionChangeRequest, UnsubscriptionRequest, SubscriptionChangeResponse>,
		SubscriptionExtendedClient<FailedContentResponse, FailedContentRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberClientImpl.class);

	@Autowired
	private RestTemplateHelper restTemplateHelper;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * When {@code true}, callback URLs include {@code intentMode} for hub verification
	 * ({@code mosip.kernel.websub-db-version-client-behaviour-enable}).
	 */
	@Value("${mosip.kernel.websub-db-version-client-behaviour-enable:false}")
	private boolean isWebsubDbVersionClientBehaviourEnable;

	/**
	 * Sends a subscribe request ({@code hub.mode=subscribe}) and returns hub URL and topic
	 * on HTTP {@code 202} or {@code 200} with {@code hub.mode=accepted}.
	 *
	 * @param subscriptionRequest hub URL, topic, callback, secret, and optional lease; all
	 *                            required string fields must be non-blank
	 * @return the accepted subscription (hub URL and topic copied from the request)
	 * @throws WebSubClientException if required fields are missing, the hub HTTP call fails,
	 *                               or {@code hub.mode} is not accepted
	 */
	@Override
	public SubscriptionChangeResponse subscribe(SubscriptionChangeRequest subscriptionRequest) {
		// TODO code duplicacy remove
		// TODO retries on redirect
		verifySubscribeModel(subscriptionRequest);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.SUBSCRIBE.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, subscriptionRequest.getTopic());
		if (!isWebsubDbVersionClientBehaviourEnable) {
			map.add(WebSubClientConstants.HUB_CALLBACK,
					subscriptionRequest.getCallbackURL());
		} else {
			map.add(WebSubClientConstants.HUB_CALLBACK, subscriptionRequest.getCallbackURL().concat("?intentMode=")
					.concat(HubMode.SUBSCRIBE.gethubModeValue()));
		}
		map.add(WebSubClientConstants.HUB_SECRET, subscriptionRequest.getSecret());

		if (subscriptionRequest.getLeaseSeconds() > 0) {
			map.add(WebSubClientConstants.HUB_LEASE_SECONDS, Integer.toString(subscriptionRequest.getLeaseSeconds()));
		}
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response;
		try {
			response = restTemplateHelper.getRestTemplate().exchange(subscriptionRequest.getHubURL(), HttpMethod.POST,
					entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("subscribing for topic {} at hub", subscriptionRequest.getTopic());
			SubscriptionChangeResponse subscriptionChangeResponse = new SubscriptionChangeResponse();
			subscriptionChangeResponse.setHubURL(subscriptionRequest.getHubURL());
			subscriptionChangeResponse.setTopic(subscriptionRequest.getTopic());
			return subscriptionChangeResponse;
		} else if (response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("subscribing for topic {} at hub", subscriptionRequest.getTopic());
				SubscriptionChangeResponse subscriptionChangeResponse = new SubscriptionChangeResponse();
				subscriptionChangeResponse.setHubURL(subscriptionRequest.getHubURL());
				subscriptionChangeResponse.setTopic(subscriptionRequest.getTopic());
				return subscriptionChangeResponse;
			} else {
				LOGGER.error(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + response.getBody());
				throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
						WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + hubResponse.getErrorReason());
			}

		} else {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + response.getBody());
		}
	}

	/**
	 * Rejects a subscribe request that is missing callback, hub URL, secret, or topic.
	 *
	 * @param subscriptionRequest request to validate
	 * @throws WebSubClientException if any required field is null or empty
	 */
	private void verifySubscribeModel(SubscriptionChangeRequest subscriptionRequest) {
		if (EmptyCheckUtils.isNullEmpty(subscriptionRequest.getCallbackURL())) {
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage()
							.concat("callback url is null or empty"));
		} else if (EmptyCheckUtils.isNullEmpty(subscriptionRequest.getHubURL())) {
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage()
							.concat("HUB url is null or empty"));
		} else if (EmptyCheckUtils.isNullEmpty(subscriptionRequest.getSecret())) {
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("secret is null or empty"));
		} else if (EmptyCheckUtils.isNullEmpty(subscriptionRequest.getTopic())) {
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("topic is null or empty"));
		}

	}

	/**
	 * Rejects an unsubscribe request that is missing callback, hub URL, or topic.
	 *
	 * @param unsubscriptionRequest request to validate
	 * @throws WebSubClientException if any required field is null or empty
	 */
	private void verifyUnsubscribeModel(UnsubscriptionRequest unsubscriptionRequest) {
		if (EmptyCheckUtils.isNullEmpty(unsubscriptionRequest.getCallbackURL())) {
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage()
							.concat("callback url is null or empty"));
		} else if (EmptyCheckUtils.isNullEmpty(unsubscriptionRequest.getHubURL())) {
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage()
							.concat("HUB url is null or empty"));
		} else if (EmptyCheckUtils.isNullEmpty(unsubscriptionRequest.getTopic())) {
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("topic is null or empty"));
		}

	}

	/**
	 * Sends an unsubscribe request ({@code hub.mode=unsubscribe}) and returns hub URL and
	 * topic on HTTP {@code 202} or {@code 200} with {@code hub.mode=accepted}.
	 *
	 * @param unsubscriptionRequest hub URL, topic, and callback; all required string fields
	 *                              must be non-blank
	 * @return the accepted unsubscription (hub URL and topic copied from the request)
	 * @throws WebSubClientException if required fields are missing, the hub HTTP call fails,
	 *                               or {@code hub.mode} is not accepted
	 */
	@Override
	public SubscriptionChangeResponse unSubscribe(UnsubscriptionRequest unsubscriptionRequest) {
		// TODO code duplicacy remove
		// TODO retries on redirect
		verifyUnsubscribeModel(unsubscriptionRequest);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.UNSUBSCRIBE.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, unsubscriptionRequest.getTopic());
		if (!isWebsubDbVersionClientBehaviourEnable) {
		map.add(WebSubClientConstants.HUB_CALLBACK, unsubscriptionRequest.getCallbackURL());
		}else {
			map.add(WebSubClientConstants.HUB_CALLBACK, unsubscriptionRequest.getCallbackURL().concat("?intentMode=")
					.concat(HubMode.UNSUBSCRIBE.gethubModeValue()));
		}
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response;
		try {
			response = restTemplateHelper.getRestTemplate().exchange(unsubscriptionRequest.getHubURL(), HttpMethod.POST,
					entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.UNSUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.UNSUBSCRIBE_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("unsubscribed for topic {} at hub", unsubscriptionRequest.getTopic());
			SubscriptionChangeResponse subscriptionChangeResponse = new SubscriptionChangeResponse();
			subscriptionChangeResponse.setHubURL(unsubscriptionRequest.getHubURL());
			subscriptionChangeResponse.setTopic(unsubscriptionRequest.getTopic());
			return subscriptionChangeResponse;
		} else if (response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("unsubscribed for topic {} at hub", unsubscriptionRequest.getTopic());
				SubscriptionChangeResponse subscriptionChangeResponse = new SubscriptionChangeResponse();
				subscriptionChangeResponse.setHubURL(unsubscriptionRequest.getHubURL());
				subscriptionChangeResponse.setTopic(unsubscriptionRequest.getTopic());
				return subscriptionChangeResponse;
			} else {
				LOGGER.error(WebSubClientErrorCode.UNSUBSCRIBE_ERROR.getErrorMessage() + response.getBody());
				throw new WebSubClientException(WebSubClientErrorCode.UNSUBSCRIBE_ERROR.getErrorCode(),
						WebSubClientErrorCode.UNSUBSCRIBE_ERROR.getErrorMessage() + hubResponse.getErrorReason());
			}

		} else {
			throw new WebSubClientException(WebSubClientErrorCode.UNSUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.UNSUBSCRIBE_ERROR.getErrorMessage() + response.getBody());
		}
	}

	/**
	 * Pulls previously failed content from the hub (MOSIP hub extension; not part of
	 * core WebSub).
	 * <p>
	 * Signs the request with HMAC-SHA256 in {@code X-Subscriber-Signature}. Negative
	 * {@code paginationIndex} is treated as {@code 0}. {@code messageCount} is omitted
	 * from the query when it is not positive.
	 * </p>
	 *
	 * @param failedContentRequest hub URL, topic, callback, timestamp, secret, and optional
	 *                             pagination
	 * @return parsed failed-content payload, or {@code null} if the body cannot be mapped
	 * @deprecated MOSIP failed-content pull is deprecated; prefer hub replay / new
	 *             subscriptions
	 */
	@Generated
	@Deprecated
	@Override
	public FailedContentResponse getFailedContent(FailedContentRequest failedContentRequest) {
		int pageIndex = failedContentRequest.getPaginationIndex() < 0 ? 0 : failedContentRequest.getPaginationIndex();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		if (failedContentRequest.getMessageCount() > 0) {
			headers.set(WebSubClientConstants.SUBSCRIBER_SIGNATURE_HEADER,
					getHmac256(
							failedContentRequest.getTopic() + failedContentRequest.getCallbackURL()
									+ failedContentRequest.getTimestamp() + String.valueOf(pageIndex)
									+ String.valueOf(failedContentRequest.getMessageCount()),
							failedContentRequest.getSecret()));
		} else {
			headers.set(WebSubClientConstants.SUBSCRIBER_SIGNATURE_HEADER,
					getHmac256(
							failedContentRequest.getTopic() + failedContentRequest.getCallbackURL()
									+ failedContentRequest.getTimestamp() + String.valueOf(pageIndex),
							failedContentRequest.getSecret()));
		}
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(failedContentRequest.getHubURL())
				.queryParam("topic", failedContentRequest.getTopic())
				.queryParam("callback",
						Base64.encodeBase64URLSafeString(failedContentRequest.getCallbackURL().getBytes()))
				.queryParam("timestamp", failedContentRequest.getTimestamp()).queryParam("pageindex", pageIndex)
				.queryParam("messageCount",
						failedContentRequest.getMessageCount() <= 0 ? null : failedContentRequest.getMessageCount());

		HttpEntity<Void> entity = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplateHelper.getRestTemplate().exchange(builder.toUriString(),
				HttpMethod.GET, entity, String.class);
		FailedContentResponse failedContentResponse = null;
		try {
			failedContentResponse = objectMapper.readValue(response.getBody(), FailedContentResponse.class);
		} catch (IOException e) {
			LOGGER.error(WebSubClientErrorCode.IO_ERROR.getErrorMessage() + e.getMessage());
		}
		return failedContentResponse;
	}

	/**
	 * Computes Base64 HMAC-SHA256 of {@code value} using {@code secret}.
	 *
	 * @param value  canonical string signed for failed-content requests
	 * @param secret subscriber secret
	 * @return Base64-encoded HMAC
	 */
	@Generated
	private String getHmac256(String value, String secret) {
		HmacUtils hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret);
		return Base64.encodeBase64String(hmacUtils.hmac(value));

	}

}
