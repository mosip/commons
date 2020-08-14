package io.mosip.commons.packet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class PacketInfo {

    private String id;
    private String packetName;
    private String source;
    private String process;
    private String schemaVersion;
    private String signature;
    private String encryptedHash;
    private String providerName;
    private String providerVersion;
    private String creationDate;
}
