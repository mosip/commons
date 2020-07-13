package io.mosip.commons.packetmanager.controller;

import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packetmanager.dto.FieldDto;
import io.mosip.commons.packetmanager.dto.FieldDtos;
import io.mosip.commons.packetmanager.dto.FieldDtosResponse;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import javax.validation.Valid;
import java.util.Map;

@RestController
public class PacketReaderController {

    @Autowired
    private PacketReader packetReader;

    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PostMapping(path = "/searchField", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<String> searchField(@RequestBody(required = true) FieldDto fieldDto) {

        String resultField = packetReader.getField(fieldDto.getId(), fieldDto.getField(), fieldDto.getSource(), fieldDto.getProcess());
        ResponseWrapper<String> response = new ResponseWrapper<String>();
        response.setResponse(resultField);
        return response;
    }

    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PostMapping(path = "/searchFields", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<FieldDtosResponse> searchFields(@RequestBody(required = true) FieldDtos fieldDtos) {

        Map<String, String> resultFields = packetReader.getFields(fieldDtos.getId(), fieldDtos.getFields(), fieldDtos.getSource(), fieldDtos.getProcess());
        FieldDtosResponse resultField = new FieldDtosResponse();
        resultField.setFields(resultFields);
        ResponseWrapper<FieldDtosResponse> response = new ResponseWrapper<FieldDtosResponse>();
        response.setResponse(resultField);
        return response;
    }
}
