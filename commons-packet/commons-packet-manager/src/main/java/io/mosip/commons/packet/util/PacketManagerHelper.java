package io.mosip.commons.packet.util;

import static io.mosip.commons.packet.constants.PacketManagerConstants.CREATION_DATE;
import static io.mosip.commons.packet.constants.PacketManagerConstants.ENCRYPTED_HASH;
import static io.mosip.commons.packet.constants.PacketManagerConstants.ID;
import static io.mosip.commons.packet.constants.PacketManagerConstants.PACKET_NAME;
import static io.mosip.commons.packet.constants.PacketManagerConstants.PROCESS;
import static io.mosip.commons.packet.constants.PacketManagerConstants.PROVIDER_NAME;
import static io.mosip.commons.packet.constants.PacketManagerConstants.PROVIDER_VERSION;
import static io.mosip.commons.packet.constants.PacketManagerConstants.SCHEMA_VERSION;
import static io.mosip.commons.packet.constants.PacketManagerConstants.SIGNATURE;
import static io.mosip.commons.packet.constants.PacketManagerConstants.SOURCE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.mosip.kernel.biometrics.entities.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.kernel.biometrics.commons.CbeffValidator;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.cbeffutil.container.impl.CbeffContainerImpl;
import io.mosip.kernel.core.util.HMACUtils2;

@Component
public class PacketManagerHelper {

    /**
     * The config server file storage URL.
     */
    @Value("${mosip.kernel.xsdstorage-uri}")
    private String configServerFileStorageURL;

    /*
     * XSD file name
     */

    /**
     * The schema name.
     */
    @Value("${mosip.kernel.xsdfile}")
    private String schemaName;


    public byte[] getXMLData(BiometricRecord biometricRecord, boolean offlineMode) throws Exception {
        try (InputStream xsd = (offlineMode) ?
                getClass().getClassLoader().getResourceAsStream(PacketManagerConstants.CBEFF_SCHEMA_FILE_PATH) :
                new URL(configServerFileStorageURL + schemaName).openStream()) {
            CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
            BIR bir = cbeffContainer.createBIRType(biometricRecord.getSegments());
            List<Entry> entries = new ArrayList<>();
            biometricRecord.getOthers().forEach((k, v) -> {
                entries.add(new Entry(k, v));
            });
            bir.setOthers(entries);
            return CbeffValidator.createXMLBytes(bir, IOUtils.toByteArray(xsd));
        }
    }

    public static byte[] generateHash(List<String> order, Map<String, byte[]> data) throws IOException, NoSuchAlgorithmException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (order != null && !order.isEmpty()) {
            for (String name : order) {
                outputStream.write(data.get(name));
            }
            return HMACUtils2.digestAsPlainText(outputStream.toByteArray()).getBytes();
        }
        return null;
    }

    public static Map<String, Object> getMetaMap(PacketInfo packetInfo) {
        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put(ID, packetInfo.getId());
        metaMap.put(PACKET_NAME, packetInfo.getPacketName());
        metaMap.put(SOURCE, packetInfo.getSource());
        metaMap.put(PROCESS, packetInfo.getProcess());
        metaMap.put(SCHEMA_VERSION, packetInfo.getSchemaVersion());
        metaMap.put(SIGNATURE, packetInfo.getSignature());
        metaMap.put(ENCRYPTED_HASH, packetInfo.getEncryptedHash());
        metaMap.put(PROVIDER_NAME, packetInfo.getProviderName());
        metaMap.put(PROVIDER_VERSION, packetInfo.getProviderVersion());
        metaMap.put(CREATION_DATE, packetInfo.getCreationDate());
        return metaMap;
    }

    public static PacketInfo getPacketInfo(Map<String, Object> metaMap) {
        PacketInfo packetInfo = new PacketInfo();
        packetInfo.setId((String) metaMap.get(ID));
        packetInfo.setPacketName((String) metaMap.get(PACKET_NAME));
        packetInfo.setSource((String) metaMap.get(SOURCE));
        packetInfo.setProcess((String) metaMap.get(PROCESS));
        packetInfo.setSchemaVersion((String) metaMap.get(SCHEMA_VERSION));
        packetInfo.setSignature((String) metaMap.get(SIGNATURE));
        packetInfo.setEncryptedHash((String) metaMap.get(ENCRYPTED_HASH));
        packetInfo.setProviderName((String) metaMap.get(PROVIDER_NAME));
        packetInfo.setProviderVersion((String) metaMap.get(PROVIDER_VERSION));
        packetInfo.setCreationDate((String) metaMap.get(CREATION_DATE));
        return packetInfo;
    }

}
