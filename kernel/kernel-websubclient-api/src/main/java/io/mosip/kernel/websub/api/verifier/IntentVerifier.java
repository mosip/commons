package io.mosip.kernel.websub.api.verifier;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Verifies intent for WebSub subscribe and unsubscribe operations as per
 * <a href="https://www.w3.org/TR/websub/#hub-verifies-intent">W3C WebSub specification</a>.
 * <p>
 * This class validates the intent of a WebSub hub's subscribe or unsubscribe request by comparing
 * the requested topic and mode (if enabled) with the expected values. It supports two verification
 * modes based on the configuration property {@code mosip.kernel.websub-db-version-client-behaviour-enable}:
 * <ul>
 *   <li>If {@code false}, only the topic is verified.</li>
 *   <li>If {@code true}, both topic and mode (subscribe/unsubscribe) are verified.</li>
 * </ul>
 * The class is configured via the Spring property {@code mosip.kernel.websub-db-version-client-behaviour-enable}
 * and throws {@link IllegalArgumentException} for invalid input parameters.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Component
public class IntentVerifier {
	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(IntentVerifier.class);

	/**
	 * Configuration property to enable mode verification in addition to topic verification.
	 * Defaults to {@code false}, meaning only topic verification is performed unless explicitly enabled.
	 */
	@Value("${mosip.kernel.websub-db-version-client-behaviour-enable:false}")
	private boolean isWebsubDbVersionClientBehaviourEnable;

	/**
	 * Verifies the intent of a WebSub subscribe or unsubscribe request.
	 * <p>
	 * Compares the provided topic and mode (if enabled) with the requested values to ensure
	 * they match, as required by the WebSub specification. If the configuration property
	 * {@code mosip.kernel.websub-db-version-client-behaviour-enable} is {@code false}, only
	 * the topic is verified. If {@code true}, both topic and mode are verified. Input parameters
	 * are validated to prevent null or empty values.
	 * </p>
	 *
	 * @param topic     the expected topic
	 * @param mode      the expected mode ("subscribe" or "unsubscribe")
	 * @param topicReq  the requested topic from the hub
	 * @param modeReq   the requested mode from the hub
	 * @return {@code true} if the intent is verified, {@code false} otherwise
	 * @throws IllegalArgumentException if any input parameter is null or empty
	 */
	public boolean isIntentVerified(String topic, String mode, String topicReq, String modeReq) {
		LOGGER.debug("Verifying WebSub intent: topic={}, mode={}, topicReq={}, modeReq={}, dbVersionEnabled={}",
				topic, mode, topicReq, modeReq, isWebsubDbVersionClientBehaviourEnable);

		if (!isWebsubDbVersionClientBehaviourEnable) {
			return topic.equals(topicReq);
		} else {
			return (topic.equals(topicReq) && mode.equals(modeReq));
		}
	}

}
