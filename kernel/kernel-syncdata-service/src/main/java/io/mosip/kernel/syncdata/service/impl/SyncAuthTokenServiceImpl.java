package io.mosip.kernel.syncdata.service.impl;


import com.auth0.jwt.impl.JWTParser;
import com.auth0.jwt.interfaces.Header;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mosip.kernel.clientcrypto.exception.ClientCryptoException;
import io.mosip.kernel.clientcrypto.service.impl.ClientCryptoFacade;
import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.syncdata.constant.MasterDataErrorCode;
import io.mosip.kernel.syncdata.constant.SyncAuthErrorCode;
import io.mosip.kernel.syncdata.dto.AuthLoginUser;
import io.mosip.kernel.syncdata.dto.IdSchemaDto;
import io.mosip.kernel.syncdata.dto.MachineAuthDto;
import io.mosip.kernel.syncdata.dto.response.TokenResponseDto;
import io.mosip.kernel.syncdata.entity.Machine;
import io.mosip.kernel.syncdata.exception.RequestException;
import io.mosip.kernel.syncdata.repository.MachineRepository;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Objects;


/**
 * @since 1.1.3
 */
@RefreshScope
@Service
public class SyncAuthTokenServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(SyncAuthTokenServiceImpl.class);

    @Value("${mosip.syncdata.tpm.required}")
    private boolean isTPMRequired;

    @Value("${mosip.kernel.authtoken.internal.url}")
    private String authTokenInternalUrl;

    @Value("${mosip.kernel.authtoken.internal.appId}")
    private String authTokenInternalAppId;

    @Value("${auth.token.header}")
    private String authTokenHeaderName;

    @Value("${auth.refreshtoken.header}")
    private String authRefreshTokenHeaderName;

    @Autowired
    private ClientCryptoFacade clientCryptoFacade;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private RestTemplate restTemplate;

    private static ObjectMapper objectMapper = new ObjectMapper();

    static  {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public String getAuthToken(String requestData) {
        String[] parts = requestData.split("\\.");
        if(parts.length == 3) {
            byte[] header = Base64.getUrlDecoder().decode(parts[0]);
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            byte[] signature = Base64.getUrlDecoder().decode(parts[2]);

            Machine machine = validateRequestData(header, payload, signature);
            try {
                MachineAuthDto machineAuthDto = objectMapper.readValue(payload, MachineAuthDto.class);

                AuthLoginUser authLoginUser = new AuthLoginUser(machineAuthDto.getUserId(), machineAuthDto.getPassword(),
                        authTokenInternalAppId);
                RequestWrapper<AuthLoginUser> requestWrapper = new RequestWrapper();
                requestWrapper.setRequest(authLoginUser);
                HttpEntity<RequestWrapper<AuthLoginUser>> httpEntity = new HttpEntity<>(requestWrapper);

                UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(authTokenInternalUrl);
                ResponseEntity<String> responseEntity = restTemplate.postForEntity(builder.build().toUri(),
                        httpEntity, String.class);

                objectMapper.registerModule(new JavaTimeModule());
                ResponseWrapper<TokenResponseDto> responseWrapper = objectMapper.readValue(responseEntity.getBody(),
                        new TypeReference<ResponseWrapper<TokenResponseDto>>() {});
                responseWrapper.getResponse().setTimestamp(LocalDateTime.now(ZoneId.of("UTC")));
                String token = objectMapper.writeValueAsString(responseWrapper.getResponse());

                logger.info("token >>>> {}", token);

                byte[] cipher = clientCryptoFacade.encrypt(CryptoUtil.decodeBase64(machine.getPublicKey()),
                        token.getBytes(), this.isTPMRequired);

                return CryptoUtil.encodeBase64(cipher);

            } catch (Exception ex) {
                logger.error("Failed to parse auth request payload", ex);
            }
        }
        throw new RequestException(SyncAuthErrorCode.INVALID_REQUEST.getErrorCode(),
                SyncAuthErrorCode.INVALID_REQUEST.getErrorMessage());
    }

    private Machine validateRequestData(byte[] header, byte[] payload, byte[] signature) {
        JWTParser jwtParser = new JWTParser();
        Header jwtheader = jwtParser.parseHeader(new String(header));

        if(Objects.nonNull(jwtheader.getKeyId())) {
            List<Machine> machines = machineRepository.findBySignKeyIndexAndIsActive(jwtheader.getKeyId());

            if(Objects.isNull(machines) || machines.isEmpty())
                throw new RequestException(MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorCode(),
                        MasterDataErrorCode.MACHINE_NOT_FOUND.getErrorMessage());

            try {
                boolean verified = clientCryptoFacade.validateSignature(CryptoUtil.decodeBase64(machines.get(0).getSignPublicKey()),
                        signature, payload, isTPMRequired);
                logger.info("validateRequestData verified : {}", verified);
                if(verified) {  return machines.get(0); }

            } catch(ClientCryptoException ex) {
                logger.error("Failed to validate signature", ex);
            }
        }
        throw new RequestException(SyncAuthErrorCode.INVALID_REQUEST.getErrorCode(),
                SyncAuthErrorCode.INVALID_REQUEST.getErrorMessage());
    }
}
