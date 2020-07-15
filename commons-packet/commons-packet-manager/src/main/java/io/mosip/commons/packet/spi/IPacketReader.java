package io.mosip.commons.packet.spi;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.ProviderInfo;
import io.mosip.commons.packet.exception.PacketReaderException;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;

import java.util.List;
import java.util.Map;

/**
 * The packet reader interface
 *
 */
public interface IPacketReader {

    public ProviderInfo init(String schemaUrl, byte[] publicKey, PacketSigner signer);

    public boolean validatePacket(String id, String process);

    public Map<String, Object> getAll(String id, String process);

    public String getField(String id, String field, String process, boolean bypassCache);

    public Map<String, String> getFields(String id, List<String> fields, String process, boolean bypassCache);

    public Document getDocument(String id, String documentName, String process)throws PacketReaderException ;

    public BiometricRecord getBiometric(String id, String person, List<BiometricType> modalities, String process);

    public Map<String, String> getMetaInfo(String id, String source, String process, boolean bypassCache);
}
