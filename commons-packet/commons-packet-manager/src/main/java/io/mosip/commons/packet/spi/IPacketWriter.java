package io.mosip.commons.packet.spi;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.packet.AuditDto;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import org.json.JSONException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The packet Writer interface
 *
 */
@Service
public interface IPacketWriter {

    public void setField(String id, String fieldName, String value) throws JSONException;

    public void setFields(String id, Map<String, String> fields) throws JSONException;

    public void setBiometric(String id, String fieldName, BiometricRecord biometricRecord);

    public void setDocument(String id, String documentName, Document document);

    public void setMetaInfo(String id, Map<String, String> metaInfo);

    public void setAudits(String id, List<AuditDto> audits);

    public List<PacketInfo> persistPacket(String id, double version, String schemaJson, String source, String process, boolean offlineMode);
}
