package io.mosip.commons.packet.util;

import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.cbeffutil.container.impl.CbeffContainerImpl;
import io.mosip.kernel.core.cbeffutil.common.CbeffValidator;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.kernel.core.util.HMACUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        byte[] xmlBytes = null;
        if (offlineMode) {
            try (InputStream xsd = getClass().getClassLoader().getResourceAsStream(PacketManagerConstants.CBEFF_SCHEMA_FILE_PATH)) {
                CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
                BIRType bir = cbeffContainer.createBIRType(biometricRecord.getSegments());
                xmlBytes = CbeffValidator.createXMLBytes(bir, IOUtils.toByteArray(xsd));
            }
        } else {
            try (InputStream xsd = new URL(configServerFileStorageURL + schemaName).openStream()) {
                CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
                BIRType bir = cbeffContainer.createBIRType(biometricRecord.getSegments());
                xmlBytes = CbeffValidator.createXMLBytes(bir, IOUtils.toByteArray(xsd));
            }
        }
        return xmlBytes;
    }

    public static byte[] generateHash(List<String> order, Map<String, byte[]> data) {
        if (order != null && !order.isEmpty()) {
            for (String name : order) {
                HMACUtils.update(data.get(name));
            }
            return HMACUtils.digestAsPlainText(HMACUtils.updatedHash()).getBytes();
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
