package io.mosip.kernel.cryptomanager.constant;

/**
 * Constant class for Crypto-Manager-Service
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
public class CryptomanagerConstant {
	/**
	 * Private Constructor for this class
	 */
	private CryptomanagerConstant() {

	}

	public static final String WHITESPACE = " ";
	public static final String INVALID_REQUEST = "should not be null or empty";
	public static final String EMPTY_ATTRIBUTE = "should not be empty";
	public static final String EMPTY_REGEX = ".+\\S.*";

	public static final String SESSIONID = "CryptoManagerSession";

	public static final String ENCRYPT = "CryptoManagerEncrypt";

	public static final String DECRYPT = "CryptoManagerDecrypt";

	public static final int THUMBPRINT_LENGTH = 32;

	public static final int ENCRYPTED_SESSION_KEY_LENGTH = 256;
}
