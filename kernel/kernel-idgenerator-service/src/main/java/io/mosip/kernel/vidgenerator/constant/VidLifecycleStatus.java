package io.mosip.kernel.vidgenerator.constant;

/**
 * Lifecycle values stored in {@code kernel.vid.vid_status}.
 */
public class VidLifecycleStatus {

	/**
	 * Prevents instantiation of this constants type.
	 */
	private VidLifecycleStatus() {

	}

	/**
	 * Unused VID that may be issued.
	 */
	public static final String AVAILABLE = "AVAILABLE";
	/**
	 * Issued VID whose expiry has elapsed.
	 */
	public static final String EXPIRED = "EXPIRED";
	/**
	 * VID already issued to a caller.
	 */
	public static final String ASSIGNED = "ASSIGNED";
}
