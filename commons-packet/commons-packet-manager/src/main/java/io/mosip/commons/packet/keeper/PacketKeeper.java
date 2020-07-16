package io.mosip.commons.packet.keeper;

import io.mosip.commons.packet.constants.LoggerFileConstant;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.Manifest;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import io.mosip.commons.packet.exception.FileNotFoundInDestinationException;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.commons.packet.util.PacketDecryptorUtil;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The packet keeper is used to store & retrieve packet, creation of audit, encrypt and sign packet.
 * Packet keeper is used to get container information and list of sources from a packet.
 *
 */
@Component
public class PacketKeeper {

    @Autowired
    private FileSystemAdapter fileSystemAdapter;

    /** The reg proc logger. */
    private static Logger LOGGER = PacketManagerLogger.getLogger(PacketKeeper.class);

    /** The Constant PACKET_NOTAVAILABLE_ERROR_DESC. */
    private static final String PACKET_NOTAVAILABLE_ERROR_DESC = "the requested file is not found in the destination";

    /*@Autowired
    private FileSystemAdapter fileSystemAdapter;*/

    /** The decryptor. */
    @Autowired
    private PacketDecryptorUtil decryptor;

    /**
     * Get the manifest information for given packet id
     *
     * @param id : packet id
     * @return : Manifest
     */
    public Manifest getManifest(String id) {
        return new Manifest();
    }

    /**
     * Check packet integrity.
     *
     * @param packetInfo : the packet information
     * @return : boolean
     */
    public boolean checkIntegrity(PacketInfo packetInfo) {
        return false;
    }

    /**
     * Get packet
     *
     * @param packetInfo : packet info
     * @return : Packet
     */
    public Packet getPacket(PacketInfo packetInfo) throws IOException, PacketDecryptionFailureException, ApiNotAccessibleException {
        InputStream subPacket = getSubpacket(packetInfo.getId(), packetInfo.getPacketName());
        Packet packet = new Packet();
        packet.setPacket(subPacket);
        packet.setPacketInfo(packetInfo);
        return packet;
    }

    /**
     * Put packet into storage/cache
     *
     * @param packet : the Packet
     * @return PacketInfo
     */
    public PacketInfo putPacket(Packet packet) {
        return null;
    }

    private InputStream getSubpacket(String id, String packetName)
            throws PacketDecryptionFailureException, IOException, ApiNotAccessibleException {
        LOGGER.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
                "PacketKeeper::getFile() : getting packet from packet store");
        InputStream data = fileSystemAdapter.getPacket(id);
        //File file = new File("C:\\Users\\M1045447\\Downloads\\10001100770000220200715093118.zip");
        //InputStream data = new FileInputStream(file);
        if (data == null) {
            throw new FileNotFoundInDestinationException(
                    PACKET_NOTAVAILABLE_ERROR_DESC);
        }
        LOGGER.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
                "PacketKeeper::getFile():: extracting sub packet");

        InputStream sourceFolderInputStream = ZipUtils.unzipAndGetFile(data, id + "_" + packetName);

        if (sourceFolderInputStream == null) {
            throw new FileNotFoundInDestinationException(
                    PACKET_NOTAVAILABLE_ERROR_DESC);
        }

        LOGGER.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
                "PacketKeeper::getFile(regid)::decrypt sub packet");
        InputStream decryptedData = decryptor.decrypt(sourceFolderInputStream, id);
        if (decryptedData == null) {
            throw new PacketDecryptionFailureException();
        }
        return decryptedData;
    }
}
