package io.mosip.commons.packet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Packet {

    private PacketInfo packetInfo;
    private byte[] packet;
}
