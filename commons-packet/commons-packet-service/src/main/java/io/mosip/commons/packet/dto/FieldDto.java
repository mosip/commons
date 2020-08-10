package io.mosip.commons.packet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class FieldDto {

    private String id;
    private String field;
    private String source;
    private String process;
    private Boolean bypassCache;
}
