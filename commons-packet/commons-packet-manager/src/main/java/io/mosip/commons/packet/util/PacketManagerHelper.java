package io.mosip.commons.packet.util;

import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.cbeffutil.container.impl.CbeffContainerImpl;
import io.mosip.kernel.core.cbeffutil.common.CbeffValidator;
import io.mosip.kernel.core.cbeffutil.entity.BDBInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.entity.BIRInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIRVersion;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.*;
import io.mosip.kernel.core.util.HMACUtils2;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
                List<BIR> birList = new ArrayList<>();
                biometricRecord.getSegments().forEach(s -> birList.add(convertToBIR(s)));
                BIRType bir = cbeffContainer.createBIRType(birList);
                xmlBytes = CbeffValidator.createXMLBytes(bir, IOUtils.toByteArray(xsd));
            }
        } else {
            try (InputStream xsd = new URL(configServerFileStorageURL + schemaName).openStream()) {
                CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
                List<BIR> birList = new ArrayList<>();
                biometricRecord.getSegments().forEach(s -> birList.add(convertToBIR(s)));
                BIRType bir = cbeffContainer.createBIRType(birList);
                xmlBytes = CbeffValidator.createXMLBytes(bir, IOUtils.toByteArray(xsd));
            }
        }
        return xmlBytes;
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

    public static io.mosip.kernel.biometrics.entities.BIR convertToBiometricRecordBIR(BIR bir) {
        List<BiometricType> bioTypes = new ArrayList<>();
        for(SingleType type : bir.getBdbInfo().getType()) {
            bioTypes.add(BiometricType.fromValue(type.value()));
        }

        io.mosip.kernel.biometrics.entities.RegistryIDType format = new io.mosip.kernel.biometrics.entities.RegistryIDType(bir.getBdbInfo().getFormat().getOrganization(),
                bir.getBdbInfo().getFormat().getType());

        io.mosip.kernel.biometrics.constant.QualityType qualityType;

        if(Objects.nonNull(bir.getBdbInfo().getQuality())) {
            io.mosip.kernel.biometrics.entities.RegistryIDType birAlgorithm = bir.getBdbInfo().getQuality()
                    .getAlgorithm() == null ? null : new io.mosip.kernel.biometrics.entities.RegistryIDType(
                    bir.getBdbInfo().getQuality().getAlgorithm().getOrganization(),
                    bir.getBdbInfo().getQuality().getAlgorithm().getType());

            qualityType = new io.mosip.kernel.biometrics.constant.QualityType();
            qualityType.setAlgorithm(birAlgorithm);
            qualityType.setQualityCalculationFailed(bir.getBdbInfo().getQuality().getQualityCalculationFailed());
            qualityType.setScore(bir.getBdbInfo().getQuality().getScore());

        } else {
            qualityType = null;
        }

        io.mosip.kernel.biometrics.entities.VersionType version;
        if(Objects.nonNull(bir.getVersion())) {
            version = new io.mosip.kernel.biometrics.entities.VersionType(bir.getVersion().getMajor(),
                    bir.getVersion().getMinor());
        } else {
            version = null;
        }

        io.mosip.kernel.biometrics.entities.VersionType cbeffversion;
        if(Objects.nonNull(bir.getCbeffversion())) {
            cbeffversion = new io.mosip.kernel.biometrics.entities.VersionType(bir.getCbeffversion().getMajor(),
                    bir.getCbeffversion().getMinor());
        } else {
            cbeffversion = null;
        }

        io.mosip.kernel.biometrics.constant.PurposeType purposeType;
        if(Objects.nonNull(bir.getBdbInfo().getPurpose())) {
            purposeType = io.mosip.kernel.biometrics.constant.PurposeType.fromValue(bir.getBdbInfo().getPurpose().name());
        } else {
            purposeType = null;
        }

        io.mosip.kernel.biometrics.constant.ProcessedLevelType processedLevelType;
        if(Objects.nonNull(bir.getBdbInfo().getLevel())) {
            processedLevelType = io.mosip.kernel.biometrics.constant.ProcessedLevelType.fromValue(
                    bir.getBdbInfo().getLevel().name());
        } else{
            processedLevelType = null;
        }

        return new io.mosip.kernel.biometrics.entities.BIR.BIRBuilder()
                .withBdb(bir.getBdb())
                .withVersion(version)
                .withCbeffversion(cbeffversion)
                .withBirInfo(new io.mosip.kernel.biometrics.entities.BIRInfo.BIRInfoBuilder().withIntegrity(true).build())
                .withBdbInfo(new io.mosip.kernel.biometrics.entities.BDBInfo.BDBInfoBuilder()
                        .withFormat(format)
                        .withType(bioTypes)
                        .withQuality(qualityType)
                        .withCreationDate(bir.getBdbInfo().getCreationDate())
                        .withIndex(bir.getBdbInfo().getIndex())
                        .withPurpose(purposeType)
                        .withLevel(processedLevelType)
                        .withSubtype(bir.getBdbInfo().getSubtype()).build()).build();
    }

    public static BIR convertToBIR(io.mosip.kernel.biometrics.entities.BIR bir) {
        List<SingleType> bioTypes = new ArrayList<>();
        for(BiometricType type : bir.getBdbInfo().getType()) {
            bioTypes.add(SingleType.fromValue(type.value()));
        }

        RegistryIDType format = null;
        if (bir.getBdbInfo() != null && bir.getBdbInfo().getFormat() != null) {
            format = new RegistryIDType();
            format.setOrganization(bir.getBdbInfo().getFormat().getOrganization());
            format.setType(bir.getBdbInfo().getFormat().getType());
        }

        RegistryIDType birAlgorithm = null;
        if (bir.getBdbInfo() != null
                && bir.getBdbInfo().getQuality() != null && bir.getBdbInfo().getQuality().getAlgorithm() != null) {
            birAlgorithm = new RegistryIDType();
            birAlgorithm.setOrganization(bir.getBdbInfo().getQuality().getAlgorithm().getOrganization());
            birAlgorithm.setType(bir.getBdbInfo().getQuality().getAlgorithm().getType());
        }


        QualityType qualityType = null;
        if (bir.getBdbInfo() != null && bir.getBdbInfo().getQuality() != null) {
            qualityType = new QualityType();
            qualityType.setAlgorithm(birAlgorithm);
            qualityType.setQualityCalculationFailed(bir.getBdbInfo().getQuality().getQualityCalculationFailed());
            qualityType.setScore(bir.getBdbInfo().getQuality().getScore());
        }

        return new BIR.BIRBuilder()
                .withBdb(bir.getBdb())
                .withVersion(bir.getVersion() == null ? null : new BIRVersion.BIRVersionBuilder()
                        .withMinor(bir.getVersion().getMinor())
                        .withMajor(bir.getVersion().getMajor()).build())
                .withCbeffversion(bir.getCbeffversion() == null ? null : new BIRVersion.BIRVersionBuilder()
                        .withMinor(bir.getCbeffversion().getMinor())
                        .withMajor(bir.getCbeffversion().getMajor()).build())
                .withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(true).build())
                .withBdbInfo(bir.getBdbInfo() == null ? null : new BDBInfo.BDBInfoBuilder()
                        .withFormat(format)
                        .withType(bioTypes)
                        .withQuality(qualityType)
                        .withCreationDate(bir.getBdbInfo().getCreationDate())
                        .withIndex(bir.getBdbInfo().getIndex())
                        .withPurpose(bir.getBdbInfo().getPurpose() == null ? null :
                                PurposeType.fromValue(io.mosip.kernel.biometrics.constant.PurposeType.fromValue(bir.getBdbInfo().getPurpose().name()).value()))
                        .withLevel(bir.getBdbInfo().getLevel() == null ? null :
                                ProcessedLevelType.fromValue(io.mosip.kernel.biometrics.constant.ProcessedLevelType.fromValue(bir.getBdbInfo().getLevel().name()).value()))
                        .withSubtype(bir.getBdbInfo().getSubtype()).build()).build();
    }

}
