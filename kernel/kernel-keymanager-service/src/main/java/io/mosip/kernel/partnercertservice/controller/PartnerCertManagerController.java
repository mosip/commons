package io.mosip.kernel.partnercertservice.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.partnercertservice.dto.CACertificateResponseDto;
import io.mosip.kernel.partnercertservice.dto.CACertificateRequestDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertDownloadRequestDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertDownloadResponeDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertificateRequestDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertificateResponseDto;
import io.mosip.kernel.partnercertservice.service.spi.PartnerCertificateManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * Rest Controller for Partner Certificate Management includes certificate Validation and certificate Storage.
 * 
 * @author Mahammed Taheer
 *
 * @since 1.2.0
 */

@CrossOrigin
@RestController
@Api(value = "Operation related to partner certificate management.", tags = { "partnercertmanager" })
public class PartnerCertManagerController {
	
	/**
	 * Instance for KeymanagerService
	 */
	@Autowired
	PartnerCertificateManagerService partnerCertManagerService;

     /**
	 * To Upload CA/Sub-CA certificates
	 * 
	 * @param caCertRequestDto {@link CACertificateRequestDto} request
	 * @return {@link CACertficateResponseDto} Upload Success
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL', 'PMS_ADMIN')")
	@ResponseFilter
	@PostMapping(value = "/uploadCACertificate", produces = "application/json")
	public ResponseWrapper<CACertificateResponseDto> uploadCACertificate(
			@ApiParam("Upload CA/Sub-CA certificates.") @RequestBody @Valid RequestWrapper<CACertificateRequestDto> caCertRequestDto) {

		ResponseWrapper<CACertificateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(partnerCertManagerService.uploadCACertificate(caCertRequestDto.getRequest()));
		return response;
    }
    
    /**
	 * To Upload Partner Certificate.
	 * 
	 * @param partnerCertRequestDto {@link PartnerCertificateRequestDto} request
	 * @return {@link PartnerCertificateResponseDto} signed certificate response
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL', 'ID_AUTHENTICATION', 'PMS_USER')")
	@ResponseFilter
	@PostMapping(value = "/uploadPartnerCertificate", produces = "application/json")
	public ResponseWrapper<PartnerCertificateResponseDto> uploadPartnerCertificate(
			@ApiParam("Upload Partner Certificates.") @RequestBody @Valid RequestWrapper<PartnerCertificateRequestDto> partnerCertRequestDto) {

		ResponseWrapper<PartnerCertificateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(partnerCertManagerService.uploadPartnerCertificate(partnerCertRequestDto.getRequest()));
		return response;
	}

    /**
	 * To Download Partner Certificate.
	 * 
	 * @param certDownloadRequestDto {@link PartnerCertDownloadRequestDto} request
	 * @return {@link PartnerCertDownloadResponeDto} encrypted Data
	 */
	//@PreAuthorize("hasAnyRole('INDIVIDUAL', 'ID_AUTHENTICATION', 'PMS_USER')")
	@ResponseFilter
	@GetMapping(value = "/getPartnerCertificate/{partnerCertId}")
	public ResponseWrapper<PartnerCertDownloadResponeDto> getPartnerCertificate(
			@ApiParam("To download re-signed partner certificate.") @PathVariable("partnerCertId") String partnerCertId) {
		PartnerCertDownloadRequestDto certDownloadRequestDto = new PartnerCertDownloadRequestDto();
		certDownloadRequestDto.setPartnerCertId(partnerCertId);
		ResponseWrapper<PartnerCertDownloadResponeDto> response = new ResponseWrapper<>();
		response.setResponse(partnerCertManagerService.getPartnerCertificate(certDownloadRequestDto));
		return response;
    }
    
}