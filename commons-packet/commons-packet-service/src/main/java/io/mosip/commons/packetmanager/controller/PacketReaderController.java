package io.mosip.commons.packetmanager.controller;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packetmanager.dto.DocumentDto;
import io.mosip.commons.packetmanager.dto.FieldDto;
import io.mosip.commons.packetmanager.dto.FieldDtos;
import io.mosip.commons.packetmanager.dto.FieldDtosResponse;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PacketReaderController {

    @Autowired
    private PacketReader packetReader;

    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PostMapping(path = "/searchField", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<String> searchField(@RequestBody(required = true) RequestWrapper<FieldDto> fieldDto) {

        String resultField = packetReader.getField(fieldDto.getRequest().getId(),
                fieldDto.getRequest().getField(), fieldDto.getRequest().getSource(), fieldDto.getRequest().getProcess(), fieldDto.getRequest().getBypassCache());
        ResponseWrapper<String> response = new ResponseWrapper<String>();
        response.setResponse(resultField);
        return response;
    }

    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PostMapping(path = "/searchFields", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<FieldDtosResponse> searchFields(@RequestBody(required = true) RequestWrapper<FieldDtos> request) {
        FieldDtos fieldDtos = request.getRequest();
        Map<String, String> resultFields = packetReader.getFields(fieldDtos.getId(), fieldDtos.getFields(), fieldDtos.getSource(), fieldDtos.getProcess(), fieldDtos.getBypassCache());
        FieldDtosResponse resultField = new FieldDtosResponse();
        resultField.setFields(resultFields);
        ResponseWrapper<FieldDtosResponse> response = new ResponseWrapper<FieldDtosResponse>();
        response.setResponse(resultField);
        return response;
    }

    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PostMapping(path = "/document", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<Document> getDocument(@RequestBody(required = true) RequestWrapper<DocumentDto> request) {
        DocumentDto documentDto = request.getRequest();
        Document document = packetReader.getDocument(documentDto.getId(), documentDto.getDocumentName(), documentDto.getSource(), documentDto.getProcess());
        ResponseWrapper<Document> response = new ResponseWrapper<Document>();
        response.setResponse(document);
        return response;
    }
}
