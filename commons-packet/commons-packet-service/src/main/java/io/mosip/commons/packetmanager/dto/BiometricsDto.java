package io.mosip.commons.packetmanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class BiometricsDto {

    private String type;
    private List<String> subtypes;
}
