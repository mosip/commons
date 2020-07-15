package io.mosip.commons.packet.dto;

import java.io.InputStream;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class Document {

    private String type;
    private InputStream document;
}
