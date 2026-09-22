package io.mosip.kernel.core.signatureutil.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a kernel signing call: signature payload and sign timestamp.
 * <p>
 * Contract: returned by
 * {@link io.mosip.kernel.core.signatureutil.spi.SignatureUtil#sign(String)}.
 * {@code data} is the signature (not the original plaintext). Does not perform
 * I/O.
 * </p>
 *
 * @author Srinivasan
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignatureResponse {

	/**
	 * Signature value returned by keymanager; may be null if signing failed.
	 */
	private String data;

	/**
	 * Server time at which the signature was produced; may be null.
	 */
	private LocalDateTime timestamp;
}
