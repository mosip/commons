package io.mosip.kernel.partnercertservice.constant;

/**
 * Constants for Partner Certificate Manager
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */

public interface PartnerCertManagerConstants {
    
    /**
	 * The constant Session Id.
	 */
	String SESSIONID = "pcSessionId";

	/**
	 * The constant EMPTY
	 */
	String EMPTY = "";
	
	/**
	 * The constant EQUALS
	 */
	String 	EQUALS = "=";
	
	/**
	 * The constant COMMA
	 */
    String COMMA = ",";
    
    /**
	 * The constant UPLOAD_CA_CERT
	 */
	String UPLOAD_CA_CERT = "UploadCACertificate";

	/**
	 * The constant PCM_UTIL
	 */
	String PCM_UTIL = "pcmUtil";

	/**
	 * The constant TRUST_ROOT
	 */
	String TRUST_ROOT = "TrustRoot";

	/**
	 * The constant TRUST_INTER
	 */
	String TRUST_INTER = "TrustInter";

	/**
	 * The constant SUCCESS_UPLOAD
	 */
	String SUCCESS_UPLOAD = "Upload Success.";

	/**
	 * The constant PARTIAL_SUCCESS_UPLOAD
	 */
	String PARTIAL_SUCCESS_UPLOAD = "Partial Upload Success.";

	/**
	 * The constant UPLOAD_FAILED
	 */
	String UPLOAD_FAILED = "Upload Failed.";

	 /**
	 * The constant UPLOAD_PARTNER_CERT
	 */
	String UPLOAD_PARTNER_CERT = "UploadPartnerCertificate";

	/**
	 * The constant RSA_ALGORITHM
	 */
	String RSA_ALGORITHM = "RSA";

	/**
	 * The constant RSA_MIN_KEY_SIZE
	 */
	int RSA_MIN_KEY_SIZE = 2048;

	/**
	 * The constant HASH_SHA2
	 */
	String HASH_SHA2 = "SHA2";

	int YEAR_DAYS = 365;

    String GET_PARTNER_CERT = "GetPartnerCertificate";

	String CERT_TRUST_VALIDATION = "CertTrustPathValidation";

	String FTM_PARTNER_DOMAIN = "FTM";

	String ROOT_APP_ID = "ROOT";

	String AUTH_DOMAIN = "AUTH";
}
