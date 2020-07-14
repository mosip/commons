package io.mosip.commons.packet.spi;

public interface PacketSigner {
	
	public byte[] signZip(byte[] data);

}
