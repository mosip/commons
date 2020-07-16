package io.mosip.commons.packet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.InputStream;

@Data
@EqualsAndHashCode
public class Packet {

    private PacketInfo packetInfo;
    private InputStream packet;
}
