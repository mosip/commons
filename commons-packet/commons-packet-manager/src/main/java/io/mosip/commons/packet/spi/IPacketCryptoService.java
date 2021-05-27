package io.mosip.commons.packet.spi;

public interface IPacketCryptoService {

    public byte[] sign(byte[] packet);

    public byte[] encrypt(String id, byte[] packet);

    public byte[] decrypt(String id, byte[] packet);

    public boolean verify(String machineId, byte[] packet, byte[] signature);
}
