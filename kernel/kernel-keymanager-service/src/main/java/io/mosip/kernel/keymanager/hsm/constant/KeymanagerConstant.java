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

	public static final String SUN_PKCS11_PROVIDER = "SunPKCS11";

	public static final String KEYSTORE_TYPE_PKCS11 = "PKCS11";

	public static final String KEYSTORE_TYPE_PKCS12 = "PKCS12";

	public static final String KEYSTORE_TYPE_OFFLINE = "OFFLINE";

	public static final String SYM_KEY_ALGORITHM = "SYM_KEY_ALGORITHM";

	public static final String SYM_KEY_SIZE = "SYM_KEY_SIZE";

	public static final String ASYM_KEY_ALGORITHM = "ASYM_KEY_ALGORITHM";

	public static final String ASYM_KEY_SIZE = "ASYM_KEY_SIZE";

	public static final String CERT_SIGN_ALGORITHM = "CERT_SIGN_ALGORITHM";

	public static final String CONFIG_FILE_PATH = "CONFIG_FILE_PATH";

	public static final String PKCS11_KEYSTORE_PASSWORD = "PKCS11_KEYSTORE_PASSWORD";

	public static final String PKCS11_KS_IMPL_CLAZZ = "io.mosip.kernel.keymanager.hsm.impl.pkcs.PKCS11KeyStoreImpl";

	public static final String PKCS12_KS_IMPL_CLAZZ = "io.mosip.kernel.keymanager.hsm.impl.pkcs.PKCS12KeyStoreImpl";

	public static final String OFFLINE_KS_IMPL_CLAZZ = "io.mosip.kernel.keymanager.hsm.impl.offline.OLKeyStoreImpl";

	public static final String JCE_CLAZZ_NAME = "className";

	public static final String FLAG_KEY_REF_CACHE = "FLAG_KEY_REF_CACHE";

}
