package io.mosip.kernel.biometrics.commons;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BiometricRecord;
import io.mosip.kernel.biometrics.entities.Entry;
import io.mosip.kernel.biometrics.model.JWTSignatureVerifyRequestDto;
import io.mosip.kernel.biometrics.model.JWTSignatureVerifyResponseDto;
import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;

@Component
public class BiometricsSignatureValidator {

	private static final String DATETIME_PATTERN = "mosip.registration.processor.datetime.pattern";

	public static final String TRUST_VALID = "TRUST_CERT_PATH_VALID";

	@Autowired
	private Environment env;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	RestTemplate restTemplate;

	@Value("${mosip.regproc.packet-validator.signature.disable-trust-validation:false}")
	private Boolean disableTrustValidation;

	public void validateSignature(BiometricRecord biometricRecord) throws JSONException, JsonParseException,
			JsonMappingException, JsonProcessingException, IOException, BaseCheckedException {

		List<BIR> birs = biometricRecord.getSegments();
		for (BIR bir : birs) {
			String sb = new String(bir.getSb(), StandardCharsets.UTF_8);
			String bdb = Base64.getUrlEncoder().encodeToString(bir.getBdb());
			if (bir.getOthers() != null) {
				for (Entry entry : bir.getOthers()) {
					if (entry.getKey().equals("PAYLOAD")) {
						entry.setValue(entry.getValue().replace("<bioValue>", bdb));
						String encodedPayloadValue = CryptoUtil.encodeBase64(entry.getValue().getBytes());
						String constructedJWTToken = constructJWTToken(sb, encodedPayloadValue);
						validateCertificateHeader(sb, new JSONObject(entry.getValue()));
						validateJWTToken(constructedJWTToken);
					}
				}
			}
		}

	}

	private void validateCertificateHeader(String sb, JSONObject jsonObject) throws JSONException, BaseCheckedException {
		String digitalIdHeader = new String(CryptoUtil.decodeBase64(jsonObject.getString("digitalId").split("\\.")[0]));
		JSONObject digitalHeaderCertificate = new JSONObject(digitalIdHeader);
		JSONArray digitalHeadercertificateJsonArray = digitalHeaderCertificate.getJSONArray("x5c");
		String digitalCertificate = digitalHeadercertificateJsonArray.getString(0);
		
		String sbHeader = new String(CryptoUtil.decodeBase64(sb.split("\\.")[0]));
		JSONObject sbHeaderCertificate = new JSONObject(sbHeader);
		JSONArray sbHeadercertificateJsonArray = sbHeaderCertificate.getJSONArray("x5c");
		String sbCertificate = sbHeadercertificateJsonArray.getString(0);
		
		if(!digitalCertificate.equalsIgnoreCase(sbCertificate)) {
			throw new BaseCheckedException("Header Certificate mismatch");
		}
	}

	private String constructJWTToken(String sb, String encodedPayloadValue) {
		return sb.replace("..", "." + encodedPayloadValue + ".");
	}

	@SuppressWarnings({ "rawtypes" })
	private void validateJWTToken(String token) throws JsonParseException, JsonMappingException,
			JsonProcessingException, IOException, JSONException, BaseCheckedException {
		JWTSignatureVerifyRequestDto jwtSignatureVerifyRequestDto = new JWTSignatureVerifyRequestDto();
		jwtSignatureVerifyRequestDto.setApplicationId("REGISTRATION");
		jwtSignatureVerifyRequestDto.setReferenceId("SIGN");
		jwtSignatureVerifyRequestDto.setJwtSignatureData(token);
		jwtSignatureVerifyRequestDto.setActualData(token.split("\\.")[1]);
		jwtSignatureVerifyRequestDto.setValidateTrust(!disableTrustValidation);
		jwtSignatureVerifyRequestDto.setDomain("Device");
		RequestWrapper<JWTSignatureVerifyRequestDto> request = new RequestWrapper<>();

		request.setRequest(jwtSignatureVerifyRequestDto);
		request.setVersion("1.0");
		DateTimeFormatter format = DateTimeFormatter.ofPattern(env.getProperty(DATETIME_PATTERN));
		LocalDateTime localdatetime = LocalDateTime
				.parse(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)), format);
		request.setRequesttime(localdatetime);
		
		String jwtVerifyrUrl = env.getProperty("JWTVERIFY");
		ResponseEntity<ResponseWrapper> response = restTemplate.postForEntity(jwtVerifyrUrl, request,
				ResponseWrapper.class);
		ResponseWrapper responseWrapper = response.getBody();
		if (responseWrapper.getResponse() != null) {
			JWTSignatureVerifyResponseDto jwtResponse = mapper.readValue(
					mapper.writeValueAsString(responseWrapper.getResponse()), JWTSignatureVerifyResponseDto.class);

			if (!jwtResponse.isSignatureValid()) {
				throw new BaseCheckedException("JWT signature Validation Failed");
			} else {
				if (!disableTrustValidation && !jwtResponse.getTrustValid().contentEquals(TRUST_VALID)) {
					throw new BaseCheckedException("JWT signature Validation Failed -->" + jwtResponse.getTrustValid());
				}
			}
		} else {
			List<ServiceError> errors = mapper.readValue(mapper.writeValueAsString(responseWrapper.getErrors()),
					new TypeReference<List<ServiceError>>() {
					});
			throw new BaseCheckedException(errors.get(0).getErrorCode(), errors.get(0).getMessage());
		}

	}

}
