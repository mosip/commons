package io.mosip.kernel.core.crypto.spi;

/**
 * JSON Web Signature (JWS) sign and verify operations.
 * <p>
 * Contract: deprecated since 1.0.5; new callers must use
 * {@link CryptoCoreSpec#sign(Object, Object)} and
 * {@link CryptoCoreSpec#verifySignature(Object, Object, Object)}.
 * Implementations perform local crypto (no HTTP). Payload, key, and
 * certificate must be non-null when signing.
 * </p>
 *
 * @author Rajath
 * @since 1.0.0
 * @deprecated since 1.0.5; use {@link CryptoCoreSpec} instead
 *
 * @param <R> signed output type
 * @param <D> payload / signature input type
 * @param <C> certificate type
 * @param <P> private-key type
 */
@Deprecated
public interface JwsSpec<R, D, C, P> {

	/**
	 * Signs {@code payload} with {@code pKey} and embeds {@code cert} in the JWS.
	 *
	 * @param payload never-null data to sign
	 * @param pKey    never-null private key
	 * @param cert    never-null signer certificate
	 * @return never-null JWS compact or serialized form
	 */
	R jwsSign(D payload, P pKey, C cert);

	/**
	 * Verifies a JWS signature.
	 *
	 * @param sign never-null signed payload or compact JWS
	 * @return {@code true} if the signature is valid
	 */
	boolean verifySignature(D sign);

}
