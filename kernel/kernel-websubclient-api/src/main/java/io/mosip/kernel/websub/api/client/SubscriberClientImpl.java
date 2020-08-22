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
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.core.websub.spi.SubscriptionClient;
import io.mosip.kernel.websub.api.constants.HubMode;
import io.mosip.kernel.websub.api.constants.WebSubClientConstants;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.model.SubscriptionChangeRequest;
import io.mosip.kernel.websub.api.model.SubscriptionChangeResponse;
import io.mosip.kernel.websub.api.model.UnsubscriptionRequest;

/**
 * This class is responsible for all the specification stated in
 * {@link SubscriptionClient} interface.
 * 
 * @author Urvil Joshi
 *
 */
@Component
public class SubscriberClientImpl implements SubscriptionClient<SubscriptionChangeRequest,UnsubscriptionRequest, SubscriptionChangeResponse> {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberClientImpl.class);

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public SubscriptionChangeResponse subscribe(SubscriptionChangeRequest subscriptionRequest) {
		//TODO code duplicacy remove
		// TODO retries on redirect
        verifySubscribeModel(subscriptionRequest);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.SUBSCRIBE.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, subscriptionRequest.getTopic());
		map.add(WebSubClientConstants.HUB_CALLBACK, subscriptionRequest.getCallbackURL().concat("?intentMode=")
				.concat(HubMode.SUBSCRIBE.gethubModeValue()));
		map.add(WebSubClientConstants.HUB_SECRET, subscriptionRequest.getSecret());

		if (subscriptionRequest.getLeaseSeconds() > 0) {
			map.add(WebSubClientConstants.HUB_LEASE_SECONDS, Integer.toString(subscriptionRequest.getLeaseSeconds()));
		}
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(subscriptionRequest.getHubURL(), HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("subscribed for topic {} at hub", subscriptionRequest.getTopic());
			SubscriptionChangeResponse subscriptionChangeResponse = new SubscriptionChangeResponse();
			subscriptionChangeResponse.setHubURL(subscriptionRequest.getHubURL());
			subscriptionChangeResponse.setTopic(subscriptionRequest.getTopic());
			return subscriptionChangeResponse;
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + response.getBody());
		}
	}

	
	

	private void verifySubscribeModel(SubscriptionChangeRequest subscriptionRequest) {
		if(EmptyCheckUtils.isNullEmpty(subscriptionRequest.getCallbackURL())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("callback url is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(subscriptionRequest.getHubURL())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("HUB url is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(subscriptionRequest.getSecret())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("secret is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(subscriptionRequest.getTopic())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("topic is null or empty"));
		}
		
	}

	
	private void verifyUnsubscribeModel(UnsubscriptionRequest unsubscriptionRequest) {
		if(EmptyCheckUtils.isNullEmpty(unsubscriptionRequest.getCallbackURL())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("callback url is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(unsubscriptionRequest.getHubURL())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("HUB url is null or empty"));
		}else if(EmptyCheckUtils.isNullEmpty(unsubscriptionRequest.getTopic())){
			throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
					WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage().concat("topic is null or empty"));
		}
		
	}



	@Override
	public SubscriptionChangeResponse unSubscribe(UnsubscriptionRequest unsubscriptionRequest) {
		//TODO code duplicacy remove
		// TODO retries on redirect
		verifyUnsubscribeModel(unsubscriptionRequest);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(WebSubClientConstants.HUB_MODE, HubMode.UNSUBSCRIBE.gethubModeValue());
		map.add(WebSubClientConstants.HUB_TOPIC, unsubscriptionRequest.getTopic());
		map.add(WebSubClientConstants.HUB_CALLBACK, unsubscriptionRequest.getCallbackURL().concat("?intentMode=")
				.concat(HubMode.UNSUBSCRIBE.gethubModeValue()));
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(unsubscriptionRequest.getHubURL(), HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException | HttpServerErrorException exception) {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + exception.getResponseBodyAsString());
		}
		if (response != null && response.getStatusCode() == HttpStatus.ACCEPTED) {
			LOGGER.info("unsubscribed for topic {} at hub", unsubscriptionRequest.getTopic());
			SubscriptionChangeResponse subscriptionChangeResponse = new SubscriptionChangeResponse();
			subscriptionChangeResponse.setHubURL(unsubscriptionRequest.getHubURL());
			subscriptionChangeResponse.setTopic(unsubscriptionRequest.getTopic());
			return subscriptionChangeResponse;
		} else {
			throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
					WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage() + response.getBody());
		}
	}

}
