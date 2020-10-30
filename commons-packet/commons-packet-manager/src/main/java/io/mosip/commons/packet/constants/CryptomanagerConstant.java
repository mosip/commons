package io.mosip.commons.packet.constants;

/**
 * The Class CryptomanagerConstant.
 * 
 * @author Sowmya
 */
public class CryptomanagerConstant {

	/**
	 * Private Constructor for this class.
	 */
	private CryptomanagerConstant() {

	}

	/** The Constant WHITESPACE. */
	public static final String WHITESPACE = " ";

	/** The Constant INVALID_REQUEST. */
	public static final String INVALID_REQUEST = "should not be null or empty";

	public static final int GCM_NONCE_LENGTH = 12;

	public static final int GCM_AAD_LENGTH = 32;

	public static final String SIGNATURES_SUCCESS = "success";
}
