package io.mosip.commons.packetmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class SourceProcessDto implements Serializable {
    private String source;
    private String process;
}
