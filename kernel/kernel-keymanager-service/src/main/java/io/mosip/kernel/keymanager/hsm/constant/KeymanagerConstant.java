package io.mosip.kernel.keymanager.hsm.constant;

/**
 * Constants for Softhsm Keystore
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public final class KeymanagerConstant {

	/**
	 * Private constructor for SofthsmKeystoreConstant
	 */
	private KeymanagerConstant() {
	}

	/**
	 * String constant for dot
	 */
	public static final String DOT = ".";
	/**
	 * String constant for signature algorithm
	 */
	public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";


	public static final String 	KEYSTORE_TYPE_PKCS11 = "PKCS11";

	public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";
}
