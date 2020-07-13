package io.mosip.commons.packetmanager.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class FieldDto {

    String id;
    String field;
    String source;
    String process;
}
