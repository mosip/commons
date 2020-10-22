package io.mosip.kernel.syncdata.service.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.syncdata.constant.MasterDataErrorCode;
import io.mosip.kernel.syncdata.dto.response.KeyPairGenerateResponseDto;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.exception.SyncInvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Component
public class KeymanagerHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentitySchemaHelper.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mosip.kernel.keymanager.cert.url}")
    private String certificateUrl;

    public KeyPairGenerateResponseDto getCertificate(String applicationId, Optional<String> referenceId) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(certificateUrl);
            builder.queryParam("applicationId", applicationId);
            if (referenceId.isPresent())
                builder.queryParam("referenceId", referenceId.get());
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(builder.build().toUri(), String.class);

            objectMapper.registerModule(new JavaTimeModule());
            ResponseWrapper<KeyPairGenerateResponseDto> resp = objectMapper.readValue(responseEntity.getBody(),
                    new TypeReference<ResponseWrapper<KeyPairGenerateResponseDto>>() {});

            if(resp.getErrors() != null && !resp.getErrors().isEmpty())
                throw new SyncInvalidArgumentException(resp.getErrors());

            return resp.getResponse();
        } catch (Exception e) {
            LOGGER.error("Failed to fetch latest schema", e);
            throw new SyncDataServiceException(MasterDataErrorCode.SCHEMA_FETCH_FAILED.getErrorCode(),
                    MasterDataErrorCode.SCHEMA_FETCH_FAILED.getErrorMessage() + " : " +
                            ExceptionUtils.buildMessage(e.getMessage(), e.getCause()));
        }
    }
}
