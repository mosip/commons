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
 * Stores and retrieves MOSIP cryptographic keys from a JCA / HSM keystore.
 * <p>
 * Contract: implementations access a PKCS#11 or software keystore. Aliases
 * must be non-blank. Missing aliases throw
 * {@link io.mosip.kernel.core.keymanager.exception.NoSuchAliasException}.
 * Private keys and secrets are sensitive; do not log them. Call from kernel
 * keymanager.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public interface KeyStore {

	/**
	 * Returns the private key for {@code alias}.
	 *
	 * @param alias never-null, never-blank keystore alias
	 * @return never-null private key
	 * @throws io.mosip.kernel.core.keymanager.exception.NoSuchAliasException when
	 *                                                                         the
	 *                                                                         alias
	 *                                                                         is
	 *                                                                         missing
	 */
	PrivateKey getPrivateKey(String alias);

	/**
	 * Returns the public key for {@code alias}.
	 *
	 * @param alias never-null, never-blank keystore alias
	 * @return never-null public key
	 * @throws io.mosip.kernel.core.keymanager.exception.NoSuchAliasException when
	 *                                                                         the
	 *                                                                         alias
	 *                                                                         is
	 *                                                                         missing
	 */
	PublicKey getPublicKey(String alias);

	/**
	 * Returns the certificate for {@code alias}.
	 *
	 * @param alias never-null, never-blank keystore alias
	 * @return never-null certificate
	 * @throws io.mosip.kernel.core.keymanager.exception.NoSuchAliasException when
	 *                                                                         the
	 *                                                                         alias
	 *                                                                         is
	 *                                                                         missing
	 */
	Certificate getCertificate(String alias);

	/**
	 * Returns the secret key for {@code alias}.
	 *
	 * @param alias never-null, never-blank keystore alias
	 * @return never-null symmetric key
	 * @throws io.mosip.kernel.core.keymanager.exception.NoSuchAliasException when
	 *                                                                         the
	 *                                                                         alias
	 *                                                                         is
	 *                                                                         missing
	 */
	SecretKey getSymmetricKey(String alias);

	/**
	 * Returns the private-key entry (key plus certificate chain) for {@code alias}.
	 *
	 * @param alias never-null, never-blank keystore alias
	 * @return never-null private-key entry
	 * @throws io.mosip.kernel.core.keymanager.exception.NoSuchAliasException when
	 *                                                                         the
	 *                                                                         alias
	 *                                                                         is
	 *                                                                         missing
	 */
	PrivateKeyEntry getAsymmetricKey(String alias);

	/**
	 * Lists every alias in the keystore.
	 *
	 * @return never-null list; may be empty
	 */
	List<String> getAllAlias();

	/**
	 * Returns the key associated with {@code alias}, or null if none exists.
	 *
	 * @param alias never-null, never-blank keystore alias
	 * @return key, or null if the alias is absent or is not a key entry
	 */
	Key getKey(String alias);

	/**
	 * Generates a symmetric key and stores it under {@code alias}.
	 *
	 * @param alias never-null, never-blank alias to create
	 */
	void generateAndStoreSymmetricKey(String alias);

	/**
	 * Generates an asymmetric key pair, signs it with {@code signKeyAlias}, and
	 * stores it under {@code alias}.
	 *
	 * @param alias        never-null, never-blank alias to create
	 * @param signKeyAlias never-null alias of the signing key
	 * @param certParams   never-null certificate subject and validity
	 */
	void generateAndStoreAsymmetricKey(String alias, String signKeyAlias, CertificateParameters certParams);

	/**
	 * Deletes the key entry for {@code alias}.
	 *
	 * @param alias never-null, never-blank alias to remove
	 */
	void deleteKey(String alias);

	//void storeCertificate(String alias, Certificate[] chain, PrivateKey privateKey);

	/**
	 * Stores a trusted certificate and optional private key under {@code alias}.
	 *
	 * @param alias       never-null, never-blank alias
	 * @param privateKey  private key matching the certificate; may be null for
	 *                    trusted-cert-only entries
	 * @param certificate never-null certificate to store
	 */
	void storeCertificate(String alias, PrivateKey privateKey, Certificate certificate);

	/**
	 * Returns the JCA provider name backing this keystore.
	 *
	 * @return never-null provider name such as {@code SunJCE} or PKCS#11 name
	 */
	String getKeystoreProviderName();
}
