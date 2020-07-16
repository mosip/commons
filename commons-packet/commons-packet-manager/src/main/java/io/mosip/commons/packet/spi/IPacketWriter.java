package io.mosip.commons.packet.spi;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.packet.AuditDto;
import io.mosip.commons.packet.exception.PacketCreatorException;
import io.mosip.commons.packet.impl.PacketSigner;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The packet Writer interface
 *
 */
@Service
public interface IPacketWriter {

    public void initialize(String source, String process);

    public void setField(String fieldName, String value);

    public void setBiometric(String fieldName, BiometricRecord biometricRecord);

    public void setDocument(String documentName, Document document);

    public void setMetaInfo(String key, String value);

    public PacketInfo persistPacket();

    public void setAudits(List<AuditDto> auditList);

    public byte[] createPacket(String registrationId, double version, String schemaJson,
                               byte[] publicKey, PacketSigner signer) throws PacketCreatorException;
}
