//discussion on this optional api
package io.mosip.kernel.cryptosignature.test;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.signatureutil.exception.ParseResponseException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilClientException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilException;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.cryptosignature.dto.SignResponseDto;
import io.mosip.kernel.cryptosignature.dto.SignatureRequestDto;
import io.mosip.kernel.cryptosignature.service.impl.SignatureUtilImpl;

public class SignatureUtilImplTest {

	private String syncDataRequestId = "SIGNATURE.REQUEST";

	private String syncDataVersionId = "v1.0";

	private String signUrl = "http://localhost:8088/v1/keymanager/sign";

	private ObjectMapper objectMapper;

	private MockRestServiceServer server;

	private SignatureUtil signingUtil;

	private SignatureRequestDto cryptoManagerRequestDto;

	private RequestWrapper<SignatureRequestDto> requestWrapper;

	private RestTemplate restTemplate;

	@Before
	public void setUp() {
		restTemplate = new RestTemplate();
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new AfterburnerModule());
		objectMapper.registerModule(new JavaTimeModule());
		signingUtil = new SignatureUtilImpl();
		ReflectionTestUtils.setField(signingUtil, "restTemplate", restTemplate);
		ReflectionTestUtils.setField(signingUtil, "objectMapper", objectMapper);
		ReflectionTestUtils.setField(signingUtil, "signDataRequestId", syncDataRequestId);
		ReflectionTestUtils.setField(signingUtil, "signDataVersionId", syncDataVersionId);
		ReflectionTestUtils.setField(signingUtil, "signUrl", signUrl);
		server = MockRestServiceServer.bindTo(restTemplate).build();

		requestWrapper = new RequestWrapper<>();

		server = MockRestServiceServer.bindTo(restTemplate).build();

	}

	@Test
	public void signResponseData() throws JsonProcessingException {

		requestWrapper.setId(syncDataRequestId);
		requestWrapper.setVersion(syncDataVersionId);
		requestWrapper.setRequest(cryptoManagerRequestDto);
		SignResponseDto signatureResponse = new SignResponseDto();
		signatureResponse.setSignature(
				"jxAq1SysvWKK78C-2TduZDn2ACJLXReYjM4rWsd2KBSVat_wFxU5D_tiNUvI7gZ9hEGZbhcnQ5n0z8TsAMD3VYFc8WBVIjGsskE7ijhlVHjP3wsP4G1llj0eWcwLAido9K5iwSeeGbT7bJzsiVJTsqtZKRvHFj8gBW0T76jpviri2joYxJY3xD7f2HiA0dbVHzUiD5D8NkYZmQwlYMTeSNoHPYn2hq4Bt22YAjdIlQNNTxlUu1XM7P7eR-unRXXPsl9wDw6Gl1xzgN3SOE-WqmI3oIq61JvZiXhi_SKIt_RqMwymUHmTlb1MQfGB32ip6nPR1xdU3ArGRAuvYnmIGA");

		ResponseWrapper<SignResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(signatureResponse);
		String response = objectMapper.writeValueAsString(responseWrapper);
		server.expect(requestTo(signUrl))
				.andRespond(withSuccess().body(response).contentType(MediaType.APPLICATION_JSON));

		signingUtil.sign("MOSIP");
	}

	@Test(expected = SignatureUtilClientException.class)
	public void signResponseDataErrorTest() throws JsonProcessingException {

		requestWrapper.setId(syncDataRequestId);
		requestWrapper.setVersion(syncDataVersionId);
		requestWrapper.setRequest(cryptoManagerRequestDto);

		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		List<ServiceError> errors = new ArrayList<>();
		ServiceError serviceError = new ServiceError("KER-CRY-001", "No Such algorithm is supported");
		errors.add(serviceError);
		responseWrapper.setErrors(errors);

		String response = objectMapper.writeValueAsString(responseWrapper);
		server.expect(requestTo(signUrl))
				.andRespond(withSuccess().body(response).contentType(MediaType.APPLICATION_JSON));

		signingUtil.sign("MOSIP");
	}

	@Test(expected = ParseResponseException.class)
	public void signResponseDataExceptionTest() throws JsonProcessingException {

		String response = "{\"id\": \"string\",\"version\": \"string\",\"responsetime\": \"2019-04-06T12:52:32.450Z\",\"metadata\": null,\"response\": {\"data\": \"n7AvMtZ_nHb2AyD9IrXfA6sG9jc8IEgmkIYN2pVFaJ9Qw8v1JEMgneL0lVR-},\"errors\": null}";
		server.expect(requestTo(signUrl))
				.andRespond(withSuccess().body(response).contentType(MediaType.APPLICATION_JSON));

		signingUtil.sign("MOSIP");
	}

	@Test(expected = SignatureUtilClientException.class)
	public void signResponseDataClientTest() throws JsonProcessingException {
		ResponseWrapper<List<ServiceError>> responseWrapper = new ResponseWrapper<>();
		ServiceError serviceError = new ServiceError("KER-KYM-004", "No such alias found---->sdasd-dsfsdf-sdfdsf");
		List<ServiceError> serviceErrors = new ArrayList<>();
		serviceErrors.add(serviceError);
		responseWrapper.setErrors(serviceErrors);
		String response = objectMapper.writeValueAsString(responseWrapper);
		server.expect(requestTo(signUrl))
				.andRespond(withBadRequest().body(response).contentType(MediaType.APPLICATION_JSON));

		signingUtil.sign("MOSIP");
	}

	@Test(expected = SignatureUtilException.class)
	public void signResponseDataClientServiceErrorTest() throws JsonProcessingException {
		ResponseWrapper<List<ServiceError>> responseWrapper = new ResponseWrapper<>();
		List<ServiceError> serviceErrors = new ArrayList<>();
		responseWrapper.setErrors(serviceErrors);
		String response = objectMapper.writeValueAsString(responseWrapper);
		server.expect(requestTo(signUrl))
				.andRespond(withBadRequest().body(response).contentType(MediaType.APPLICATION_JSON));

		signingUtil.sign("MOSIP");
	}

	@Test(expected = SignatureUtilException.class)
	public void validateWithPublicKeyTest() throws InvalidKeySpecException, NoSuchAlgorithmException {

		signingUtil.validateWithPublicKey("MOCKSIGNATURE", "MOCKEDDATATOSIGN", "MOCKPUBLICKEY");
	}

	@Test(expected = SignatureUtilException.class)
	public void validateMethodTest() throws InvalidKeySpecException, NoSuchAlgorithmException, JsonProcessingException {
		boolean isVerfied = signingUtil.validate("MOCKSIGNATURE", "MOCKEDDATATOSIGN", "2019-09-09T09:09:09.000Z");
		assertTrue(isVerfied);
	}

}
