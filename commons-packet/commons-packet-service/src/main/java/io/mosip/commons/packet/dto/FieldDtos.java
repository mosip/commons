package io.mosip.commons.packet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
public class FieldDtos {

    private String id;
    private List<String> fields;
    private String source;
    private String process;
    private Boolean bypassCache;
}
