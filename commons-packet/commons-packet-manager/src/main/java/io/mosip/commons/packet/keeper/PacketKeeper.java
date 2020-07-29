package io.mosip.commons.packet.keeper;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.commons.packet.constants.LoggerFileConstant;
import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.Manifest;
import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import io.mosip.commons.packet.exception.ObjectStoreAdapterException;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.commons.packet.spi.IPacketCryptoHelper;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.core.logger.spi.Logger;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The packet keeper is used to store & retrieve packet, creation of audit, encrypt and sign packet.
 * Packet keeper is used to get container information and list of sources from a packet.
 *
 */
@Component
public class PacketKeeper {

    /** The reg proc logger. */
    private static Logger LOGGER = PacketManagerLogger.getLogger(PacketKeeper.class);

    private static final String PACKET_MANAGER_ACCOUNT = "PACKET_MANAGER_ACCOUNT";

    /** The Constant PACKET_NOTAVAILABLE_ERROR_DESC. */
    private static final String PACKET_NOTAVAILABLE_ERROR_DESC = "the requested file is not found in the destination";

    @Autowired
    @Qualifier("JossAdapter")
    private ObjectStoreAdapter jossAdapter;

    @Autowired
    @Qualifier("PosixAdapter")
    private ObjectStoreAdapter posixAdapter;

    /** The decryptor. */
    @Autowired
    private IPacketCryptoHelper decryptor;

    @Value("${objectstore.adapter.name}")
    private String adapterName;

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
        InputStream is = getAdapter().getObject(PACKET_MANAGER_ACCOUNT, packetInfo.getId(), packetInfo.getPacketName());
        InputStream subPacket = decryptPacket(is, packetInfo.getId());
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
        boolean result = getAdapter().putObject(PACKET_MANAGER_ACCOUNT,
                packet.getPacketInfo().getId(), packet.getPacketInfo().getPacketName(), packet.getPacket());
        return result ? packet.getPacketInfo() : null;
    }

    private ObjectStoreAdapter getAdapter() {
        if (adapterName.equalsIgnoreCase(jossAdapter.getClass().getSimpleName()))
            return jossAdapter;
        else if (adapterName.equalsIgnoreCase(posixAdapter.getClass().getSimpleName()))
            return posixAdapter;
        else
            throw new ObjectStoreAdapterException();
    }

    private InputStream decryptPacket(InputStream is, String id)
            throws PacketDecryptionFailureException, IOException, ApiNotAccessibleException {
        LOGGER.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
                "PacketKeeper::getFile() : getting packet from packet store");

        LOGGER.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), id,
                "PacketKeeper::getFile(regid)::decrypt sub packet");
        byte[] decryptedData = decryptor.decrypt(id, IOUtils.toByteArray(is));
        if (decryptedData == null) {
            throw new PacketDecryptionFailureException();
        }
        return new ByteArrayInputStream(decryptedData);
    }
}
