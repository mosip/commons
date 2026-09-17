package io.mosip.kernel.core.idgenerator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when Virtual ID (VID) generation or assignment
 * fails.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.idgenerator.spi.VidGenerator}
 * when the unused-VID pool is empty or persistence fails.
 * </p>
 *
 * @see io.mosip.kernel.core.idgenerator.spi.VidGenerator
 * @author M1043226
 * @since 1.0.0
 */
public class VidGenerationFailedException extends BaseUncheckedException {
	/**
	 * The generated serial version id
	 */
	private static final long serialVersionUID = -6990502141757024297L;

	/**
	 * Constructs a VID-generation exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public VidGenerationFailedException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
