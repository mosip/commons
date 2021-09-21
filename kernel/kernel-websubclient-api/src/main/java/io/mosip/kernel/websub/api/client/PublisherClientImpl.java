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
 * This class is responsible for all the specification stated in
 * {@link PublisherClient} interface.
 * 
 * @author Urvil Joshi
 *
 * @param <P> Type of payload.
 */
public class PublisherClientImpl<P> implements PublisherClient<String, P, HttpHeaders> {

	private static final Logger LOGGER = LoggerFactory.getLogger(PublisherClientImpl.class);

	@Autowired
	private RestTemplateHelper restTemplateHelper;

	@Override
	public void registerTopic(String topic, String hubURL) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.REGISTER.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = null;
		try {
			response = restTemplateHelper.getRestTemplate().exchange(hubURL, HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("topic {} registered at hub", topic);
		} else if (response != null && response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("topic {} registered at hub", topic);
			} else {
				throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
						WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + hubResponse.getErrorReason());
			}

		} else {
			throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + response.getBody());
		}
	}

	@Override
	public void unregisterTopic(String topic, String hubURL) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.UNREGISTER.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = null;
		try {
			response = restTemplateHelper.getRestTemplate().exchange(hubURL, HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.UNREGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.UNREGISTER_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("topic {} unregistered at hub", topic);
		} else if (response != null && response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("topic {} unregistered at hub", topic);
			} else {
				throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
						WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + hubResponse.getErrorReason());
			}

		} else {
			throw new WebSubClientException(WebSubClientErrorCode.UNREGISTER_ERROR.getErrorCode(),
					WebSubClientErrorCode.UNREGISTER_ERROR.getErrorMessage() + response.getBody());
		}

	}

	@Override
	public void publishUpdate(String topic, P payload, String contentType, HttpHeaders headers, String hubURL) {
		if (headers == null) {
			headers = new HttpHeaders();
		}
		headers.setContentType(MediaType.parseMediaType(contentType));

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hubURL)
				.queryParam(WebSubClientConstants.HUB_MODE, HubMode.PUBLISH.gethubModeValue())
				.queryParam(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<P> entity = new HttpEntity<>(payload, headers);
		ResponseEntity<String> response = null;
		try {
			response= restTemplateHelper.getRestTemplate().exchange(builder.toUriString(), HttpMethod.POST, entity,
				String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.PUBLISH_ERROR.getErrorCode(),
					WebSubClientErrorCode.PUBLISH_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("published topic {} update at hub", topic);
		} else if (response != null && response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("published topic {} update at hub", topic);
			} else {
				throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
						WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + hubResponse.getErrorReason());
			}

		} else {
			throw new WebSubClientException(WebSubClientErrorCode.PUBLISH_ERROR.getErrorCode(),
					WebSubClientErrorCode.PUBLISH_ERROR.getErrorMessage() + response.getBody());
		}
	}

	@Override
	public void notifyUpdate(String topic, HttpHeaders headers, String hubURL) {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(hubURL)
				.queryParam(WebSubClientConstants.HUB_MODE, HubMode.PUBLISH.gethubModeValue())
				.queryParam(WebSubClientConstants.HUB_TOPIC, topic);

		HttpEntity<P> entity = new HttpEntity<>(headers);
		ResponseEntity<String> response = null;
		try {
			response= restTemplateHelper.getRestTemplate().exchange(builder.toUriString(), HttpMethod.POST, entity,
				String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorCode(),
					WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("notify topic {} update at hub", topic);
		} else if (response != null && response.getStatusCode() == HttpStatus.OK) {
			HubResponse hubResponse = ParseUtil.parseHubResponse(response.getBody());
			if (hubResponse.getHubResult().equals("accepted")) {
				LOGGER.info("notify topic {} update at hub", topic);
			} else {
				throw new WebSubClientException(WebSubClientErrorCode.REGISTER_ERROR.getErrorCode(),
						WebSubClientErrorCode.REGISTER_ERROR.getErrorMessage() + hubResponse.getErrorReason());
			}

		} else {
			throw new WebSubClientException(WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorCode(),
					WebSubClientErrorCode.NOTIFY_UPDATE_ERROR.getErrorMessage() + response.getBody());
		}

	}

}
