package io.mosip.kernel.syncdata.dto;

import lombok.Data;

import java.util.List;


@Data
public class MachineResponseDto {
    private List<MachineDto> machines;
}
