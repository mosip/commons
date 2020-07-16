package io.mosip.commons.packet.impl;

import org.springframework.stereotype.Service;

@Service
public interface PacketSigner {
	
	public byte[] signZip(byte[] data);

}
