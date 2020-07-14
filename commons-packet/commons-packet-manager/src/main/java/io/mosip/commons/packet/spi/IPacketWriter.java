package io.mosip.commons.packet.spi;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.ProviderInfo;
import io.mosip.kernel.biometrics.entities.BiometricRecord;

/**
 * The packet Writer interface
 *
 */
public interface IPacketWriter {

    public ProviderInfo init(String schemaUrl, byte[] publicKey, PacketSigner signer);

    public boolean setField(String fieldName, String value);

    public boolean setBiometric(String fieldName, BiometricRecord biometricRecord);

    public boolean setDocument(String documentName, Document document);

    public boolean setMetaInfo(String key, String value);

    public PacketInfo persistPacket();
}
