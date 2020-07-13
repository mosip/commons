package io.mosip.commons.packet.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.InputStream;

@Data
@EqualsAndHashCode
public class Document {

    private String type;
    private String category;
    private InputStream document;
}
