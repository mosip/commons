package io.mosip.kernel.core.packetuploader.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when the packet upload source path is empty.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.packetuploader.spi.PacketUploader}
 * when {@code source} is blank. Does not perform I/O.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class EmptyPathException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = -4601559589099809931L;

	/**
	 * Constructor with errorCode and errorMessage
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public EmptyPathException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
