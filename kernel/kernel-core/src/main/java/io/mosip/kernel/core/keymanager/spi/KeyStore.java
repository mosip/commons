package io.mosip.kernel.core.keymanager.spi;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.time.LocalDateTime;
import java.util.List;

import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

import io.mosip.kernel.core.keymanager.model.CertificateParameters;

/**
 * Keymanager interface that handles and stores its cryptographic keys.
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public interface KeyStore {

	/**
	 * Get private key from keystore
	 * 
	 * @param alias the alias
	 * @return The private key
	 */
	PrivateKey getPrivateKey(String alias);

	/**
	 * Get public key from keystore
	 * 
	 * @param alias the alias
	 * @return The public key
	 */
	PublicKey getPublicKey(String alias);

	/**
	 * Get certificate from keystore
	 * 
	 * @param alias the alias
	 * @return The certificate
	 */
	Certificate getCertificate(String alias);

	/**
	 * Get Symmetric key from keystore
	 * 
	 * @param alias the alias
	 * @return The Symmetric key
	 */
	SecretKey getSymmetricKey(String alias);

	/**
	 * Get Asymmetric key from keystore
	 * 
	 * @param alias the alias
	 * @return The asymmetric key
	 */
	PrivateKeyEntry getAsymmetricKey(String alias);

	/**
	 * Lists all the alias names of this keystore.
	 * 
	 * @return list of all alias in keystore
	 */
	List<String> getAllAlias();

	/**
	 * Returns the key associated with the given alias, using the given password to
	 * recover it. The key must have been associated with the alias by a call to
	 * setKeyEntry, or by a call to setEntry with a PrivateKeyEntry or
	 * SecretKeyEntry.
	 * 
	 * @param alias the alias
	 * @return the requested key, or null if the given alias does not exist or does
	 *         not identify a key-related entry
	 */
	Key getKey(String alias);

	/**
	 * Symmetric key will be generated based on the provider specified and Store the key in provider specific keystore
	 * 
	 * @param secretKey the secret key
	 * @param alias     the alias
	 */
	void generateAndStoreSymmetricKey(String alias);

	/**
	 * Asymmetric(keypair) keys will be generated based on the provider specified and Store the keys 
	 * along with self-signed certificate in provider specific keystore
	 * 
	 * @param alias        the alias
	 * @param signKeyAlias alias used to sign the generated key
	 * @param certParams   required Certificate Parameters to create the certificate
	 */
	void generateAndStoreAsymmetricKey(String alias, String signKeyAlias, CertificateParameters certParams);

	/**
	 * Delete key form keystore
	 * 
	 * @param alias the alias
	 */
	void deleteKey(String alias);

	//void storeCertificate(String alias, Certificate[] chain, PrivateKey privateKey);

	/**
	 * Stores the given trusted certificate to the given alias
	 * 
	 * @param alias        the alias
	 * @param privateKey   privateKey reference of the provided certificate
	 * @param certificate  Certificate to be stored
	 */
	void storeCertificate(String alias, PrivateKey privateKey, Certificate certificate);

	/**
	 * Gets the keyStore provider name.
	 * 
	 */
	String getKeystoreProviderName();
}
