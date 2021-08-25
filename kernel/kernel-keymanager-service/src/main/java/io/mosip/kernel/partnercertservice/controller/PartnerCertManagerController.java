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
import io.mosip.kernel.partnercertservice.dto.CACertificateRequestDto;
import io.mosip.kernel.partnercertservice.dto.CACertificateResponseDto;
import io.mosip.kernel.partnercertservice.dto.CertificateTrustRequestDto;
import io.mosip.kernel.partnercertservice.dto.CertificateTrustResponeDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertDownloadRequestDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertDownloadResponeDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertificateRequestDto;
import io.mosip.kernel.partnercertservice.dto.PartnerCertificateResponseDto;
import io.mosip.kernel.partnercertservice.service.spi.PartnerCertificateManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Rest Controller for Partner Certificate Management includes certificate Validation and certificate Storage.
 * 
 * @author Mahammed Taheer
 *
 * @since 1.1.2
 */

@CrossOrigin
@RestController
@Tag(name = "partnercertmanager", description = "Operation related to partner certificate management")
public class PartnerCertManagerController {
	
	/**
	 * Instance for PartnerCertificateManagerService
	 */
	@Autowired
	PartnerCertificateManagerService partnerCertManagerService;

     /**
	 * To Upload CA/Sub-CA certificates
	 * 
	 * @param caCertRequestDto {@link CACertificateRequestDto} request
	 * @return {@link CACertficateResponseDto} Upload Success
	 */
		// @PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','INDIVIDUAL',
		// 'PMS_ADMIN')")
	@Operation(summary = "To Upload CA/Sub-CA certificates", description = "To Upload CA/Sub-CA certificates", tags = { "partnercertmanager" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','PMS_ADMIN')")
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
	// @PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','INDIVIDUAL',
	// 'ID_AUTHENTICATION', 'PMS_USER')")
	@Operation(summary = "To Upload Partner Certificate", description = "To Upload Partner Certificate", tags = { "partnercertmanager" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','PMS_ADMIN','PMS_USER')")
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
	// @PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','INDIVIDUAL',
	// 'ID_AUTHENTICATION', 'PMS_USER')")
	@Operation(summary = "To Download Partner Certificate", description = "To Download Partner Certificate", tags = { "partnercertmanager" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','PMS_ADMIN','PMS_USER')")
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
	
	/**
	 * To Upload Partner Certificate.
	 * 
	 * @param certificateTrustRequestDto {@CertificateTrustRequestDto CertificateTrustDto} request
	 * @return {@link CertificateTrustResponeDto} certificate verify response
	 */
	// @PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','INDIVIDUAL',
	// 'ID_AUTHENTICATION', 'PMS_USER')")
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','PMS_ADMIN','PMS_USER')")
	@ResponseFilter
	@PostMapping(value = "/verifyCertificateTrust", produces = "application/json")
	@Operation(summary = "To Upload Partner Certificate", description = "To Upload Partner Certificate", tags = { "partnercertmanager" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Success or you may find errors in error array in response"),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "403", description = "Forbidden", content = @Content(schema = @Schema(hidden = true))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(hidden = true))) })
	public ResponseWrapper<CertificateTrustResponeDto> verifyCertificateTrust(
			@ApiParam("Upload Partner Certificates.") @RequestBody @Valid RequestWrapper<CertificateTrustRequestDto> certificateTrustRequestDto) {

		ResponseWrapper<CertificateTrustResponeDto> response = new ResponseWrapper<>();
		response.setResponse(partnerCertManagerService.verifyCertificateTrust(certificateTrustRequestDto.getRequest()));
		return response;
	}
    
}