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

	/**
	 * Whether the hub GET matches the expected topic (and mode when DB-version
	 * client behaviour is enabled).
	 *
	 * @param topic    topic from {@link io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent}
	 * @param mode     {@code intentMode} query parameter (subscribe/unsubscribe)
	 * @param topicReq {@code hub.topic} from the hub
	 * @param modeReq  {@code hub.mode} from the hub
	 * @return {@code true} if the intent is accepted
	 */
	public boolean isIntentVerified(String topic, String mode, String topicReq, String modeReq) {
		if (!isWebsubDbVersionClientBehaviourEnable) {
			return topic.equals(topicReq);
		} else {
			return (topic.equals(topicReq) && mode.equals(modeReq));
		}
	}

}
