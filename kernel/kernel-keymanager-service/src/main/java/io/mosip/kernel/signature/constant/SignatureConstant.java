package io.mosip.kernel.signature.constant;

/**
 * Constant class for Signature Constant Service
 * 
 * @author Uday Kumar
 *
 * @since 1.0.0
 */
public class SignatureConstant {
	/**
	 * Private Constructor for this class
	 */
	private SignatureConstant() {

	}

	public static final String VALIDATION_SUCCESSFUL = "Validation Successful";
	public static final String SUCCESS = "success";

	public static final String SESSIONID = "SignatureSessionId";

	public static final String JWT_SIGN = "JWTSignature";

	public static final String BLANK = "";

	public static final Boolean DEFAULT_INCLUDES = false;

	public static final String JWT_HEADER_CERT_KEY = "x5c";

	public static final String PERIOD = "\\.";

	public static final String VALIDATION_FAILED = "Validation Failed";

	public static final String TRUST_NOT_VERIFIED = "TRUST_NOT_VERIFIED";

	public static final String TRUST_NOT_VERIFIED_NO_DOMAIN = "TRUST_NOT_VERIFIED_NO_DOMAIN";

	public static final String TRUST_NOT_VALID = "TRUST_CERT_PATH_NOT_VALID";

	public static final String TRUST_VALID = "TRUST_CERT_PATH_VALID";

}
