package io.mosip.kernel.crypto.jce.util;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.core.crypto.spi.JwsSpec;

/**
 * 
 * @author M1037717 This class will verify and sign the JWT
 * 
 * @deprecated(This class is deprecated from version 1.0.5, Please use
 *                  {@link CryptoCoreSpec#sign(Object, Object)} and
 *                  {@link CryptoCoreSpec#verifySignature(Object, Object, Object)}
 *                  instead of these methods)
 *
 */
@Deprecated
@Component
public class JWSValidation implements JwsSpec<String, String, X509Certificate, PrivateKey> {

	/** The public key. */
	protected PublicKey publicKey;

	/**
	 * 
	 * @param pKey
	 * @param certificate
	 * @param payload
	 * @return signature
	 * @throws JoseException
	 */
	@Override
	public String jwsSign(String payload, PrivateKey pKey, X509Certificate certificate) {
		try {
			JsonWebSignature jws = new JsonWebSignature();
			List<X509Certificate> certList = new ArrayList<>();
			certList.add(certificate);
			X509Certificate[] certArray = certList.toArray(new X509Certificate[] {});
			jws.setCertificateChainHeaderValue(certArray);
			jws.setPayload(payload);
			jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
			jws.setKey(pKey);
			jws.setDoKeyValidation(false);
			return jws.getCompactSerialization();
		} catch (JoseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param sign
	 * @return boolean
	 */
	@Override
	public boolean verifySignature(String sign) {
		try {
			JsonWebSignature jws = new JsonWebSignature();
			jws.setCompactSerialization(sign);
			List<X509Certificate> certificateChainHeaderValue = jws.getCertificateChainHeaderValue();
			X509Certificate certificate = certificateChainHeaderValue.get(0);
			certificate.checkValidity();
			publicKey = certificate.getPublicKey();
			// certificate.verify(publicKey);
			jws.setKey(publicKey);
			return jws.verifySignature();
		} catch (CertificateException | JoseException e) {
			e.printStackTrace();
		}
		return false;

	}

}
