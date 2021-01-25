package io.mosip.kernel.keymanager.hsm.impl.offline;

import java.security.Key;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import io.mosip.kernel.core.keymanager.exception.KeystoreProcessingException;
import io.mosip.kernel.core.keymanager.model.CertificateParameters;
import io.mosip.kernel.keymanager.hsm.constant.KeymanagerConstant;
import io.mosip.kernel.keymanager.hsm.constant.KeymanagerErrorCode;


/**
 * Offline Keymanager implementation to support only encryption & signature verification API.
 * 
 * @author Mahammed Taheer
 * @since 1.1.4
 *
 */
public class OLKeyStoreImpl implements io.mosip.kernel.core.keymanager.spi.KeyStore {

	public OLKeyStoreImpl(Map<String, String> params) throws Exception {
        // Key Generation is not allowed in case of offline keystore.
    }
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getAllAlias()
	 */
	@Override
	public List<String> getAllAlias() {
		throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
						KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getKey(java.lang.String)
	 */
	@Override
	public Key getKey(String alias) {
		throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
						KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getAsymmetricKey(java.
	 * lang.String)
	 */
	@Override
	public PrivateKeyEntry getAsymmetricKey(String alias) {
		throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
						KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getPrivateKey(java.lang.
	 * String)
	 */
	@Override
	public PrivateKey getPrivateKey(String alias) {
        throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
                    KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getPublicKey(java.lang.
	 * String)
	 */
	@Override
	public PublicKey getPublicKey(String alias) {
		throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
						KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getCertificate(java.lang.
	 * String)
	 */
	@Override
	public X509Certificate getCertificate(String alias) {
		throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
						KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#getSymmetricKey(java.lang
	 * .String)
	 */
	@Override
	public SecretKey getSymmetricKey(String alias) {
		throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
						KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#deleteKey(java.lang.
	 * String)
	 */
	@Override
	public void deleteKey(String alias) {
		throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
						KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#storeAsymmetricKey(java.
	 * security.KeyPair, java.lang.String)
	 */
	@Override
	public void generateAndStoreAsymmetricKey(String alias, String signKeyAlias, CertificateParameters certParams) {
        throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
                    KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.keymanager.spi.SofthsmKeystore#storeSymmetricKey(javax.
	 * crypto.SecretKey, java.lang.String)
	 */
	@Override
	public void generateAndStoreSymmetricKey(String alias) {
		throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
						KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	@Override
	public void storeCertificate(String alias, PrivateKey privateKey, Certificate certificate) {
		throw new KeystoreProcessingException(KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorCode(),
						KeymanagerErrorCode.OFFLINE_KEYSTORE_ACCESS_ERROR.getErrorMessage());
	}

	@Override
	public String getKeystoreProviderName() {
		return KeymanagerConstant.KEYSTORE_TYPE_OFFLINE;
	}
}
