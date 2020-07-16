package io.mosip.commons.packet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.InputStream;

@Data
@EqualsAndHashCode
public class Document {

    private byte[] document;
    private String value;
    private String type;
    private String format;
}
