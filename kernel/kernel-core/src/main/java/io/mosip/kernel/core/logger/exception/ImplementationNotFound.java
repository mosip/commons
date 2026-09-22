/*
 * 
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.core.logger.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a MOSIP logger implementation class cannot
 * be loaded.
 * <p>
 * Contract: raised when {@code logger.implementation} (or equivalent) does
 * not resolve to a {@link io.mosip.kernel.core.logger.spi.Logger}.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class ImplementationNotFound extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 105555533L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public ImplementationNotFound(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
