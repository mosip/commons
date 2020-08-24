package io.mosip.commons.packet.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

public class PacketCreatorException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 80279436742398851L;
	
	public PacketCreatorException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
