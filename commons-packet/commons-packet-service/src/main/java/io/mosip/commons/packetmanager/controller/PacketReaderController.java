package io.mosip.commons.packetmanager.controller;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.mosip.commons.packetmanager.dto.InfoRequestDto;
import io.mosip.commons.packetmanager.dto.InfoResponseDto;
import io.mosip.commons.packetmanager.dto.SourceAndProcess;
import io.mosip.commons.packetmanager.service.PacketReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import io.mosip.commons.packetmanager.dto.ValidatePacketResponse;
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
    	FieldDto dto=fieldDto.getRequest();
    	if(dto.getSource() ==null ||dto.getSource().isBlank() 
        		|| dto.getProcess() ==null ||dto.getProcess().isBlank()) {
        	SourceAndProcess sp=packetReaderService.getSourceAndProcess(dto.getId(),dto.getField());
        	dto.setSource(sp.getSource());
        	dto.setProcess(sp.getProcess());
        	fieldDto.setRequest(dto);
        }
        String resultField = packetReader.getField(fieldDto.getRequest().getId(),
                fieldDto.getRequest().getField(), fieldDto.getRequest().getSource(), fieldDto.getRequest().getProcess(), fieldDto.getRequest().getBypassCache());
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
    public ResponseWrapper<FieldResponseDto> searchFields(@RequestBody(required = true) RequestWrapper<FieldDtos> request)  {
        FieldDtos fieldDtos = request.getRequest();
        if(fieldDtos.getSource() ==null ||fieldDtos.getSource().isBlank() 
        		|| fieldDtos.getProcess() ==null ||fieldDtos.getProcess().isBlank()) {
        	Map<SourceAndProcess,List<String>> map=packetReaderService.getSourcesAndProcesses(fieldDtos.getId() ,fieldDtos.getFields());
        	Map<String, String> resultFields=new HashMap<>();
        	for(Entry<SourceAndProcess,List<String>> entry:map.entrySet()) {
        		if(!entry.getValue().isEmpty())
        		 resultFields.putAll(packetReader.getFields(fieldDtos.getId(), entry.getValue(), entry.getKey().getSource(), entry.getKey().getProcess(), fieldDtos.getBypassCache()));   
        	}
        	FieldResponseDto resultField = new FieldResponseDto(resultFields);
            ResponseWrapper<FieldResponseDto> response = new ResponseWrapper<FieldResponseDto>();
            response.setResponse(resultField);
            return response;
        }
        Map<String, String> resultFields = packetReader.getFields(fieldDtos.getId(), fieldDtos.getFields(), fieldDtos.getSource(), fieldDtos.getProcess(), fieldDtos.getBypassCache());
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
        if(documentDto.getSource() ==null ||documentDto.getSource().isBlank() 
        		|| documentDto.getProcess() ==null ||documentDto.getProcess().isBlank()) {
        	SourceAndProcess sp=packetReaderService.getSourceAndProcess(documentDto.getId(),documentDto.getDocumentName());
        	documentDto.setSource(sp.getSource());
        	documentDto.setProcess(sp.getProcess());
        }
        Document document = packetReader.getDocument(documentDto.getId(), documentDto.getDocumentName(), documentDto.getSource(), documentDto.getProcess());
        ResponseWrapper<Document> response = new ResponseWrapper<Document>();
        response.setResponse(document);
        return response;
    }

    @PreAuthorize("hasAnyRole('BIOMETRIC_READ')")
    @ResponseFilter
    @PostMapping(path = "/biometrics", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<BiometricRecord> getBiometrics(@RequestBody(required = true) RequestWrapper<BiometricRequestDto> request) {
        BiometricRequestDto bioRequest = request.getRequest();
        if(bioRequest.getSource() ==null ||bioRequest.getSource().isBlank() 
        		|| bioRequest.getProcess() ==null ||bioRequest.getProcess().isBlank()) {
        	SourceAndProcess sp=packetReaderService.getSourceAndProcess(bioRequest.getId(),bioRequest.getPerson());
        	bioRequest.setSource(sp.getSource());
        	bioRequest.setProcess(sp.getProcess());
        }
        List<String> modalities = bioRequest.getModalities() == null ? Lists.newArrayList() : bioRequest.getModalities();
        BiometricRecord responseDto = packetReader.getBiometric(bioRequest.getId(), bioRequest.getPerson(), modalities, bioRequest.getSource(), bioRequest.getProcess(), bioRequest.isBypassCache());
        ResponseWrapper<BiometricRecord> response = getResponseWrapper();
        response.setResponse(responseDto);
        return response;
    }

    @PreAuthorize("hasAnyRole('METADATA_READ')")
    @ResponseFilter
    @PostMapping(path = "/metaInfo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<FieldResponseDto> getMetaInfo(@RequestBody(required = true) RequestWrapper<InfoDto> request) {
        InfoDto metaDto = request.getRequest();
        if(metaDto.getSource() ==null ||metaDto.getSource().isBlank() 
        		|| metaDto.getProcess() ==null ||metaDto.getProcess().isBlank()) {
        	SourceAndProcess sp=packetReaderService.getSourceAndProcess(metaDto.getId());
        	metaDto.setSource(sp.getSource());
        	metaDto.setProcess(sp.getProcess());
        }
        Map<String, String> resultFields = packetReader.getMetaInfo(metaDto.getId(), metaDto.getSource(), metaDto.getProcess(), metaDto.getBypassCache());
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
        if(metaDto.getSource() ==null ||metaDto.getSource().isBlank() 
        		|| metaDto.getProcess() ==null ||metaDto.getProcess().isBlank()) {
        	SourceAndProcess sp=packetReaderService.getSourceAndProcess(metaDto.getId());
        	metaDto.setSource(sp.getSource());
        	metaDto.setProcess(sp.getProcess());
        }
        List<Map<String, String>> resultFields = packetReader.getAudits(metaDto.getId(), metaDto.getSource(), metaDto.getProcess(), metaDto.getBypassCache());
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
        if(metaDto.getSource() ==null ||metaDto.getSource().isBlank() 
        		|| metaDto.getProcess() ==null ||metaDto.getProcess().isBlank()) {
        	SourceAndProcess sp=packetReaderService.getSourceAndProcess(metaDto.getId());
        	metaDto.setSource(sp.getSource());
        	metaDto.setProcess(sp.getProcess());
        }
        boolean resultFields = packetReader.validatePacket(metaDto.getId(), metaDto.getSource(), metaDto.getProcess());
        ResponseWrapper<ValidatePacketResponse> response = getResponseWrapper();
        response.setResponse(new ValidatePacketResponse(resultFields));
        return response;
    }

	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@PostMapping(path = "/getTags", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<TagResponseDto> getTags(
			@RequestBody(required = true) RequestWrapper<TagRequestDto> request) {

		TagResponseDto tagResponseDto = packetReader.getTags(request.getRequest().getId(),
				request.getRequest().getTagNames());
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
