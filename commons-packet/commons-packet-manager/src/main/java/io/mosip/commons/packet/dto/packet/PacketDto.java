package io.mosip.commons.packet.dto.packet;

import io.mosip.commons.packet.dto.Document;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode
public class PacketDto {

    private String id;
    private String process;
    private String source;
    private String schemaVersion;
    private String schemaJson;
    private Map<String, String> fields;
    private Map<String, String> metaInfo;
    private Map<String, Document> documents;
    private List<Map<String, String>> audits;
    private Map<String, BiometricRecord> biometrics;



}
