package io.mosip.kernel.websub.api.verifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mosip.kernel.websub.api.client.PublisherClientImpl;

/**
 * This is a helper class to verify intent after subscribe and unsubscribe
 * operation according to
 * <a href="https://www.w3.org/TR/websub/#hub-verifies-intent">WebSub Specs</a>.
 * 
 * @author Urvil Joshi
 *
 */
public class IntentVerifier {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PublisherClientImpl.class);

	public boolean isIntentVerified(String topic, String mode, String topicReq, String modeReq) {
		LOGGER.info("verification result "+(topic.equals(topicReq) && mode.equals(modeReq)));
		return (topic.equals(topicReq) && mode.equals(modeReq));
	}

}
