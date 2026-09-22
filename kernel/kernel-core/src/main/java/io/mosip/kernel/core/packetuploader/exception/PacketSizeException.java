package io.mosip.kernel.core.packetuploader.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when the packet file size is outside allowed bounds.
 * <p>
 * Contract: raised before upload when the local file is empty or exceeds the
 * configured maximum size.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class PacketSizeException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 3585613514626311385L;

	/**
	 * Constructor with errorCode and errorMessage
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public PacketSizeException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
