package io.mosip.commons.packetmanager.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ContainerInfoDto {

    private String source;
    private String process;
    private Date lastModified;
    private Set<String> demographics;
    private List<BiometricsDto> biometrics;
    private Map<String, String> documents;

}
