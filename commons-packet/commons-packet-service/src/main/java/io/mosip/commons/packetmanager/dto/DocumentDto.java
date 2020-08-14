package io.mosip.commons.packetmanager.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class DocumentDto {

    private String id;
    private String documentName;
    private String source;
    private String process;
}
