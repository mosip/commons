package io.mosip.commons.packetmanager.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class InfoDto {

    private String id;
    private String source;
    private String process;
    private Boolean bypassCache;
}
