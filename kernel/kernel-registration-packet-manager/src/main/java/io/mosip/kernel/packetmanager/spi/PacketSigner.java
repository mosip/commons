package io.mosip.kernel.packetmanager.spi;

public interface PacketSigner {
	
	public byte[] signZip(byte[] data);

}
