package io.mosip.commons.packetmanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class InfoResponseDto {
    private String applicationId;
    private String packetId;
    private String requestToken;
    private List<ContainerInfoDto> info;
}
