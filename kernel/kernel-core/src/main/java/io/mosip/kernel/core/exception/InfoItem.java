package io.mosip.kernel.core.exception;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Single MOSIP error-code / error-text pair stored on
 * {@link BaseUncheckedException} and {@link BaseCheckedException}.
 * <p>
 * Contract: package-private entity; fields may be null until populated.
 * Serializes with the parent exception. Does not perform I/O.
 * </p>
 *
 * @author Shashank Agrawal
 * @since 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
class InfoItem implements Serializable {

	private static final long serialVersionUID = -779695043380592601L;

	/**
	 * MOSIP error code such as {@code KER-UTL-001}; may be null.
	 */
	@Getter
	@Setter
	public String errorCode = null;

	/**
	 * Human-readable error text; may be null or empty.
	 */
	@Getter
	@Setter
	public String errorText = null;

}
