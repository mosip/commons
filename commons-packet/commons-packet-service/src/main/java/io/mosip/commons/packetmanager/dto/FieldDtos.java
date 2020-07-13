package io.mosip.commons.packetmanager.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode
public class FieldDtos {

    String id;
    List<String> fields;
    String source;
    String process;
}
