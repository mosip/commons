package io.mosip.kernel.core.idgenerator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when token-ID generation fails.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.idgenerator.spi.TokenIdGenerator}
 * on algorithm, checksum, or configuration errors.
 * </p>
 *
 * @see io.mosip.kernel.core.idgenerator.spi.TokenIdGenerator
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public class TokenIdGeneratorException extends BaseUncheckedException {

	/**
	 * Serial version ID.
	 */
	private static final long serialVersionUID = -7905208050229631306L;

	/**
	 * Constructs a token-ID generation exception with MOSIP error code and
	 * message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public TokenIdGeneratorException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
