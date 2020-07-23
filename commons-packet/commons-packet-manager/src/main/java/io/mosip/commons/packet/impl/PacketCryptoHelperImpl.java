package io.mosip.commons.packet.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.commons.packet.dto.SignRequestDto;
import io.mosip.commons.packet.dto.SignResponseDto;
import io.mosip.commons.packet.exception.SignatureException;
import io.mosip.commons.packet.spi.IPacketCryptoHelper;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.DateUtils;

@Component
public class PacketCryptoHelperImpl implements IPacketCryptoHelper {

	@Autowired
	private Environment environment;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper mapper;

	private static final String DATETIME_PATTERN = "mosip.registration.processor.datetime.pattern";

	@Value("${KEYMANAGER_SIGN}")
	private String keymanagerSignUrl;

	@Override
	public byte[] sign(byte[] packet) {
		try {
		String packetData = new String(packet, StandardCharsets.UTF_8);
		SignRequestDto dto = new SignRequestDto();
		dto.setData(packetData);
		RequestWrapper<SignRequestDto> request = new RequestWrapper<>();
		request.setRequest(dto);
		request.setMetadata(null);
		DateTimeFormatter format = DateTimeFormatter.ofPattern(environment.getProperty(DATETIME_PATTERN));
		LocalDateTime localdatetime = LocalDateTime
				.parse(DateUtils.getUTCCurrentDateTimeString(environment.getProperty(DATETIME_PATTERN)), format);
		request.setRequesttime(localdatetime);
		HttpEntity<RequestWrapper<SignRequestDto>> httpEntity = new HttpEntity<>(request);
		ResponseEntity<String> response = restTemplate.exchange(keymanagerSignUrl, HttpMethod.POST, httpEntity,
				String.class);
			// To do need to check errors by wrapping response
			SignResponseDto responseObject = mapper.readValue(response.getBody(), SignResponseDto.class);
			String signedData = responseObject.getSignature();
			return signedData.getBytes();
		} catch (IOException e) {
			new SignatureException(e);
		}
		return null;
	}

	@Override
	public byte[] encrypt(byte[] packet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] decrypt(byte[] packet) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verify(byte[] packet) {
		// TODO Auto-generated method stub
		return false;
	}

}
