package io.mosip.commons.packet.spi;

public interface IPacketCryptoHelper {

    public byte[] sign(byte[] packet);

    public byte[] encrypt(byte[] packet);

    public byte[] decrypt(byte[] packet);

    public boolean verify(byte[] packet);
}
