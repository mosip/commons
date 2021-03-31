package io.mosip.commons.packetmanager.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;

import io.mosip.commons.packet.dto.Document;
import io.mosip.commons.packet.dto.TagRequestDto;
import io.mosip.commons.packet.dto.TagResponseDto;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packetmanager.dto.BiometricRequestDto;
import io.mosip.commons.packetmanager.dto.DocumentDto;
import io.mosip.commons.packetmanager.dto.FieldDto;
import io.mosip.commons.packetmanager.dto.FieldDtos;
import io.mosip.commons.packetmanager.dto.FieldResponseDto;
import io.mosip.commons.packetmanager.dto.InfoDto;
import io.mosip.commons.packetmanager.dto.InfoRequestDto;
import io.mosip.commons.packetmanager.dto.InfoResponseDto;
import io.mosip.commons.packetmanager.dto.ValidatePacketResponse;
import io.mosip.commons.packetmanager.service.PacketReaderService;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;

@RestController
public class PacketReaderController {

    @Autowired
    private PacketReader packetReader;

    @Autowired
    private PacketReaderService packetReaderService;

    @PreAuthorize("hasAnyRole('DATA_READ')")
    @ResponseFilter
    @PostMapping(path = "/searchField", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseWrapper<FieldResponseDto> searchField(@RequestBody(required = true) RequestWrapper<FieldDto> fieldDto) {

        String resultField = packetReader.getField(fieldDto.getRequest().getId(),
                fieldDto.getRequest().getField(), packetReaderService.getSource(fieldDto.getRequest().getField(), fieldDto.getRequest().getSource(),
                        fieldDto.getRequest().getProcess()), fieldDto.getRequest().getProcess(), fieldDto.getRequest().getBypassCache());
        ResponseWrapper<FieldResponseDto> response = new ResponseWrapper<FieldResponseDto>();
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put(fieldDto.getRequest().getField(), resultField);
        FieldResponseDto fieldResponseDto = new FieldResponseDto(responseMap);

        response.setResponse(fieldResponseDto);
        return response;
    }

    @PreAuthorize("hasAnyRole('DATA_READ')")
    @ResponseFilter
    @PostMapping(path = "/searchFields", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<FieldResponseDto> searchFields(@RequestBody(required = true) RequestWrapper<FieldDtos> request) {
        FieldDtos fieldDtos = request.getRequest();
        Map<String, String> resultFields = new HashMap<>();
        if ((fieldDtos.getSource()) == null) {
            for (String field : fieldDtos.getFields()) {
                String value = packetReader.getField(fieldDtos.getId(), field, packetReaderService.getSource(
                        field, fieldDtos.getSource(), fieldDtos.getProcess()), fieldDtos.getProcess(), fieldDtos.getBypassCache());
                resultFields.put(field, value);
            }
        } else
            resultFields = packetReader.getFields(fieldDtos.getId(), fieldDtos.getFields(), fieldDtos.getSource(), fieldDtos.getProcess(), fieldDtos.getBypassCache());
        FieldResponseDto resultField = new FieldResponseDto(resultFields);
        ResponseWrapper<FieldResponseDto> response = new ResponseWrapper<FieldResponseDto>();
        response.setResponse(resultField);
        return response;
    }

    @PreAuthorize("hasAnyRole('DOCUMENT_READ')")
    @ResponseFilter
    @PostMapping(path = "/document", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<Document> getDocument(@RequestBody(required = true) RequestWrapper<DocumentDto> request) {
        DocumentDto documentDto = request.getRequest();
        Document document = packetReader.getDocument(documentDto.getId(), documentDto.getDocumentName(), packetReaderService
                .getSource(documentDto.getDocumentName(), documentDto.getSource(), documentDto.getProcess()), documentDto.getProcess());
        ResponseWrapper<Document> response = new ResponseWrapper<Document>();
        response.setResponse(document);
        return response;
    }

    @PreAuthorize("hasAnyRole('BIOMETRIC_READ')")
    @ResponseFilter
    @PostMapping(path = "/biometrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<BiometricRecord> getBiometrics(@RequestBody(required = true) RequestWrapper<BiometricRequestDto> request) {
        BiometricRequestDto bioRequest = request.getRequest();
        List<String> modalities = bioRequest.getModalities() == null ? Lists.newArrayList() : bioRequest.getModalities();
        BiometricRecord responseDto = packetReader.getBiometric(bioRequest.getId(), bioRequest.getPerson(), modalities,
                packetReaderService.getSource(bioRequest.getPerson(), bioRequest.getSource(), bioRequest.getProcess()), bioRequest.getProcess(), bioRequest.isBypassCache());
        ResponseWrapper<BiometricRecord> response = getResponseWrapper();
        response.setResponse(responseDto);
        return response;
    }

    @PreAuthorize("hasAnyRole('METADATA_READ')")
    @ResponseFilter
    @PostMapping(path = "/metaInfo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<FieldResponseDto> getMetaInfo(@RequestBody(required = true) RequestWrapper<InfoDto> request) {
        InfoDto metaDto = request.getRequest();
        Map<String, String> resultFields = packetReader.getMetaInfo(metaDto.getId(), packetReaderService
                .getSource(PacketReaderService.META_INFO, metaDto.getSource(), metaDto.getProcess()), metaDto.getProcess(), metaDto.getBypassCache());
        FieldResponseDto resultField = new FieldResponseDto(resultFields);
        ResponseWrapper<FieldResponseDto> response = getResponseWrapper();
        response.setResponse(resultField);
        return response;
    }

    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PostMapping(path = "/audits", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<List<FieldResponseDto>> getAudits(@RequestBody(required = true) RequestWrapper<InfoDto> request) {
        InfoDto metaDto = request.getRequest();
        List<Map<String, String>> resultFields = packetReader.getAudits(metaDto.getId(), packetReaderService.getSource(
                PacketReaderService.AUDITS, metaDto.getSource(), metaDto.getProcess()), metaDto.getProcess(), metaDto.getBypassCache());
        List<FieldResponseDto> resultField = new ArrayList<>();
        if (resultFields != null && !resultFields.isEmpty()) {
            resultFields.stream().forEach(e -> {
                FieldResponseDto fieldResponseDto = new FieldResponseDto(e);
                resultField.add(fieldResponseDto);
            });
        }
        ResponseWrapper<List<FieldResponseDto>> response = getResponseWrapper();
        response.setResponse(resultField);
        return response;
    }

    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PostMapping(path = "/validatePacket", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<ValidatePacketResponse> validatePacket(@RequestBody(required = true) RequestWrapper<InfoDto> request) {
        InfoDto metaDto = request.getRequest();
        boolean resultFields = packetReader.validatePacket(metaDto.getId(), packetReaderService.getSource(
                null, metaDto.getSource(), metaDto.getProcess()), metaDto.getProcess());
        ResponseWrapper<ValidatePacketResponse> response = getResponseWrapper();
        response.setResponse(new ValidatePacketResponse(resultFields));
        return response;
    }

	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@PostMapping(path = "/getTags", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<TagResponseDto> getTags(
			@RequestBody(required = true) RequestWrapper<TagRequestDto> request) {

		TagResponseDto tagResponseDto = packetReaderService.getTags(request.getRequest());
		ResponseWrapper<TagResponseDto> response = getResponseWrapper();
		response.setResponse(tagResponseDto);
		return response;
	}

    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PostMapping(path = "/info", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<InfoResponseDto> info(@RequestBody(required = true) RequestWrapper<InfoRequestDto> request) {
        String id = request.getRequest().getId();
        InfoResponseDto resultFields = null;
        if (id != null && !id.isEmpty())
            resultFields = packetReaderService.info(id);
        ResponseWrapper<InfoResponseDto> response = getResponseWrapper();
        response.setResponse(resultFields);
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
