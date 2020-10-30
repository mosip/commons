package io.mosip.kernel.jasperreport.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.jasperreport.dto.JasperreportResponseDto;
import io.mosip.kernel.jasperreport.request.JasperreportRequestDto;


@RestController
@CrossOrigin
public class JasperreportController {


	@ResponseFilter
	@PostMapping(value = "/jasperreport", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseWrapper<JasperreportResponseDto> addAudit(@RequestBody @Valid RequestWrapper<JasperreportRequestDto> requestDto) {
		ResponseWrapper<JasperreportResponseDto> response = new ResponseWrapper<>();
		return response;
	}
}
