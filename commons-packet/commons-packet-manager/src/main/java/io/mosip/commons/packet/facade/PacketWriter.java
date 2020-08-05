package io.mosip.commons.packet.facade;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.packet.AuditDto;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.commons.packet.exception.NoAvailableProviderException;
import io.mosip.commons.packet.spi.IPacketWriter;
import io.mosip.commons.packet.util.PacketHelper;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.logger.spi.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/**
 * The packet writer facade
 *
 */
@Component
public class PacketWriter {

    private static final Logger LOGGER = PacketManagerLogger.getLogger(PacketWriter.class);

    @Autowired(required = false)
    @Qualifier("referenceWriterProviders")
    @Lazy
    private List<IPacketWriter> referenceWriterProviders;

    /**
     * Set field in identity object
     *
     * @param fieldName : name of the field
     * @param value : the value to be set
     * @return PacketWriter
     */
    public void setField(String id, String fieldName, String value, String source, String process) throws JSONException {
        LOGGER.info(PacketManagerLogger.SESSIONID,PacketManagerLogger.REGISTRATIONID, id,
                "setField for field name : " + fieldName + " source : " + source + " process : " + process);
        getProvider(source, process).setField(id, fieldName, value);
    }

    /**
     * Set Biometric
     *
     * @param fieldName : name of the field
     * @param biometricRecord : the biometric information
     * @return PacketWriter
     */
    public void setBiometric(String id, String fieldName, BiometricRecord biometricRecord, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID,PacketManagerLogger.REGISTRATIONID, id,
                "setBiometric for field name : " + fieldName + " source : " + source + " process : " + process);
        getProvider(source, process).setBiometric(id, fieldName, biometricRecord);

    }

    /**
     * Set documents
     *
     * @param documentName : name of the document
     * @param document : the document
     * @return PacketWriter
     */
    public void setDocument(String id, String documentName, Document document, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID,PacketManagerLogger.REGISTRATIONID, id,
                "setDocument for field name : " + documentName + " source : " + source + " process : " + process);
        getProvider(source, process).setDocument(id, documentName, document);
    }

    /**
     * Set meta information
     *
     * @param metaInfo : meta key value pairs
     * @return PacketWriter
     */
    public void setMetaInfo(String id, Map<String, String> metaInfo, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID,PacketManagerLogger.REGISTRATIONID, id,
                "setMetaInfo for source : " + source + " process : " + process);
        getProvider(source, process).setMetaInfo(id, metaInfo);
    }

    /**
     * Set audit information
     *
     * @param id : the registration id
     * @param source : the source packet. Default if not provided.
     * @param process : the process
     * @return PacketInfo
     */
    public void setAudits(String id, List<AuditDto> audits, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID,PacketManagerLogger.REGISTRATIONID, id,
                "setAudits for source : " + source + " process : " + process);
        getProvider(source, process).setAudits(id, audits);
    }

    /**
     * Persist the packet into storage.
     *
     * @param id : the registration id
     * @param source : the source packet. Default if not provided.
     * @param process : the process
     * @return PacketInfo
     */
    public List<PacketInfo> persistPacket(String id, double version, String schemaJson, String source, String process, byte[] publicKey, boolean offlineMode) {
        LOGGER.info(PacketManagerLogger.SESSIONID,PacketManagerLogger.REGISTRATIONID, id,
                "persistPacket for source : " + source + " process : " + process);
        return getProvider(source, process).persistPacket(id, version, schemaJson, source, process, offlineMode);
    }

    public List<PacketInfo> createPacket(PacketDto packetDto, boolean offlineMode) {
        LOGGER.info(PacketManagerLogger.SESSIONID,PacketManagerLogger.REGISTRATIONID, packetDto.getId(),
                "createPacket for RID : " + packetDto.getId() + " source : " + packetDto.getSource() + " process : " + packetDto.getProcess());
        List<PacketInfo> packetInfos = null;
        IPacketWriter provider = getProvider(packetDto.getSource(), packetDto.getProcess());
        try {
            provider.setFields(packetDto.getId(), packetDto.getFields());
            provider.setMetaInfo(packetDto.getId(), packetDto.getMetaInfo());
            packetDto.getDocuments().entrySet().forEach(doc -> provider.setDocument(packetDto.getId(), doc.getKey(), doc.getValue()));
            provider.setAudits(packetDto.getId(), packetDto.getAudits());
            packetDto.getBiometrics().entrySet().forEach(bio -> provider.setBiometric(packetDto.getId(), bio.getKey(), bio.getValue()));
            packetInfos = provider.persistPacket(packetDto.getId(), packetDto.getSchemaVersion(),
                    packetDto.getSchemaJson(), packetDto.getSource(), packetDto.getProcess(), offlineMode);

        } catch (Exception e) {
            LOGGER.error(PacketManagerLogger.SESSIONID,PacketManagerLogger.REGISTRATIONID, packetDto.getId(), e.getStackTrace().toString());
        }
        return packetInfos;
    }

    /**
     * Get the packet writer provider instance for source and process
     *
     * @param source : the source packet. Default if not provided.
     * @param process : the process
     * @return IPacketWriter : the provider instance
     */
    private IPacketWriter getProvider(String source, String process) {
        IPacketWriter provider = null;
        if (referenceWriterProviders != null && !referenceWriterProviders.isEmpty()) {
            Optional<IPacketWriter> refProvider = referenceWriterProviders.stream().filter(refPr ->
                    (PacketHelper.isSourceAndProcessPresent(refPr.getClass().getName(), source, process, PacketHelper.Provider.WRITER))).findAny();
            if (refProvider.isPresent() && refProvider.get() != null)
                provider = refProvider.get();
        }

        if (provider == null) {
            LOGGER.error(PacketManagerLogger.SESSIONID,PacketManagerLogger.REGISTRATIONID, null,
                    "No available provider found for source : " + source + " process : " + process);
            throw new NoAvailableProviderException();
        }

        return provider;
    }
}
