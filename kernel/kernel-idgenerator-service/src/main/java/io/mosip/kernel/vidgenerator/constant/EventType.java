package io.mosip.kernel.vidgenerator.constant;

/**
 * Vert.x event-bus addresses used to initialize and refill the VID pool.
 */
public class EventType {

	/**
	 * Prevents instantiation of this constants type.
	 */
	private EventType() {

	}

	/**
	 * Ask {@code VidPoolCheckerVerticle} whether unused VIDs are below threshold.
	 */
	public static final String CHECKPOOL = "CHECK_POOL";
	/**
	 * Ask {@code VidPopulatorVerticle} to generate and persist unused VIDs.
	 */
	public static final String GENERATEPOOL = "GENERATE_POOL";
	/**
	 * Start-up event that fills the pool before HTTP fetch is considered ready.
	 */
	public static final String INITPOOL = "INIT_POOL";
	/**
	 * Start-up generate event (reserved for pool bootstrap).
	 */
	public static final String INITPOOLGENERATE = "INIT_POOL_GENERATE";
}
