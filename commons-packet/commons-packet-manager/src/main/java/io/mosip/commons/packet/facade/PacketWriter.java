package io.mosip.commons.packet.facade;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.TagDto;
import io.mosip.commons.packet.dto.TagRequestDto;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.commons.packet.exception.NoAvailableProviderException;
import io.mosip.commons.packet.keeper.PacketKeeper;
import io.mosip.commons.packet.spi.IPacketWriter;
import io.mosip.commons.packet.util.PacketHelper;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;

/**
 * The packet writer facade
 */
@Component
public class PacketWriter {

    private static final Logger LOGGER = PacketManagerLogger.getLogger(PacketWriter.class);

    @Autowired(required = false)
    @Qualifier("referenceWriterProviders")
    @Lazy
    private List<IPacketWriter> referenceWriterProviders;

	@Autowired
	private PacketKeeper packetKeeper;

    /**
     * Set field in identity object
     *
     * @param fieldName : name of the field
     * @param value     : the value to be set
     * @return PacketWriter
     */
    public void setField(String id, String fieldName, String value, String source, String process){
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "setField for field name : " + fieldName + " source : " + source + " process : " + process);
        getProvider(source, process).setField(id, fieldName, value);
    }

    /**
     * Set fields in identity object
     *
     * @param fields : name value pair
     * @return PacketWriter
     */
    public void setFields(String id, Map<String, String> fields, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "setFields : source : " + source + " process : " + process);
        getProvider(source, process).setFields(id, fields);
    }

    /**
     * Set Biometric
     *
     * @param fieldName       : name of the field
     * @param biometricRecord : the biometric information
     * @return PacketWriter
     */
    public void setBiometric(String id, String fieldName, BiometricRecord biometricRecord, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "setBiometric for field name : " + fieldName + " source : " + source + " process : " + process);
        getProvider(source, process).setBiometric(id, fieldName, biometricRecord);

    }

    /**
     * Set documents
     *
     * @param documentName : name of the document
     * @param document     : the document
     * @return PacketWriter
     */
    public void setDocument(String id, String documentName, Document document, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "setDocument for field name : " + documentName + " source : " + source + " process : " + process);
        getProvider(source, process).setDocument(id, documentName, document);
    }

    /**
     * Set all meta information
     *
     * @param metaInfo : meta key value pairs
     * @return PacketWriter
     */
    public void addMetaInfo(String id, Map<String, String> metaInfo, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "setMetaInfo for source : " + source + " process : " + process);
        getProvider(source, process).addMetaInfo(id, metaInfo);
    }

    /**
     * Set individual meta information
     *
     * @param key : meta key
     * @Param value : meta value
     * @return PacketWriter
     */
    public void addMetaInfo(String id, String key, String value, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "setMetaInfo for source : " + source + " process : " + process);
        getProvider(source, process).addMetaInfo(id, key, value);
    }

    /**
     * Add audit list information
     *
     * @param id      : the registration id
     * @param source  : the source packet. Default if not provided.
     * @param process : the process
     * @return PacketInfo
     */
    public void addAudits(String id, List<Map<String, String>> audits, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "setAudits for source : " + source + " process : " + process);
        getProvider(source, process).addAudits(id, audits);
    }

    /**
     * Add single audit information
     *
     * @param id      : the registration id
     * @param source  : the source packet. Default if not provided.
     * @param process : the process
     * @return PacketInfo
     */
    public void addAudit(String id, Map<String, String> audit, String source, String process) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "setAudits for source : " + source + " process : " + process);
        getProvider(source, process).addAudit(id, audit);
    }

    /**
     * Persist the packet into storage.
     *
     * @param id      : the registration id
     * @param source  : the source packet. Default if not provided.
     * @param process : the process
     * @return PacketInfo
     */
    public List<PacketInfo> persistPacket(String id, String version, String schemaJson, String source,
                                          String process, String additionalInfoReqId, String refId, boolean offlineMode) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, id,
                "persistPacket for source : " + source + " process : " + process);
        return getProvider(source, process).persistPacket(id, version, schemaJson, source, process, additionalInfoReqId, refId, offlineMode);
    }

    public List<PacketInfo> createPacket(PacketDto packetDto) {
        LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, packetDto.getId(),
                "createPacket for RID : " + packetDto.getId() + " source : " + packetDto.getSource() + " process : " + packetDto.getProcess());
        List<PacketInfo> packetInfos = null;
        IPacketWriter provider = getProvider(packetDto.getSource(), packetDto.getProcess());
        try {
            if (packetDto.getFields() != null)
                provider.setFields(packetDto.getId(), packetDto.getFields());
            if (packetDto.getMetaInfo() != null)
                provider.addMetaInfo(packetDto.getId(), packetDto.getMetaInfo());
            if (packetDto.getDocuments() != null)
                packetDto.getDocuments().entrySet().forEach(doc -> provider.setDocument(packetDto.getId(), doc.getKey(), doc.getValue()));
            if (packetDto.getAudits() != null)
                provider.addAudits(packetDto.getId(), packetDto.getAudits());
            if (packetDto.getBiometrics() != null)
                packetDto.getBiometrics().entrySet().forEach(bio -> provider.setBiometric(packetDto.getId(), bio.getKey(), bio.getValue()));
            packetInfos = provider.persistPacket(packetDto.getId(), packetDto.getSchemaVersion(),
                    packetDto.getSchemaJson(), packetDto.getSource(), packetDto.getProcess(), packetDto.getAdditionalInfoReqId(),
                    packetDto.getRefId(), packetDto.isOfflineMode());

        } catch (Exception e) {
            LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, packetDto.getId(),
                    ExceptionUtils.getStackTrace(e));
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, packetDto.getId(), ExceptionUtils.getStackTrace(e));
        }
        return packetInfos;
    }

    /**
     * Get the packet writer provider instance for source and process
     *
     * @param source  : the source packet. Default if not provided.
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
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, null,
                    "No available provider found for source : " + source + " process : " + process);
            throw new NoAvailableProviderException();
        }

        return provider;
    }

	@CacheEvict(value = "tags", key = "{#tagDto.id}")
	public Map<String, String> addTags(TagDto tagDto) {
		Map<String, String> tags = packetKeeper.addTags(tagDto);
		return tags;

	}

	@CacheEvict(value = "tags", key = "{#tagDto.id}")
	public Map<String, String> addorUpdate(TagDto tagDto) {
		Map<String, String> tags = packetKeeper.addorUpdate(tagDto);
		return tags;

	}
	
	@CacheEvict(value = "tags", key = "{#tagDto.id}")
	public void deleteTags(TagRequestDto tagDto) {
		packetKeeper.deleteTags(tagDto);
	}
}
