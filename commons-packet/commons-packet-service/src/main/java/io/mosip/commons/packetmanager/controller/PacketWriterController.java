package io.mosip.commons.packetmanager.controller;

import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.commons.packetmanager.dto.FieldResponseDto;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class PacketWriterController {

    @Autowired
    private PacketWriter packetWriter;

    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PostMapping(path = "/createPacket", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<PacketInfo> createPacket(@RequestBody(required = true) RequestWrapper<PacketDto> requestr) {

        PacketInfo resultField = packetWriter.createPacket(requestr.getRequest(), true);
        ResponseWrapper<PacketInfo> response = getResponseWrapper();
        response.setResponse(resultField);
        return response;
    }

    private ResponseWrapper getResponseWrapper() {
        ResponseWrapper<Object> response = new ResponseWrapper<>();
        response.setId("mosip.registration.packet.reader");
        response.setVersion("v1");
        response.setResponsetime(DateUtils.getUTCCurrentDateTime());
        return response;
    }
}
