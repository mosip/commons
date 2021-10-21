package io.mosip.commons.packetmanager.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class InfoResponseDto {
    private String applicationId;
    private String packetId;
    private String requestToken;
    private List<ContainerInfoDto> info;
    private Map<String, String> tags;
}
