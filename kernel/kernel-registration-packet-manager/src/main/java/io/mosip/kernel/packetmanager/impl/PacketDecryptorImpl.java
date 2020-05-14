package io.mosip.kernel.packetmanager.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.packetmanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.packetmanager.dto.CryptomanagerResponseDto;
import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.exception.PacketDecryptionFailureException;
import io.mosip.kernel.packetmanager.spi.PacketDecryptor;
import io.mosip.kernel.packetmanager.util.RestUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Decryptor class for packet decryption.
 *
 * @author Sowmya
 * 
 * @since 1.0.0
 */
@Component
public class PacketDecryptorImpl implements PacketDecryptor {

	@Value("${registration.processor.application.id}")
	private String applicationId;

	@Value("${mosip.kernel.machineid.length}")
	private int machineIdLength;

	@Value("${mosip.kernel.registrationcenterid.length}")
	private int centerIdLength;

	@Value("${registration.processor.rid.machineidsubstring}")
	private int machineIdSubStringLength;

	@Autowired
	private Environment environment;

	@Autowired
	private RestUtil restUtil;

	@Autowired
	private ObjectMapper mapper;

	private static final String DECRYPT_SERVICE_ID = "mosip.registration.processor.crypto.decrypt.id";
	private static final String REG_PROC_APPLICATION_VERSION = "mosip.registration.processor.application.version";
	private static final String DATETIME_PATTERN = "mosip.registration.processor.datetime.pattern";

	private static final String DECRYPTION_SUCCESS = "Decryption success";
	private static final String DECRYPTION_FAILURE = "Virus scan decryption failed for registrationId ";
	private static final String IO_EXCEPTION = "Exception while reading packet inputStream";
	private static final String DATE_TIME_EXCEPTION = "Error while parsing packet timestamp";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.processor.core.spi.decryptor.Decryptor#decrypt(java.io.
	 * InputStream, java.lang.String)
	 */
	@Override
	public InputStream decrypt(InputStream encryptedPacket, String registrationId)
			throws PacketDecryptionFailureException, ApiNotAccessibleException {
		InputStream outstream = null;

		try {
			String centerId = registrationId.substring(0, centerIdLength);
			String machineId = registrationId.substring(centerIdLength, machineIdSubStringLength);
			String refId = centerId + "_" + machineId;
			String encryptedPacketString = IOUtils.toString(encryptedPacket, "UTF-8");
			CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
			RequestWrapper<CryptomanagerRequestDto> request = new RequestWrapper<>();
			cryptomanagerRequestDto.setApplicationId(applicationId);
			cryptomanagerRequestDto.setData(encryptedPacketString);
			cryptomanagerRequestDto.setReferenceId(refId);
			// setLocal Date Time
			if (registrationId.length() > 14) {
				String packetCreatedDateTime = registrationId.substring(registrationId.length() - 14);
				String formattedDate = packetCreatedDateTime.substring(0, 8) + "T"
						+ packetCreatedDateTime.substring(packetCreatedDateTime.length() - 6);

				cryptomanagerRequestDto.setTimeStamp(
						LocalDateTime.parse(formattedDate, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")));
			} else {
				throw new PacketDecryptionFailureException("Packet DecryptionFailed-Invalid Packet format");
			}
			request.setId(environment.getProperty(DECRYPT_SERVICE_ID));
			request.setMetadata(null);
			request.setRequest(cryptomanagerRequestDto);
			DateTimeFormatter format = DateTimeFormatter.ofPattern(environment.getProperty(DATETIME_PATTERN));
			LocalDateTime localdatetime = LocalDateTime
					.parse(DateUtils.getUTCCurrentDateTimeString(environment.getProperty(DATETIME_PATTERN)), format);
			request.setRequesttime(localdatetime);
			request.setVersion(environment.getProperty(REG_PROC_APPLICATION_VERSION));
			HttpEntity<RequestWrapper<CryptomanagerRequestDto>> httpEntity = new HttpEntity<>(request,
					new HttpHeaders());

			String responseString = restUtil.postApi(environment.getProperty("CRYPTOMANAGERDECRYPT"), MediaType.APPLICATION_JSON, httpEntity, String.class);

			CryptomanagerResponseDto responseObject = mapper.readValue(responseString, CryptomanagerResponseDto.class);

			if (responseObject != null &&
					responseObject.getErrors() != null && !responseObject.getErrors().isEmpty()) {
				ServiceError error = responseObject.getErrors().get(0);
				throw new PacketDecryptionFailureException(error.getMessage());
			}
			byte[] decryptedPacket = CryptoUtil.decodeBase64(responseObject.getResponse().getData());
			outstream = new ByteArrayInputStream(decryptedPacket);

		} catch (IOException e) {
			throw new PacketDecryptionFailureException(IO_EXCEPTION, e);
		} catch (DateTimeParseException e) {
			throw new PacketDecryptionFailureException(DATE_TIME_EXCEPTION);
		} catch (Exception e) {
			if (e.getCause() instanceof HttpClientErrorException) {
				HttpClientErrorException httpClientException = (HttpClientErrorException) e.getCause();
				throw new ApiNotAccessibleException(httpClientException.getResponseBodyAsString());
			} else if (e.getCause() instanceof HttpServerErrorException) {
				HttpServerErrorException httpServerException = (HttpServerErrorException) e.getCause();
				throw new ApiNotAccessibleException(httpServerException.getResponseBodyAsString());
			} else {
				throw new PacketDecryptionFailureException(e);
			}

		}
		return outstream;
	}

}