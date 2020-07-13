package io.mosip.commons.packet;

import io.mosip.commons.packet.dto.Packet;
import io.mosip.commons.packet.dto.Manifest;
import io.mosip.commons.packet.dto.PacketInfo;

import java.io.InputStream;

/**
 * The packet keeper
 *
 */
public class PacketKeeper {

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
    public Packet getPacket(PacketInfo packetInfo) {
        return null;
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
}
