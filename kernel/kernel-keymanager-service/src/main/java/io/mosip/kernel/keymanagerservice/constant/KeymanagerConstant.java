package io.mosip.kernel.keymanagerservice.constant;

/**
 * Constants for Keymanager
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public class KeymanagerConstant {

	/**
	 * Private constructor for KeyManagerConstant
	 */
	private KeymanagerConstant() {
	}

	/**
	 * The constant Whitespace
	 */
	public static final String WHITESPACE = " ";

	/**
	 * The constant EMPTY
	 */
	public static final String EMPTY = "";

	/**
	 * The constant keyalias
	 */
	public static final String KEYALIAS = "keyAlias";

	/**
	 * The constant currentkeyalias
	 */
	public static final String CURRENTKEYALIAS = "currentKeyAlias";

	/**
	 * The constant timestamp
	 */
	public static final String TIMESTAMP = "timestamp";

	/**
	 * The constant sessionID
	 */
	public static final String SESSIONID = "sessionId";

	/**
	 * The constant applicationId
	 */
	public static final String APPLICATIONID = "applicationId";

	/**
	 * The constant referenceId
	 */
	public static final String REFERENCEID = "referenceId";

	/**
	 * The constant Request received to getPublicKey
	 */
	public static final String GET_CERTIFICATE = "Request received to getCertificate";

	/**
	 * The constant Getting public key from DB Store
	 */
	public static final String GETPUBLICKEYDB = "Getting public key from DB Store";

	/**
	 * The constant Getting public key from SoftHSM
	 */
	public static final String GETPUBLICKEYHSM = "Getting Certificate from KeyStore.";

	/**
	 * The constant Getting key alias
	 */
	public static final String GETALIAS = "Getting key alias";

	/**
	 * The constant Getting expiry policy
	 */
	public static final String GETEXPIRYPOLICY = "Getting expiry policy";

	/**
	 * The constant Request received to decryptSymmetricKey
	 */
	public static final String DECRYPTKEY = "Request received to decryptSymmetricKey";

	/**
	 * The constant Getting private key
	 */
	public static final String GETPRIVATEKEY = "Getting private key";

	/**
	 * The constant Storing key in KeyAlias
	 */
	public static final String STOREKEYALIAS = "Storing key in KeyAlias";

	/**
	 * The constant Storing key in dbKeyStore
	 */
	public static final String STOREDBKEY = "Storing key in dbKeyStore";

	/**
	 * The constant keyFromDBStore
	 */
	public static final String KEYFROMDB = "keyFromDBStore";

	/**
	 * The constant keyPolicy
	 */
	public static final String KEYPOLICY = "keyPolicy";

	/**
	 * The constant symmetricKeyRequestDto
	 */
	public static final String SYMMETRICKEYREQUEST = "symmetricKeyRequestDto";

	/**
	 * The constant fetchedKeyAlias
	 */
	public static final String FETCHEDKEYALIAS = "fetchedKeyAlias";

	/**
	 * The constant dbKeyStore
	 */
	public static final String DBKEYSTORE = "dbKeyStore";

	/**
	 * The constant RSA
	 */
	public static final String RSA = "RSA";

	/**
	 * The constant INVALID_REQUEST
	 */
	public static final String INVALID_REQUEST = "should not be null or empty";

	public static final String STORECERTIFICATE = "Storing certificate";

	/**
	 * The constant INVALID_REQUEST
	 */
	public static final String REQUEST_FOR_MASTER_KEY_GENERATION = "Request for Master Key Generation";

	public static final String REQUEST_TYPE_CERTIFICATE = "CERTIFICATE";

	public static final String REQUEST_TYPE_CSR = "CSR";

	public static final String ROOT_KEY = "Root Key"; 

	public static final String CERTIFICATE_TYPE = "X.509";

	public static final String BASE_KEY_POLICY_CONST = "BASE";

	public static final String UPLOAD_SUCCESS = "Upload Success";

	public static final String CERTIFICATE_PARSE = "CERTIFICATE_PARSE";

	/**
	 * The constant KeyStore PrivateKey NotAvailable
	 */
	public static final String KS_PK_NA = "NA";

	public static final String ROOT = "ROOT";

	public static final String REQ_SYM_KEY_GEN = "Request for Symmetric Key Generation.";

	public static final int SYMMETRIC_KEY_VALIDITY = 365 * 10;

	public static final String GENERATE_SUCCESS = "Generation Success";

	public static final String SYMM_KEY_EXISTS = "Key Exists.";
}
