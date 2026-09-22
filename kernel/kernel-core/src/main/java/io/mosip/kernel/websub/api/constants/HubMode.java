package io.mosip.kernel.websub.api.constants;

/**
 * WebSub {@code hub.mode} values sent on publisher and subscriber form posts
 * (<a href="https://www.w3.org/TR/websub/">W3C WebSub</a>).
 *
 * @author Urvil Joshi
 * @see WebSubClientConstants#HUB_MODE
 */
public enum HubMode {

	/**
	 * Publisher registers a topic at the hub.
	 */
	REGISTER("register"),
	/**
	 * Publisher unregisters a topic at the hub.
	 */
	UNREGISTER("unregister"),
	/**
	 * Publisher publishes or notifies an update for a topic.
	 */
	PUBLISH("publish"),
	/**
	 * Subscriber requests a subscription.
	 */
	SUBSCRIBE("subscribe"),
	/**
	 * Subscriber requests an unsubscription.
	 */
	UNSUBSCRIBE("unsubscribe");

	/**
	 * Wire value of {@code hub.mode}.
	 */
	private final String hubModeValue;

	/**
	 * Binds the enum constant to its hub.mode token.
	 *
	 * @param hubModeValue value sent in {@code hub.mode}
	 */
	private HubMode(String hubModeValue) {
		this.hubModeValue = hubModeValue;
	}

	/**
	 * Returns the {@code hub.mode} token sent on the wire.
	 *
	 * @return {@code register}, {@code unregister}, {@code publish}, {@code subscribe}, or {@code unsubscribe}
	 */
	public String gethubModeValue() {
		return this.hubModeValue;
	}
}
