package io.mosip.kernel.websub.api.constants;

/**
 * Form field names, query parameters, and HTTP headers used by the MOSIP WebSub
 * publisher and subscriber clients.
 *
 * @author Urvil Joshi
 * @see HubMode
 */
public class WebSubClientConstants {

	/**
	 * Not instantiable; constants only.
	 */
	private WebSubClientConstants() {
	}

	/**
	 * Form field {@code hub.topic}.
	 */
	public static final String HUB_TOPIC = "hub.topic";
	/**
	 * Form field {@code hub.mode} ({@link HubMode}).
	 */
	public static final String HUB_MODE = "hub.mode";
	/**
	 * Form field {@code hub.callback}.
	 */
	public static final String HUB_CALLBACK = "hub.callback";
	/**
	 * Form field {@code hub.secret}.
	 */
	public static final String HUB_SECRET = "hub.secret";
	/**
	 * Form field {@code hub.lease_seconds}.
	 */
	public static final String HUB_LEASE_SECONDS = "hub.lease_seconds";
	/**
	 * Form field {@code hub.challenge} on intent-verification GET.
	 */
	public static final String HUB_CHALLENGE = "hub.challenge";
	/**
	 * Content-distribution signature header {@code x-hub-signature}.
	 */
	public static final String HUB_AUTHENTICATED_CONTENT_HEADER = "x-hub-signature";
	/**
	 * MOSIP failed-content HMAC header {@code X-Subscriber-Signature}.
	 */
	public static final String SUBSCRIBER_SIGNATURE_HEADER = "X-Subscriber-Signature";
	/**
	 * Failed-content query parameter {@code topic}.
	 */
	public static final String TOPIC = "topic";
	/**
	 * Failed-content query parameter {@code callback}.
	 */
	public static final String CALLBACK = "callback";
	/**
	 * Failed-content query parameter {@code timestamp}.
	 */
	public static final String TIMESTAMP = "timestamp";
	/**
	 * Failed-content query parameter {@code messageCount}.
	 */
	public static final String MESSAGECOUNT = "messageCount";
}
