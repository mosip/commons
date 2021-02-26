package io.mosip.kernel.websub.api.constants;

/**
 * This class consist constants for this api.
 * 
 * @author Urvil Joshi
 *
 */
public class WebSubClientConstants {

	private WebSubClientConstants() {
	}

	public static final String HUB_TOPIC = "hub.topic";
	public static final String HUB_MODE = "hub.mode";
	public static final String HUB_CALLBACK = "hub.callback";
	public static final String HUB_SECRET = "hub.secret";
	public static final String HUB_LEASE_SECONDS = "hub.lease_seconds";
	public static final String HUB_CHALLENGE = "hub.challenge";
	public static final String HUB_AUTHENTICATED_CONTENT_HEADER = "x-hub-signature";
	public static final String SUBSCRIBER_SIGNATURE_HEADER = "X-Subscriber-Signature";
	public static final String TOPIC = "topic";
	public static final String CALLBACK = "callback";
	public static final String TIMESTAMP = "timestamp";
	public static final String MESSAGECOUNT = "messageCount";
}
