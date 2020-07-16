package io.mosip.commons.packet.facade;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.spi.IPacketWriter;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The packet writer facade
 *
 */
@Component
public class PacketWriter {

    private String id;
    private String source;
    private String process;

    private IPacketWriter iPacketWriter;

    /**
     * Constructor. Initialize id, source and process
     *
     * @param id : the registration id
     * @param source : the source packet. Default if not provided.
     * @param process : the process
     */
    /*private PacketWriter(String id, String source, String process) {
        this.id = id;
        this.source = source;
        this.process = process;
    }*/

    /**
     * Get the packet writer instance for id, source and process
     *
     * @param id : the registration id
     * @param source : the source packet. Default if not provided.
     * @param process : the process
     * @return PacketWriter
     */
    /*public static PacketWriter getPacketWriter(String id, String source, String process) {
        return new PacketWriter(id, source, process);
    }*/

    public void initialize(String source, String process) {
        iPacketWriter = this.getProvider(source, process);
    }

    /**
     * Set field in identity object
     *
     * @param fieldName : name of the field
     * @param value : the value to be set
     * @return PacketWriter
     */
    public void setField(String fieldName, String value) {
        iPacketWriter.setField(fieldName, value);
    }

    /**
     * Set Biometric
     *
     * @param fieldName : name of the field
     * @param biometricRecord : the biometric information
     * @return PacketWriter
     */
    public void setBiometric(String fieldName, BiometricRecord biometricRecord) {
        iPacketWriter.setBiometric(fieldName, biometricRecord);

    }

    /**
     * Set documents
     *
     * @param documentName : name of the document
     * @param document : the document
     * @return PacketWriter
     */
    public void setDocument(String documentName, Document document) {
        iPacketWriter.setDocument(documentName, document);
    }

    /**
     * Set meta information
     *
     * @param key : meta key
     * @param value : meta value
     * @return PacketWriter
     */
    public PacketWriter setMetaInfo(String key, String value) {
        return this;
    }

    /**
     * Persist the packet into storage.
     *
     * @param id : the registration id
     * @param source : the source packet. Default if not provided.
     * @param process : the process
     * @return PacketInfo
     */
    public PacketInfo persistPacket(String id, String source, String process) {
        return  getProvider(source, process).persistPacket();
    }

    /**
     * Get the packet writer provider instance for source and process
     *
     * @param source : the source packet. Default if not provided.
     * @param process : the process
     * @return IPacketWriter : the provider instance
     */
    private IPacketWriter getProvider(String source, String process) {
        iPacketWriter.initialize(source, process);
        // return Packet Writer Impl instance for the source and provider;
        return iPacketWriter;
    }
}
