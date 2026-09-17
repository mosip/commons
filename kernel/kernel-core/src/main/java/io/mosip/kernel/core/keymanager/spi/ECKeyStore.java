package io.mosip.kernel.core.keymanager.spi;

import io.mosip.kernel.core.keymanager.model.CertificateParameters;

/**
 * Extends {@link KeyStore} with elliptic-curve key generation.
 * <p>
 * Contract: {@code ecCurve} must be {@code secp256k1} or {@code secp256r1}.
 * Aliases must be non-blank. Call from kernel keymanager when EC keys are
 * required.
 * </p>
 *
 * @author Mahammed Taheer
 * @since 1.2.1
 */
public interface ECKeyStore extends KeyStore {

	/**
	 * Generates an EC key pair for {@code ecCurve}, signs it, and stores it.
	 *
	 * @param alias        never-null, never-blank alias to create
	 * @param signKeyAlias never-null alias of the signing key
	 * @param certParams   never-null certificate subject and validity
	 * @param ecCurve      never-null curve name ({@code secp256k1} or
	 *                     {@code secp256r1})
	 */
	void generateAndStoreAsymmetricKey(String alias, String signKeyAlias, CertificateParameters certParams, String ecCurve);
}
