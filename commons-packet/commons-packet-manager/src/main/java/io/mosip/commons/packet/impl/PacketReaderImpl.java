package io.mosip.commons.packet.impl;

import io.mosip.commons.packet.PacketKeeper;
import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PacketReaderImpl implements IPacketReader {

    @Autowired
    private PacketKeeper keeper;

    public Map<String, Object> getAll(String id, final String source, final String process) {
        return null;
    }

    public String getField(String id, String field, String source, String process) {
        List<PacketInfo> packetInfos = keeper.getManifest(id).getPacketInfos();
        PacketInfo finalPacketInfo = packetInfos.stream().filter(
                p -> p.getSource().equalsIgnoreCase(source) && p.getProcess().equalsIgnoreCase(process)).iterator().next();

        Packet packet = keeper.getPacket(finalPacketInfo);

        // decrypt packet, get idobject and send as keyvalue pair
        return null;
    }

    public Map<String, String> getFields(String id, List<String> fields, String source, String process) {
        return null;
    }

    public Document getDocument(String id, String documentName, String source, String process) {
        return null;
    }

    public BiometricRecord getBiometric(String id, String person, List<BiometricType> modalities, String source, String process) {
        return null;
    }

    public Map<String, String> getMetaInfo(String id, String source, String process) {
        return null;
    }
}
