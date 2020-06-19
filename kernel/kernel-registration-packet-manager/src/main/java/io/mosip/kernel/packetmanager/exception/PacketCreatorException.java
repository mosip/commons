package io.mosip.kernel.packetmanager.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

public class PacketCreatorException extends BaseCheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 80279436742398851L;
	
	public PacketCreatorException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
