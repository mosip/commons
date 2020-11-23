package io.mosip.commons.packet.spi;

import io.mosip.commons.packet.dto.Document;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * The packet reader interface
 */
@Service
public interface IPacketReader {

    public boolean validatePacket(String id, String source, String process);

    public Map<String, Object> getAll(String id, String source, String process);

    public String getField(String id, String field, String source, String process);

    public Map<String, String> getFields(String id, List<String> fields, String source, String process);

    public Document getDocument(String id, String documentName, String source, String process);

    public BiometricRecord getBiometric(String id, String biometricSchemaField, List<String> modalities, String source, String process);

    public Map<String, String> getMetaInfo(String id, String source, String process);

    public List<Map<String, String>> getAuditInfo(String id, String source, String process);
}
