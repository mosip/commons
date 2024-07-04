package io.mosip.kernel.core.keymanager.spi;

import io.mosip.kernel.core.keymanager.model.CertificateParameters;

/**
 * Keymanager interface to generate and store Elliptic Curve Cryptographic keys.
 * 
 * @author Mahammed Taheer
 * @since 1.2.1
 *
 */
public interface ECKeyStore extends KeyStore {

	/**
	 * Elliptic Curve Asymmetric(keypair) keys will be generated based on the provider specified and Store the keys 
	 * along with certificate in provider specific keystore. Supported Curve (secp256k1, secp256r1)
	 * 
	 * @param alias        the alias
	 * @param signKeyAlias alias used to sign the generated key
	 * @param certParams   required Certificate Parameters to create the certificate
	 * @param ecCurve 	   ECC Curve type.
	 */
	void generateAndStoreAsymmetricKey(String alias, String signKeyAlias, CertificateParameters certParams, String ecCurve);
}
