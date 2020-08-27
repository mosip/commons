package io.mosip.commons.packet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode
public class Manifest {

    private List<PacketInfo> packetInfos;
    private Date creationDate;
}
