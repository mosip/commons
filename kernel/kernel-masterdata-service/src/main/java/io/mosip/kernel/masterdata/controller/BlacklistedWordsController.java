package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.constant.MachinePutReqDto;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.OrderEnum;
import io.mosip.kernel.masterdata.dto.BlackListedWordsUpdateDto;
import io.mosip.kernel.masterdata.dto.BlacklistedWordListRequestDto;
import io.mosip.kernel.masterdata.dto.BlacklistedWordsDto;
import io.mosip.kernel.masterdata.dto.MachineDto;
import io.mosip.kernel.masterdata.dto.MachinePostReqDto;
import io.mosip.kernel.masterdata.dto.getresponse.BlacklistedWordsResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.BlacklistedWordsExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.MachineSearchDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.id.WordAndLanguageCodeID;
import io.mosip.kernel.masterdata.service.BlacklistedWordsService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller that provides with methods for operations on blacklisted words.
 * 
 * @author Abhishek Kumar
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
@RestController
@Api(tags = { "BlacklistedWords" })
@RequestMapping("/blacklistedwords")
public class BlacklistedWordsController {
	@Autowired
	private BlacklistedWordsService blacklistedWordsService;

	@Autowired
	private AuditUtil auditUtil;

	/**
	 * Fetch the list of blacklisted words based on language code.
	 * 
	 * @param langCode language code
	 * @return {@link BlacklistedWordsResponseDto}
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','ZONAL_ADMIN','ZONAL_APPROVER')")
	@ResponseFilter
	@GetMapping("/{langcode}")
	public ResponseWrapper<BlacklistedWordsResponseDto> getAllBlackListedWordByLangCode(
			@PathVariable("langcode") String langCode) {

		ResponseWrapper<BlacklistedWordsResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(blacklistedWordsService.getAllBlacklistedWordsBylangCode(langCode));
		return responseWrapper;
	}

	/**
	 * Takes the list of string as an argument and checks if the list contains any
	 * blacklisted words.
	 * 
	 * @param blacklistedwords list of blacklisted words
	 * @return Valid if word does not belongs to black listed word and Invalid if
	 *         word belongs to black listed word
	 */
	@ResponseFilter
	@PostMapping(path = "/words")
	@ApiOperation(value = "Black listed word validation")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Valid Word"),
			@ApiResponse(code = 200, message = "Invalid Word") })
	public ResponseWrapper<CodeResponseDto> validateWords(
			@RequestBody RequestWrapper<BlacklistedWordListRequestDto> blacklistedwords) {

		String isValid = "Valid";
		if (!blacklistedWordsService.validateWord(blacklistedwords.getRequest().getBlacklistedwords())) {
			isValid = "Invalid";
		}
		CodeResponseDto dto = new CodeResponseDto();
		dto.setCode(isValid);

		ResponseWrapper<CodeResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);
		return responseWrapper;
	}

	/**
	 * Method to add blacklisted word.
	 * 
	 * @param blackListedWordsRequestDto the request dto that holds the blacklisted
	 *                                   word to be added.
	 * @return the response entity i.e. the word and language code of the word
	 *         added.
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN')")
	@ResponseFilter
	@PostMapping
	public ResponseWrapper<WordAndLanguageCodeID> createBlackListedWord(
			@RequestBody @Valid RequestWrapper<BlacklistedWordsDto> blackListedWordsRequestDto) {
		auditUtil.auditRequest(
				MasterDataConstant.CREATE_API_IS_CALLED + BlacklistedWordListRequestDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.CREATE_API_IS_CALLED + BlacklistedWordListRequestDto.class.getCanonicalName(),
				"ADM-545");
		ResponseWrapper<WordAndLanguageCodeID> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(blacklistedWordsService.createBlackListedWord(blackListedWordsRequestDto.getRequest()));
		return responseWrapper;
	}

	/**
	 * Method to update the blacklisted word
	 * 
	 * @param blackListedWordsRequestDto the request dto that holds the blacklisted
	 *                                   word to be updated .
	 * @return the response entity i.e. the word and language code of the word
	 *         updated.
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN')")
	@ResponseFilter
	@PutMapping
	@ApiOperation(value = "update the blacklisted word")
	public ResponseWrapper<WordAndLanguageCodeID> updateBlackListedWord(
			@Valid @RequestBody RequestWrapper<BlackListedWordsUpdateDto> blackListedWordsRequestDto) {
		auditUtil.auditRequest(
				MasterDataConstant.UPDATE_API_IS_CALLED + BlackListedWordsUpdateDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.UPDATE_API_IS_CALLED + BlackListedWordsUpdateDto.class.getCanonicalName(),
				"ADM-546");
		ResponseWrapper<WordAndLanguageCodeID> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(blacklistedWordsService.updateBlackListedWord(blackListedWordsRequestDto.getRequest()));
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN')")
	@ResponseFilter
	@PutMapping(path = "/details")
	@ApiOperation(value = "update the blacklisted word details except word")
	public ResponseWrapper<WordAndLanguageCodeID> updateBlackListedWordExceptWord(
			@Valid @RequestBody RequestWrapper<BlacklistedWordsDto> blackListedWordsRequestDto) {
		auditUtil.auditRequest(
				MasterDataConstant.UPDATE_API_IS_CALLED + BlackListedWordsUpdateDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.UPDATE_API_IS_CALLED + BlackListedWordsUpdateDto.class.getCanonicalName(),
				"ADM-547");
		ResponseWrapper<WordAndLanguageCodeID> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				blacklistedWordsService.updateBlackListedWordExceptWord(blackListedWordsRequestDto.getRequest()));
		return responseWrapper;
	}

	/**
	 * Method to deleted blacklisted word.
	 * 
	 * @param word input blacklisted word to be deleted.
	 * @return deleted word.
	 */
	@ResponseFilter
	@DeleteMapping("/{word}")
	@ApiOperation(value = "delete the blacklisted word")
	public ResponseWrapper<CodeResponseDto> deleteBlackListedWord(@PathVariable("word") String word) {
		CodeResponseDto dto = new CodeResponseDto();//
		dto.setCode(blacklistedWordsService.deleteBlackListedWord(word));

		ResponseWrapper<CodeResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dto);
		return responseWrapper;
	}

	/**
	 * Api to get all the blacklisted words
	 * 
	 * @return list of {@link BlacklistedWordsDto}
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','CENTRAL_ADMIN')")
	@ResponseFilter
	@GetMapping("/all")
	@ApiOperation(value = "Retrieve all the blacklisted words with additional metadata", notes = "Retrieve all the blacklisted words with metadata")
	@ApiResponses({ @ApiResponse(code = 200, message = "list of blacklistedwords"),
			@ApiResponse(code = 500, message = "Error occured while retrieving blacklisted words") })
	public ResponseWrapper<PageDto<BlacklistedWordsExtnDto>> getAllBlacklistedWords(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page no for the requested data", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size for the requested data", defaultValue = "10") int pageSize,
			@RequestParam(name = "sortBy", defaultValue = "createdDateTime") @ApiParam(value = "sort the requested data based on param value", defaultValue = "createdDateTime") String sortBy,
			@RequestParam(name = "orderBy", defaultValue = "desc") @ApiParam(value = "order the requested data based on param", defaultValue = "desc") OrderEnum orderBy) {
		ResponseWrapper<PageDto<BlacklistedWordsExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(blacklistedWordsService.getBlackListedWords(pageNumber, pageSize, sortBy, orderBy.name()));
		return responseWrapper;
	}

	/**
	 * API to search BlackListedWords.
	 * 
	 * @param request the request DTO {@link SearchDto} wrapped in
	 *                {@link RequestWrapper}.
	 * @return the response i.e. multiple entities based on the search values
	 *         required.
	 */
	@ResponseFilter
	@PostMapping("/search")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN')")
	public ResponseWrapper<PageResponseDto<BlacklistedWordsExtnDto>> searchBlackListedWords(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		auditUtil.auditRequest(
				MasterDataConstant.SEARCH_API_IS_CALLED + BlacklistedWordsExtnDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.SEARCH_API_IS_CALLED + BlacklistedWordsExtnDto.class.getCanonicalName(), "ADM-548");
		ResponseWrapper<PageResponseDto<BlacklistedWordsExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(blacklistedWordsService.searchBlackListedWords(request.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH, BlacklistedWordsExtnDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH_DESC, MachineSearchDto.class.getCanonicalName()),
				"ADM-549");
		return responseWrapper;
	}

	/**
	 * API that returns the values required for the column filter columns.
	 * 
	 * @param request the request DTO {@link FilterResponseDto} wrapper in
	 *                {@link RequestWrapper}.
	 * @return the response i.e. the list of values for the specific filter column
	 *         name and type.
	 */
	@ResponseFilter
	@PostMapping("/filtervalues")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN')")
	public ResponseWrapper<FilterResponseDto> blackListedWordsFilterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> requestWrapper) {
		auditUtil.auditRequest(
				MasterDataConstant.FILTER_API_IS_CALLED + BlacklistedWordsExtnDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.FILTER_API_IS_CALLED + BlacklistedWordsExtnDto.class.getCanonicalName(), "ADM-550");
		ResponseWrapper<FilterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(blacklistedWordsService.blackListedWordsFilterValues(requestWrapper.getRequest()));
		auditUtil.auditRequest(MasterDataConstant.SUCCESSFUL_FILTER + BlacklistedWordsExtnDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.SUCCESSFUL_FILTER_DESC + BlacklistedWordsExtnDto.class.getCanonicalName(),
				"ADM-551");
		return responseWrapper;
	}
}
