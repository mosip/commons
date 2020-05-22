package io.mosip.kernel.packetmanager.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

public class PacketReaderException extends BaseCheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6871322997948782180L;
	
	public PacketReaderException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
