package io.mosip.kernel.core.signatureutil.spi;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import io.mosip.kernel.core.signatureutil.model.SignatureResponse;

/**
 * Signs arbitrary data and verifies signatures via kernel keymanager.
 * <p>
 * Contract: implementations typically call keymanager over HTTP. {@code data}
 * and {@code signature} must be non-null. Call when producing or checking
 * MOSIP response signatures.
 * </p>
 *
 * @author Srinivasan
 * @author Urvil
 * @since 1.0.0
 */
public interface SignatureUtil {

	/**
	 * Signs {@code data} with the configured kernel signing key.
	 *
	 * @param data never-null payload to sign
	 * @return never-null signature and timestamp
	 */
	public SignatureResponse sign(String data);

	/**
	 * Verifies {@code signature} over {@code data} using {@code publickey}.
	 *
	 * @param signature never-null signature bytes encoded as text
	 * @param data      never-null original payload
	 * @param publickey never-null Base64-encoded public key
	 * @return {@code true} if the signature is valid
	 * @throws InvalidKeySpecException  when {@code publickey} cannot be parsed
	 * @throws NoSuchAlgorithmException when the signature algorithm is unavailable
	 */
	public boolean validateWithPublicKey(String signature, String data, String publickey)
			throws InvalidKeySpecException, NoSuchAlgorithmException;

	/**
	 * Verifies {@code signature} over {@code data} using the key valid at {@code timestamp}.
	 *
	 * @param signature never-null signature bytes encoded as text
	 * @param data      never-null original payload
	 * @param timestamp never-null sign time used to resolve the public key
	 * @return {@code true} if the signature is valid
	 */
	public boolean validate(String signature, String data, String timestamp);
}
