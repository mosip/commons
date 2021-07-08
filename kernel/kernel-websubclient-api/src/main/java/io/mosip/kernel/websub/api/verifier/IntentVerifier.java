package io.mosip.kernel.websub.api.verifier;

import org.springframework.stereotype.Component;

/**
 * This is a helper class to verify intent after subscribe and unsubscribe
 * operation according to
 * <a href="https://www.w3.org/TR/websub/#hub-verifies-intent">WebSub Specs</a>.
 * 
 * @author Urvil Joshi
 *
 */
//@Component
public class IntentVerifier {

	public boolean isIntentVerified(String topic, String mode, String topicReq, String modeReq) {
		return (topic.equals(topicReq) && mode.equals(modeReq));
	}

}
