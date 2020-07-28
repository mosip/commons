package io.mosip.commons.packet.facade;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.packet.AuditDto;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.commons.packet.exception.NoAvailableProviderException;
import io.mosip.commons.packet.spi.IPacketWriter;
import io.mosip.commons.packet.util.PacketHelper;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
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
        getProvider(source, process).setDocument(id, documentName, document);
    }

    /**
     * Set meta information
     *
     * @param metaInfo : meta key value pairs
     * @return PacketWriter
     */
    public void setMetaInfo(String id, Map<String, String> metaInfo, String source, String process) {
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
    public PacketInfo persistPacket(String id, double version, String schemaJson, String source, String process) {
        return getProvider(source, process).persistPacket(id, version, schemaJson);
    }

    public PacketInfo createPacket(PacketDto packetDto, boolean isServerCall) {
        PacketInfo packetInfo = null;
        IPacketWriter provider = getProvider(packetDto.getSource(), packetDto.getProcess());
        try {
            provider.setFields(packetDto.getId(), packetDto.getFields());
            provider.setMetaInfo(packetDto.getId(), packetDto.getMetaInfo());
            packetDto.getDocuments().entrySet().forEach(doc -> provider.setDocument(packetDto.getId(), doc.getKey(), doc.getValue()));
            provider.setAudits(packetDto.getId(), packetDto.getAudits());
            packetDto.getBiometrics().entrySet().forEach(bio -> provider.setBiometric(packetDto.getId(), bio.getKey(), bio.getValue()));
            packetInfo = provider.persistPacket(packetDto.getId(), packetDto.getSchemaVersion(), packetDto.getSchemaJson());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return packetInfo;
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
                    (PacketHelper.isSourceAndProcessPresent(refPr.getClass().getName(), source, process, PacketHelper.Provider.READER))).findAny();
            if (refProvider.isPresent() && refProvider.get() != null)
                provider = refProvider.get();
        }

        if (provider == null) {
            throw new NoAvailableProviderException();
        }

        return provider;
    }
}
