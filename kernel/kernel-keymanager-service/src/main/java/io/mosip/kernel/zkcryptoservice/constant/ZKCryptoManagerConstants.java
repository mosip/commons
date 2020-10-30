package io.mosip.kernel.zkcryptoservice.constant;

/**
 * Constants for Zero Knowledge Crypto Manager.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */
public interface ZKCryptoManagerConstants {
    
	int GCM_NONCE_LENGTH = 12;

	int GCM_AAD_LENGTH = 32;

	int GCM_TAG_LENGTH = 16;

	int INT_BYTES_LEN = 4;

	int GCM_NONCE_PLUS_INT_BYTES_LEN = INT_BYTES_LEN + GCM_NONCE_LENGTH;

	int GCM_NONCE_PLUS_INT_BYTES_PLUS_GCM_AAD_LEN = INT_BYTES_LEN + GCM_NONCE_LENGTH + GCM_AAD_LENGTH;
	
	String ACTIVE_STATUS = "Active";

	String SESSIONID = "zkSessionID";

	String ZK_ENCRYPT = "zkEncrypt";

	String ZK_DECRYPT = "zkDecrypt";

	String RANDOM_KEY = "RandomKey";

	String MASTER_KEY = "MasterKey";

	String EMPTY = "";

	String MASTER_CURRENT_ALIAS = "ZKMasterKeyAlias";

	String DATA_CIPHER = "DataCipher";

	String DERIVE_KEY = "DeriveKey";

	String HASH_ALGO = "SHA-256";

	String ENCRYPT_RANDOM_KEY = "EncryptRandomKey";

	String RE_ENCRYPT_RANDOM_KEY = "Re-EncryptRandomKey";

}