package io.mosip.kernel.core.keymanager.model;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Private key plus certificate chain as retrieved from a software HSM /
 * SoftHSM keystore.
 * <p>
 * Contract: treat {@code privateKey} as sensitive; do not log it. {@code chain}
 * may be null or empty. Does not perform I/O.
 * </p>
 *
 * @param <C> Certificate Type
 * @param <P> PrivateKey Type
 * @author Urvil Joshi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateEntry<C, P> {

	/**
	 * Certificate chain leaf-first; may be null or empty.
	 */
	private C[] chain;

	/**
	 * Private key matching the leaf certificate; sensitive; may be null.
	 */
	private P privateKey;

	/**
	 * Returns a diagnostic string including the chain and private key.
	 *
	 * @return never-null string; may expose key material — do not log in
	 *         production
	 */
	@Override
	public String toString() {
		return "CertificateEntry [chain=" + Arrays.toString(chain) + ", privateKey=" + privateKey + "]";
	}

}
