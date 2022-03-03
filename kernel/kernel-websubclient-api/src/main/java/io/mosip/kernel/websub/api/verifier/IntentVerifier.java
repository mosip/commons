package io.mosip.kernel.websub.api.verifier;

import org.springframework.beans.factory.annotation.Value;

/**
 * This is a helper class to verify intent after subscribe and unsubscribe
 * operation according to
 * <a href="https://www.w3.org/TR/websub/#hub-verifies-intent">WebSub Specs</a>.
 * 
 * @author Urvil Joshi
 *
 */
public class IntentVerifier {

	@Value("${mosip.kernel.websub-db-version-client-behaviour-enable:false}")
	private boolean isWebsubDbVersionClientBehaviourEnable;

	public boolean isIntentVerified(String topic, String mode, String topicReq, String modeReq) {
		if (!isWebsubDbVersionClientBehaviourEnable) {
			return topic.equals(topicReq);
		} else {
			return (topic.equals(topicReq) && mode.equals(modeReq));
		}
	}

}
