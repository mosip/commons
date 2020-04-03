package io.mosip.idrepository.vid.controller;

import static io.mosip.idrepository.core.constant.IdRepoErrorConstants.DATA_VALIDATION_FAILED;
import static io.mosip.idrepository.core.constant.IdRepoErrorConstants.INVALID_INPUT_PARAMETER;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.idrepository.core.constant.AuditEvents;
import io.mosip.idrepository.core.constant.AuditModules;
import io.mosip.idrepository.core.constant.IdType;
import io.mosip.idrepository.core.dto.VidRequestDTO;
import io.mosip.idrepository.core.dto.VidResponseDTO;
import io.mosip.idrepository.core.exception.IdRepoAppException;
import io.mosip.idrepository.core.exception.IdRepoDataValidationException;
import io.mosip.idrepository.core.helper.AuditHelper;
import io.mosip.idrepository.core.logger.IdRepoLogger;
import io.mosip.idrepository.core.security.IdRepoSecurityManager;
import io.mosip.idrepository.core.spi.VidService;
import io.mosip.idrepository.core.util.DataValidationUtil;
import io.mosip.idrepository.vid.validator.VidRequestValidator;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.logger.spi.Logger;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The Class Vid Controller - controller class for vid service.
 * These services can be used to perform various operations on 
 * VID like generate or re-generate VID, update VID status, etc.
 *
 * @author Manoj SP
 * @author Prem Kumar
 */
@RestController
public class VidController {

	/** The Constant REACTIVATE. */
	private static final String REACTIVATE = "reactivate";

	/** The Constant DEACTIVATE. */
	private static final String DEACTIVATE = "deactivate";

	/** The Constant UIN. */
	private static final String UIN = "uin";

	/** The Constant DEACTIVATE_VID. */
	private static final String DEACTIVATE_VID = "deactivateVid";

	/** The Constant VID. */
	private static final String VID = "vid";

	/** The Constant REGENERATE. */
	private static final String REGENERATE = "regenerate";

	/** The Constant REGENERATE_VID. */
	private static final String REGENERATE_VID = "regenerateVid";

	/** The Constant RETRIEVE_UIN_BY_VID. */
	private static final String RETRIEVE_UIN_BY_VID = "retrieveUinByVid";

	/** The Constant UPDATE_VID_STATUS. */
	private static final String UPDATE_VID_STATUS = "updateVidStatus";

	/** The Constant VID_CONTROLLER. */
	private static final String VID_CONTROLLER = "VidController";

	/** The Constant CREATE. */
	private static final String CREATE = "create";

	/** The Constant UPDATE. */
	private static final String UPDATE = "update";

	/** The Vid Service. */
	@Autowired
	private VidService<VidRequestDTO, ResponseWrapper<VidResponseDTO>> vidService;

	/** The Vid Request Validator. */
	@Autowired
	private VidRequestValidator validator;

	/** The Audit Helper. */
	@Autowired
	private AuditHelper auditHelper;

	/** The mosip logger. */
	Logger mosipLogger = IdRepoLogger.getLogger(VidController.class);

	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(validator);
	}

	/**
	 * This service will generate a new VID based on VID type provided.
	 *
	 * @param request the request
	 * @param errors the errors
	 * @return the response entity
	 * @throws IdRepoAppException the id repo app exception
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR','RESIDENT')")
	@PostMapping(path = "/vid", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> createVid(
			@Validated @RequestBody RequestWrapper<VidRequestDTO> request, @ApiIgnore Errors errors)
			throws IdRepoAppException {
		String uin = Optional.ofNullable(request.getRequest()).map(req -> String.valueOf(req.getUin())).orElse("null");
		try {
			validator.validateId(request.getId(), CREATE);
			DataValidationUtil.validate(errors);
			request.getRequest().setVidType(request.getRequest().getVidType().toUpperCase());
			return new ResponseEntity<>(vidService.generateVid(request.getRequest()), HttpStatus.OK);
		} catch (IdRepoAppException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.CREATE_VID, uin, IdType.VID, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, RETRIEVE_UIN_BY_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e, CREATE);
		} finally {
			String vidType = Optional.ofNullable(request.getRequest()).map(req -> String.valueOf(req.getVidType()))
					.orElse("null");
			auditHelper.audit(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.CREATE_VID, uin, IdType.VID,
					"Create VID requested for " + vidType);
		}
	}

	/**
	 * This method will accepts vid as parameter, if vid is valid it will return
	 * respective uin. This service will retrieve associated decrypted UIN for a given 
	 * VID, once VID is successfully validated.
	 *
	 * @param vid the vid
	 * @return the response entity
	 * @throws IdRepoAppException the id repo app exception
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR','ID_AUTHENTICATION','RESIDENT')")
	@GetMapping(path = "/vid/{VID}", consumes = MediaType.ALL_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> retrieveUinByVid(@PathVariable("VID") String vid)
			throws IdRepoAppException {
		try {
			validator.validateVid(vid);
			return new ResponseEntity<>(vidService.retrieveUinByVid(vid), HttpStatus.OK);
		} catch (InvalidIDException e) {
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, RETRIEVE_UIN_BY_VID, e.getMessage());
			throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), VID));
		} catch (IdRepoAppException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.RETRIEVE_VID_UIN, vid, IdType.VID, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, RETRIEVE_UIN_BY_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		} finally {
			auditHelper.audit(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.RETRIEVE_VID_UIN, vid, IdType.VID,
					"Retrieve Uin By VID requested");
		}
	}

	/**
	 * This Method accepts VidRequest body as parameter and vid from url then it
	 * will update the status if it is an valid vid. This service will update status 
	 * associated with a given VID, if the current status of VID is 'ACTIVE'.
	 *
	 * @param vid the vid
	 * @param request the request
	 * @param errors the errors
	 * @return the response entity
	 * @throws IdRepoAppException the id repo app exception
	 */
	@PreAuthorize("hasAnyRole('ID_AUTHENTICATION')")
	@PatchMapping(path = "/vid/{VID}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> updateVidStatus(@PathVariable("VID") String vid,
			@Validated @RequestBody RequestWrapper<VidRequestDTO> request, @ApiIgnore Errors errors)
			throws IdRepoAppException {
		try {
			validator.validateId(request.getId(), UPDATE);
			validator.validateVid(vid);
			DataValidationUtil.validate(errors);
			return new ResponseEntity<>(vidService.updateVid(vid, request.getRequest()), HttpStatus.OK);
		} catch (InvalidIDException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.UPDATE_VID_STATUS, vid, IdType.VID, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, UPDATE_VID_STATUS, e.getMessage());
			throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), VID));
		} catch (IdRepoDataValidationException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.UPDATE_VID_STATUS, vid, IdType.VID, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, UPDATE_VID_STATUS, e.getMessage());
			throw new IdRepoAppException(DATA_VALIDATION_FAILED, e);
		} catch (IdRepoAppException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.UPDATE_VID_STATUS, vid, IdType.VID, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, UPDATE_VID_STATUS, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		} finally {
			auditHelper.audit(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.UPDATE_VID_STATUS, vid, IdType.VID,
					"Update VID requested");
		}
	}

	/**
	 * This method will accepts an vid, if vid is valid to regenerate then
	 * regenerated vid will be returned as response. This service will re-generate 
	 * VID for a given VID, only if the current status of VID is 'ACTIVE', 'USED', 
	 * or 'EXPIRED'.
	 *
	 * @param vid the vid
	 * @return the response entity
	 * @throws IdRepoAppException the id repo app exception
	 */
	@PreAuthorize("hasAnyRole('RESIDENT')")
	@PostMapping(path = "/vid/{VID}/regenerate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> regenerateVid(@PathVariable("VID") String vid)
			throws IdRepoAppException {
		try {
			validator.validateVid(vid);
			return new ResponseEntity<>(vidService.regenerateVid(vid), HttpStatus.OK);
		} catch (InvalidIDException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.REGENERATE_VID, vid, IdType.VID, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, REGENERATE_VID, e.getMessage());
			throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), VID));
		} catch (IdRepoAppException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.REGENERATE_VID, vid, IdType.VID, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, REGENERATE_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e, REGENERATE);
		} finally {
			auditHelper.audit(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.REGENERATE_VID, vid, IdType.VID,
					"Regenerate VID requested");
		}
	}

	/**
	 * This method will accept an uin, if uin is valid then it will deactivate all
	 * the respective vid's. This service will de-activate VIDs mapped against the 
	 * provided UIN, only if the current status of VID is 'ACTIVE'.
	 *
	 * @param request the request
	 * @param errors the errors
	 * @return the response entity
	 * @throws IdRepoAppException the id repo app exception
	 */
	@PreAuthorize("hasAnyRole('RESIDENT')")
	@PostMapping(path = "/vid/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> deactivateVIDsForUIN(
			@Validated @RequestBody RequestWrapper<VidRequestDTO> request, @ApiIgnore Errors errors)
			throws IdRepoAppException {
		String uin = Optional.ofNullable(request.getRequest()).map(req -> String.valueOf(req.getUin())).orElse("null");
		try {
			validator.validateId(request.getId(), DEACTIVATE);
			DataValidationUtil.validate(errors);
			return new ResponseEntity<>(vidService.deactivateVIDsForUIN(String.valueOf(request.getRequest().getUin())),
					HttpStatus.OK);
		} catch (InvalidIDException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.DEACTIVATE_VID, uin, IdType.UIN, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, DEACTIVATE_VID, e.getMessage());
			throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), UIN));
		} catch (IdRepoAppException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.DEACTIVATE_VID, uin, IdType.UIN, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, DEACTIVATE_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e, DEACTIVATE);
		} finally {
			auditHelper.audit(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.DEACTIVATE_VID, uin, IdType.UIN,
					"Deactivate VID Requested");
		}
	}

	/**
	 * This method will accept an uin, if uin is valid then it will reactivate all
	 * the respective vid's. This service will re-activate VIDs mapped against the 
	 * provided UIN, only if the current status of VID is 'DEACTIVATED', 'INACTIVE' 
	 * and not 'EXPIRED'.
	 *
	 * @param request the request
	 * @param errors the errors
	 * @return the response entity
	 * @throws IdRepoAppException the id repo app exception
	 */
	@PreAuthorize("hasAnyRole('RESIDENT')")
	@PostMapping(path = "/vid/reactivate", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper<VidResponseDTO>> reactivateVIDsForUIN(
			@Validated @RequestBody RequestWrapper<VidRequestDTO> request, @ApiIgnore Errors errors)
			throws IdRepoAppException {
		String uin = Optional.ofNullable(request.getRequest()).map(req -> String.valueOf(req.getUin())).orElse("null");
		try {
			validator.validateId(request.getId(), REACTIVATE);
			DataValidationUtil.validate(errors);
			return new ResponseEntity<>(vidService.reactivateVIDsForUIN(String.valueOf(request.getRequest().getUin())),
					HttpStatus.OK);
		} catch (InvalidIDException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.REACTIVATE_VID, uin, IdType.UIN, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, DEACTIVATE_VID, e.getMessage());
			throw new IdRepoAppException(INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(INVALID_INPUT_PARAMETER.getErrorMessage(), UIN));
		} catch (IdRepoAppException e) {
			auditHelper.auditError(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.REACTIVATE_VID, uin, IdType.UIN, e);
			mosipLogger.error(IdRepoSecurityManager.getUser(), VID_CONTROLLER, DEACTIVATE_VID, e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e, REACTIVATE);
		} finally {
			auditHelper.audit(AuditModules.ID_REPO_VID_SERVICE, AuditEvents.REACTIVATE_VID, uin, IdType.UIN,
					"Reactivate VID Requested");
		}
	}
}