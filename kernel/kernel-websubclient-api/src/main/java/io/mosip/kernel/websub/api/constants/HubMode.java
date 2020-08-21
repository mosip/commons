package io.mosip.kernel.websub.api.constants;

/**
 * This {@link Enum} comprises of Hub mods used in this api.
 * {@link #REGISTER},{@link #UNREGISTER},{@link #PUBLISH},{@link #SUBSCRIBE},{@link #UNSUBSCRIBE}
 * 
 * @author Urvil Joshi
 *
 */
public enum HubMode {

	/**
	 * Register mode
	 */
	REGISTER("register"), 
	/**
	 * Unregister mode
	 */
	UNREGISTER("unregister"),
	/**
	 * Publish mode
	 */
	PUBLISH("publish"), 
	/**
	 * Subscribe mode
	 */
	SUBSCRIBE("subscribe"),
	/**
	 * Unsubscribe mode
	 */
	UNSUBSCRIBE("unsubscribe");

	private final String hubModeValue;

	private HubMode(String hubModeValue) {
		this.hubModeValue = hubModeValue;
	}

	public String gethubModeValue() {
		return this.hubModeValue;
	}
}
