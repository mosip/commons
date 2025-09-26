package io.mosip.kernel.websub.api.constants;

/**
 * Enum for WebSub hub modes as per RFC 7033.
 * <p>
 * Defines operation modes used in WebSub hub requests (e.g., hub.mode=subscribe) for topic registration,
 * publication, and subscription management. Used by components like
 * {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl} and
 * {@link io.mosip.kernel.websub.api.filter.IntentVerificationFilter}. Includes a lookup method
 * for string-to-enum conversion.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see WebSubClientConstants#HUB_MODE
 */
public enum HubMode {

	/**
	 * Mode for registering a topic with the hub.
	 */
	REGISTER("register"),

	/**
	 * Mode for unregistering a topic from the hub.
	 */
	UNREGISTER("unregister"),

	/**
	 * Mode for publishing content to a topic.
	 */
	PUBLISH("publish"),

	/**
	 * Mode for subscribing to a topic.
	 */
	SUBSCRIBE("subscribe"),

	/**
	 * Mode for unsubscribing from a topic.
	 */
	UNSUBSCRIBE("unsubscribe");

	/**
	 * The string value used in hub.mode parameter.
	 */
	private final String hubModeValue;

	/**
	 * Constructs a HubMode with the specified string value.
	 *
	 * @param hubModeValue the hub.mode value (e.g., "subscribe")
	 */
	HubMode(String hubModeValue) {
		this.hubModeValue = hubModeValue;
	}

	/**
	 * Gets the hub.mode value.
	 *
	 * @return the hub.mode value
	 */
	public String getHubModeValue() {
		return hubModeValue;
	}

	/**
	 * Converts a hub.mode string to a HubMode enum.
	 * <p>
	 * Returns the corresponding {@link HubMode} for the given string value, or null if no match is found.
	 * Case-sensitive to match RFC 7033.
	 * </p>
	 *
	 * @param value the hub.mode string (e.g., "subscribe")
	 * @return the matching HubMode, or null if not found
	 */
	public static HubMode fromString(String value) {
		for (HubMode mode : HubMode.values()) {
			if (mode.hubModeValue.equals(value)) {
				return mode;
			}
		}
		return null;
	}
}