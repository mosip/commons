package io.mosip.commons.packetmanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.commons.packet.dto.PacketInfo;
import io.mosip.commons.packet.dto.TagDto;
import io.mosip.commons.packet.dto.TagResponseDto;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;

@RestController
public class PacketWriterController {

    @Autowired
    private PacketWriter packetWriter;



    @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
    @PutMapping(path = "/createPacket", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseWrapper<List<PacketInfo>> createPacket(@RequestBody(required = true) RequestWrapper<PacketDto> requestr) {

        List<PacketInfo> resultField = packetWriter.createPacket(requestr.getRequest(), false);
        ResponseWrapper<List<PacketInfo>> response = getResponseWrapper();
        response.setResponse(resultField);
        return response;
    }

    private ResponseWrapper getResponseWrapper() {
        ResponseWrapper<Object> response = new ResponseWrapper<>();
		response.setId("mosip.registration.packet.writer");
        response.setVersion("v1");
        response.setResponsetime(DateUtils.getUTCCurrentDateTime());
        return response;
    }

	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@PostMapping(path = "/addTag", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<TagResponseDto> setTags(
			@RequestBody(required = true) RequestWrapper<TagDto> tagRequest) {

		TagResponseDto tagResponse = packetWriter.addTags(tagRequest.getRequest());
		ResponseWrapper<TagResponseDto> response = getResponseWrapper();
		response.setResponse(tagResponse);
		return response;
	}

	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@PostMapping(path = "/addOrUpdateTag", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<TagResponseDto> updateTags(@RequestBody(required = true) RequestWrapper<TagDto> tagRequest) {

		TagResponseDto tagResponse = packetWriter.updateTags(tagRequest.getRequest());
		ResponseWrapper<TagResponseDto> response = getResponseWrapper();
		response.setResponse(tagResponse);
		return response;
	}
}
