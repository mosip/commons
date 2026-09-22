package io.mosip.kernel.core.hotlist.constant;

/**
 * Identifier-type names used when recording or querying the MOSIP hotlist.
 * <p>
 * Contract: this type is not instantiable. Values are stored as strings in
 * hotlist APIs and must match these constants exactly.
 * </p>
 *
 * @author Manoj SP
 */
public class HotlistIdTypes {

	/**
	 * Unique Identification Number.
	 */
	public static final String UIN = "UIN";
	
	/**
	 * Virtual ID.
	 */
	public static final String VID = "VID";
	
	/**
	 * Registration-client machine identifier.
	 */
	public static final String MACHINE_ID = "MACHINE_ID";
	
	/**
	 * Partner (MISP / device / FTM) identifier.
	 */
	public static final String PARTNER_ID = "PARTNER_ID";
	
	/**
	 * Registration operator identifier.
	 */
	public static final String OPERATOR_ID = "OPERATOR_ID";
	
	/**
	 * Registration-center identifier.
	 */
	public static final String CENTER_ID = "CENTER_ID";
	
	/**
	 * Registered device identifier.
	 */
	public static final String DEVICE = "DEVICE";
	
	/**
	 * Device-provider partner identifier.
	 */
	public static final String DEVICE_PROVIDER = "DEVICE_PROVIDER";
	
	/**
	 * Foundational Trust Module public key identifier.
	 */
	public static final String FTM_PUBLIC_KEY = "FTM_PUBLIC_KEY";
}
