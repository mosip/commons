package io.mosip.kernel.websub.api.constants;

/**
 * Constants for the MOSIP WebSub API.
 * <p>
 * This class defines string constants used in WebSub protocol (RFC 7033) communications and
 * MOSIP-specific WebSub operations. These constants represent HTTP parameters, headers, and fields
 * used in hub requests, callback validation, and content authentication. The class is non-instantiable
 * to ensure immutability and is used by components like {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl},
 * {@link io.mosip.kernel.websub.api.filter.IntentVerificationFilter}, and
 * {@link io.mosip.kernel.websub.api.config.IntentVerificationConfig}.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see io.mosip.kernel.websub.api.config.WebSubClientConfig
 * @see io.mosip.kernel.websub.api.config.IntentVerificationConfig
 */
public class WebSubClientConstants {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private WebSubClientConstants() {
	}

	/**
	 * WebSub hub parameter for the topic identifier (e.g., hub.topic=test-topic).
	 */
	public static final String HUB_TOPIC = "hub.topic";

	/**
	 * WebSub hub parameter for the operation mode (e.g., hub.mode=subscribe).
	 * @see HubMode
	 */
	public static final String HUB_MODE = "hub.mode";

	/**
	 * WebSub hub parameter for the callback URL (e.g., hub.callback=http://callback).
	 */
	public static final String HUB_CALLBACK = "hub.callback";

	/**
	 * WebSub hub parameter for the shared secret for content authentication.
	 */
	public static final String HUB_SECRET = "hub.secret";

	/**
	 * WebSub hub parameter for lease duration in seconds.
	 */
	public static final String HUB_LEASE_SECONDS = "hub.lease_seconds";

	/**
	 * WebSub hub parameter for the challenge string in intent verification.
	 */
	public static final String HUB_CHALLENGE = "hub.challenge";

	/**
	 * WebSub hub parameter for error reasons in responses (e.g., hub.reason=invalid_request).
	 */
	public static final String HUB_REASON = "hub.reason";

	/**
	 * Header for hub-provided signature in authenticated content distribution (e.g., x-hub-signature=sha256:abc).
	 */
	public static final String HUB_AUTHENTICATED_CONTENT_HEADER = "x-hub-signature";

	/**
	 * MOSIP-specific header for subscriber-provided signature.
	 */
	public static final String SUBSCRIBER_SIGNATURE_HEADER = "X-Subscriber-Signature";

	/**
	 * Field for topic identifier in MOSIP WebSub metadata.
	 */
	public static final String TOPIC = "topic";

	/**
	 * Field for callback URL in MOSIP WebSub metadata.
	 */
	public static final String CALLBACK = "callback";

	/**
	 * Field for timestamp in MOSIP WebSub messages.
	 */
	public static final String TIMESTAMP = "timestamp";

	/**
	 * Field for message count in MOSIP WebSub messages.
	 */
	public static final String MESSAGECOUNT = "messageCount";
}
