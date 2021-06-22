package io.mosip.commons.packet.spi;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.kernel.biometrics.entities.BiometricRecord;

/**
 * The packet Writer interface
 */
@Service
public interface IPacketWriter {

    public void setField(String id, String fieldName, String value);

    public void setFields(String id, Map<String, String> fields);

    public void setBiometric(String id, String fieldName, BiometricRecord biometricRecord);

    public void setDocument(String id, String documentName, Document document);

    public void addMetaInfo(String id, Map<String, String> metaInfo);

    public void addMetaInfo(String id, String key, String value);

    public void addAudits(String id, List<Map<String, String>> audits);

    public void addAudit(String id, Map<String, String> audit);

    public List<PacketInfo> persistPacket(String id, String version, String schemaJson, String source,
                                          String process, String additionalInfoReqId, String refId, boolean offlineMode);
}
