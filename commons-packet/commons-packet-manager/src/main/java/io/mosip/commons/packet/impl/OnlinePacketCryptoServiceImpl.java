package io.mosip.commons.packet.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.khazana.util.EncryptionUtil;
import io.mosip.commons.packet.constants.CryptomanagerConstant;
import io.mosip.commons.packet.dto.TpmSignVerifyRequestDto;
import io.mosip.commons.packet.dto.packet.CryptomanagerRequestDto;
import io.mosip.commons.packet.dto.packet.CryptomanagerResponseDto;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.commons.packet.exception.SignatureException;
import io.mosip.commons.packet.spi.IPacketCryptoService;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.clientcrypto.dto.TpmSignRequestDto;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;

@Component
@Qualifier("OnlinePacketCryptoServiceImpl")
public class OnlinePacketCryptoServiceImpl implements IPacketCryptoService {

    private static Logger LOGGER = PacketManagerLogger.getLogger(OnlinePacketCryptoServiceImpl.class);

    /**
     * The Constant APPLICATION_ID.
     */
    public static final String APPLICATION_ID = "REGISTRATION";
    private static final String DECRYPT_SERVICE_ID = "mosip.cryptomanager.decrypt";
    private static final String IO_EXCEPTION = "Exception while reading packet inputStream";
    private static final String DATE_TIME_EXCEPTION = "Error while parsing packet timestamp";

    @Value("${mosip.utc-datetime-pattern:yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}")
    private String DATETIME_PATTERN;

    @Value("${mosip.kernel.cryptomanager.request_version:v1}")
    private String APPLICATION_VERSION;

    @Value("${CRYPTOMANAGER_DECRYPT:null}")
    private String cryptomanagerDecryptUrl;

    @Value("${crypto.PrependThumbprint.enable:true}")
    private boolean isPrependThumbprintEnabled;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Value("${CRYPTOMANAGER_ENCRYPT:null}")
    private String cryptomanagerEncryptUrl;
    
    @Value("${mosip.kernel.keymanager-service-CsSign-url:null}")
    private String keymanagerCsSignUrl;
    
    @Value("${mosip.kernel.keymanager-service-csverifysign-url:null}")
    private String keymanagerCsverifysignUrl;
    
    @Value("${mosip.kernel.syncdata-service-get-tpm-publicKey-url:null}")
    private String syncdataGetTpmKeyUrl;
    
    @Override
    public byte[] sign(byte[] packet) {
        try {
            
        	TpmSignRequestDto dto = new TpmSignRequestDto();
            dto.setData(CryptoUtil.encodeBase64(packet));
            RequestWrapper<TpmSignRequestDto> request = new RequestWrapper<>();
            request.setRequest(dto);
            request.setMetadata(null);
            DateTimeFormatter format = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
            LocalDateTime localdatetime = LocalDateTime
                    .parse(DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN), format);
            request.setRequesttime(localdatetime);
            HttpEntity<RequestWrapper<TpmSignRequestDto>> httpEntity = new HttpEntity<>(request);
            ResponseEntity<String> response = restTemplate.exchange(keymanagerCsSignUrl, HttpMethod.POST, httpEntity,
                    String.class);
            LinkedHashMap responseMap = (LinkedHashMap) mapper.readValue(response.getBody(), LinkedHashMap.class).get("response");
            if (responseMap != null && responseMap.size() > 0)
                return CryptoUtil.decodeBase64((String) responseMap.get("data"));
            else
                throw new SignatureException();
        } catch (IOException e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, null,
                    ExceptionUtils.getStackTrace(e));
            throw new SignatureException(e);
        }
    }

    @Override
    public byte[] encrypt(String refId, byte[] packet) {
        byte[] encryptedPacket = null;

        try {
            String packetString = CryptoUtil.encodeBase64String(packet);
            CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
            RequestWrapper<CryptomanagerRequestDto> request = new RequestWrapper<>();
            cryptomanagerRequestDto.setApplicationId(APPLICATION_ID);
            cryptomanagerRequestDto.setData(packetString);
            cryptomanagerRequestDto.setReferenceId(refId);
            cryptomanagerRequestDto.setPrependThumbprint(isPrependThumbprintEnabled);

            SecureRandom sRandom = new SecureRandom();
            byte[] nonce = new byte[CryptomanagerConstant.GCM_NONCE_LENGTH];
            byte[] aad = new byte[CryptomanagerConstant.GCM_AAD_LENGTH];
            sRandom.nextBytes(nonce);
            sRandom.nextBytes(aad);
            cryptomanagerRequestDto.setAad(CryptoUtil.encodeBase64String(aad));
            cryptomanagerRequestDto.setSalt(CryptoUtil.encodeBase64String(nonce));
            cryptomanagerRequestDto.setTimeStamp(DateUtils.getUTCCurrentDateTime());

            request.setId(DECRYPT_SERVICE_ID);
            request.setMetadata(null);
            request.setRequest(cryptomanagerRequestDto);
            DateTimeFormatter format = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
            LocalDateTime localdatetime = LocalDateTime
                    .parse(DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN), format);
            request.setRequesttime(localdatetime);
            request.setVersion(APPLICATION_VERSION);
            HttpEntity<RequestWrapper<CryptomanagerRequestDto>> httpEntity = new HttpEntity<>(request);

            ResponseEntity<String> response = restTemplate.exchange(cryptomanagerEncryptUrl, HttpMethod.POST, httpEntity, String.class);
            CryptomanagerResponseDto responseObject = mapper.readValue(response.getBody(), CryptomanagerResponseDto.class);
            if (responseObject != null &&
                    responseObject.getErrors() != null && !responseObject.getErrors().isEmpty()) {
                LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                        "Packet encryption failed");
                ServiceError error = responseObject.getErrors().get(0);
                LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                        "Packet encryption failure message : " + error.getMessage());
                throw new PacketDecryptionFailureException(error.getMessage());
            }
            byte[] encryptedData = CryptoUtil.decodeBase64(responseObject.getResponse().getData());
            encryptedPacket = EncryptionUtil.mergeEncryptedData(encryptedData, nonce, aad);
            LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                    "Successfully encrypted Packet");
        } catch (IOException e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                    ExceptionUtils.getStackTrace(e));
            throw new PacketDecryptionFailureException(IO_EXCEPTION, e);
        } catch (DateTimeParseException e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                    ExceptionUtils.getStackTrace(e));
            throw new PacketDecryptionFailureException(DATE_TIME_EXCEPTION);
        } catch (Exception e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                    ExceptionUtils.getStackTrace(e));
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
        return encryptedPacket;
    }

    @Override
    public byte[] decrypt(String refId, byte[] packet) {
        byte[] decryptedPacket = null;

        try {
            CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
            RequestWrapper<CryptomanagerRequestDto> request = new RequestWrapper<>();
            cryptomanagerRequestDto.setApplicationId(APPLICATION_ID);
            cryptomanagerRequestDto.setReferenceId(refId);
            byte[] nonce = Arrays.copyOfRange(packet, 0, CryptomanagerConstant.GCM_NONCE_LENGTH);
            byte[] aad = Arrays.copyOfRange(packet, CryptomanagerConstant.GCM_NONCE_LENGTH,
                                            CryptomanagerConstant.GCM_NONCE_LENGTH + CryptomanagerConstant.GCM_AAD_LENGTH);
            byte[] encryptedData = Arrays.copyOfRange(packet, CryptomanagerConstant.GCM_NONCE_LENGTH + CryptomanagerConstant.GCM_AAD_LENGTH,
                                        packet.length);
            cryptomanagerRequestDto.setAad(CryptoUtil.encodeBase64String(aad));
            cryptomanagerRequestDto.setSalt(CryptoUtil.encodeBase64String(nonce));
            cryptomanagerRequestDto.setData(CryptoUtil.encodeBase64String(encryptedData));
            cryptomanagerRequestDto.setPrependThumbprint(isPrependThumbprintEnabled);
            cryptomanagerRequestDto.setTimeStamp(DateUtils.getUTCCurrentDateTime());

            request.setId(DECRYPT_SERVICE_ID);
            request.setMetadata(null);
            request.setRequest(cryptomanagerRequestDto);
            DateTimeFormatter format = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
            LocalDateTime localdatetime = LocalDateTime
                    .parse(DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN), format);
            request.setRequesttime(localdatetime);
            request.setVersion(APPLICATION_VERSION);
            HttpEntity<RequestWrapper<CryptomanagerRequestDto>> httpEntity = new HttpEntity<>(request);

            ResponseEntity<String> response = restTemplate.exchange(cryptomanagerDecryptUrl, HttpMethod.POST, httpEntity, String.class);

            CryptomanagerResponseDto responseObject = mapper.readValue(response.getBody(), CryptomanagerResponseDto.class);

            if (responseObject != null &&
                    responseObject.getErrors() != null && !responseObject.getErrors().isEmpty()) {
                LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                        "Packet decryption failed");
                ServiceError error = responseObject.getErrors().get(0);
                LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                        "Error message : " + error.getMessage());
                throw new PacketDecryptionFailureException(error.getMessage());
            }
            decryptedPacket = CryptoUtil.decodeBase64(responseObject.getResponse().getData());
            LOGGER.info(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                    "Successfully decrypted Packet");
        } catch (IOException e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                    ExceptionUtils.getStackTrace(e));
            throw new PacketDecryptionFailureException(IO_EXCEPTION, e);
        } catch (DateTimeParseException e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                    ExceptionUtils.getStackTrace(e));
            throw new PacketDecryptionFailureException(DATE_TIME_EXCEPTION);
        } catch (Exception e) {
            LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REFERENCEID, refId,
                    ExceptionUtils.getStackTrace(e));
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
        return decryptedPacket;
    }

    @Override
    public boolean verify(String refId, byte[] packet, byte[] signature) {
       try {
           String machineId = refId.split("_")[1];
    	   	String publicKey=getPublicKey(machineId);
            TpmSignVerifyRequestDto dto = new TpmSignVerifyRequestDto();
            dto.setData(CryptoUtil.encodeBase64(packet));
            dto.setSignature(CryptoUtil.encodeBase64(signature));
            dto.setPublicKey(publicKey);
            RequestWrapper<TpmSignVerifyRequestDto> request = new RequestWrapper<>();
            request.setRequest(dto);
            request.setMetadata(null);
            DateTimeFormatter format = DateTimeFormatter.ofPattern(DATETIME_PATTERN);
            LocalDateTime localdatetime = LocalDateTime
                    .parse(DateUtils.getUTCCurrentDateTimeString(DATETIME_PATTERN), format);
            request.setRequesttime(localdatetime);
            HttpEntity<RequestWrapper<TpmSignVerifyRequestDto>> httpEntity = new HttpEntity<>(request);
            ResponseEntity<String> response = restTemplate.exchange(keymanagerCsverifysignUrl, HttpMethod.POST, httpEntity,
                    String.class);
            LinkedHashMap responseMap = (LinkedHashMap) mapper.readValue(response.getBody(), LinkedHashMap.class).get("response");//.get("signature");
            if (responseMap != null && responseMap.size() > 0)
                return responseMap.get("verified") != null && responseMap.get("verified").toString().equalsIgnoreCase("true");
            else {
                LOGGER.error(PacketManagerLogger.SESSIONID, "SIGNATURE", new String(signature),
                        "Failed to verify signature");
                throw new SignatureException();
            }
        } catch (IOException e) {
           LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, null,
                   ExceptionUtils.getStackTrace(e));
            throw new SignatureException(e);
        } catch( RestClientException e) {
        	LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID, null,
                    ExceptionUtils.getStackTrace(e));
             throw new SignatureException(e);
        }
    }

	private String getPublicKey(String machineId) throws IOException {
		ResponseEntity<String> response = restTemplate.exchange(syncdataGetTpmKeyUrl+machineId, HttpMethod.GET, null,
                String.class);
		 LinkedHashMap responseMap = (LinkedHashMap) mapper.readValue(response.getBody(), LinkedHashMap.class).get("response");//.get("signature");
		 if (responseMap != null && responseMap.size() > 0)
             return (String) responseMap.get("signingPublicKey") ;
         else {
             LOGGER.error(PacketManagerLogger.SESSIONID, "PUBLIC_KEY", machineId,
                     "Failed to get public key");
             throw new SignatureException();
         }
	}
}
