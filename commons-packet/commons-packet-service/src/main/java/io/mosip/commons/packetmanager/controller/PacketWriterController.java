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
import io.mosip.commons.packet.dto.TagDeleteResponseDto;
import io.mosip.commons.packet.dto.TagDto;
import io.mosip.commons.packet.dto.TagRequestDto;
import io.mosip.commons.packet.dto.TagResponseDto;
import io.mosip.commons.packet.dto.packet.PacketDto;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.commons.packetmanager.service.PacketWriterService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;

@RestController
public class PacketWriterController {

    @Autowired
    private PacketWriter packetWriter;

    @Autowired
    private PacketWriterService packetWriterService;

   // @PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
    @ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostcreatepacket())")
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

	//@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostaddtag())")
	@PostMapping(path = "/addTag", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<TagResponseDto> setTags(
			@RequestBody(required = true) RequestWrapper<TagDto> tagRequest) {

		TagResponseDto tagResponse = packetWriterService.addTags(tagRequest.getRequest());
		ResponseWrapper<TagResponseDto> response = getResponseWrapper();
		response.setResponse(tagResponse);
		return response;
	}

	//@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostaddorupdatetag())")
	@PostMapping(path = "/addOrUpdateTag", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<TagResponseDto> updateTags(@RequestBody(required = true) RequestWrapper<TagDto> tagRequest) {

		TagResponseDto tagResponse = packetWriterService.updateTags(tagRequest.getRequest());
		ResponseWrapper<TagResponseDto> response = getResponseWrapper();
		response.setResponse(tagResponse);
		return response;
	}
	//@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostdeletetag())")
	@PostMapping(path = "/deleteTag", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<TagDeleteResponseDto> deleteTags(@RequestBody(required = true) RequestWrapper<TagRequestDto> tagRequest) {

		TagDeleteResponseDto tagResponse = packetWriterService.deleteTags(tagRequest.getRequest());
		ResponseWrapper<TagDeleteResponseDto> response = getResponseWrapper();
		response.setResponse(tagResponse);
		return response;
	}
}
