package io.mosip.kernel.clientcrypto.controller;


import io.mosip.kernel.clientcrypto.dto.*;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoManagerService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author Anusha Sunkada
 * @since 1.2.0
 */

@CrossOrigin
@RestController
@Api(value = "Operation related to offline Encryption and Decryption", tags = { "clientcrypto" })
public class ClientCryptoController {

    @Autowired
    private ClientCryptoManagerService clientCryptoManagerService;

    /**
     *
     * @param tpmSignRequestDtoRequestWrapper
     * @return
     */
    @PostMapping(value = "/cssign", produces = "application/json")
    public ResponseWrapper<TpmSignResponseDto> signData(@RequestBody @Valid RequestWrapper<TpmSignRequestDto>
                                                                             tpmSignRequestDtoRequestWrapper) {
        ResponseWrapper<TpmSignResponseDto> responseDtoResponseWrapper = new ResponseWrapper<>();
        responseDtoResponseWrapper.setResponse(clientCryptoManagerService.csSign(tpmSignRequestDtoRequestWrapper.getRequest()));
        return responseDtoResponseWrapper;
    }

    /**
     *
     * @param tpmSignVerifyRequestDtoRequestWrapper
     * @return
     */
    @PostMapping(value = "/csverifysign", produces = "application/json")
    public ResponseWrapper<TpmSignVerifyResponseDto> verifySignature(@RequestBody @Valid RequestWrapper<TpmSignVerifyRequestDto>
                                                                  tpmSignVerifyRequestDtoRequestWrapper) {
        ResponseWrapper<TpmSignVerifyResponseDto> responseDtoResponseWrapper = new ResponseWrapper<>();
        responseDtoResponseWrapper.setResponse(clientCryptoManagerService.csVerify(tpmSignVerifyRequestDtoRequestWrapper.getRequest()));
        return responseDtoResponseWrapper;
    }

    /**
     *
     * @param tpmCryptoRequestDtoRequestWrapper
     * @return
     */
    @PostMapping(value = "/tpmencrypt", produces = "application/json")
    public ResponseWrapper<TpmCryptoResponseDto> tpmEncrypt(@RequestBody @Valid RequestWrapper<TpmCryptoRequestDto>
                                                                             tpmCryptoRequestDtoRequestWrapper) {
        ResponseWrapper<TpmCryptoResponseDto> responseDtoResponseWrapper = new ResponseWrapper<>();
        responseDtoResponseWrapper.setResponse(clientCryptoManagerService.csEncrypt(tpmCryptoRequestDtoRequestWrapper.getRequest()));
        return responseDtoResponseWrapper;
    }

    /**
     *
     * @param tpmCryptoRequestDtoRequestWrapper
     * @return
     */
    @PostMapping(value = "/tpmdecrypt", produces = "application/json")
    public ResponseWrapper<TpmCryptoResponseDto> tpmDecrypt(@RequestBody @Valid RequestWrapper<TpmCryptoRequestDto>
                                                                    tpmCryptoRequestDtoRequestWrapper) {
        ResponseWrapper<TpmCryptoResponseDto> responseDtoResponseWrapper = new ResponseWrapper<>();
        responseDtoResponseWrapper.setResponse(clientCryptoManagerService.csDecrypt(tpmCryptoRequestDtoRequestWrapper.getRequest()));
        return responseDtoResponseWrapper;
    }

    /**
     *
     * @param publicKeyRequestDtoRequestWrapper
     * @return
     */
    @PostMapping(value = "/tpmpublickey", produces = "application/json")
    public ResponseWrapper<PublicKeyResponseDto> getSigningPublicKey(@RequestBody @Valid RequestWrapper<PublicKeyRequestDto>
                                                                    publicKeyRequestDtoRequestWrapper) {
        ResponseWrapper<PublicKeyResponseDto> responseDtoResponseWrapper = new ResponseWrapper<>();
        responseDtoResponseWrapper.setResponse(clientCryptoManagerService.getSigningPublicKey(publicKeyRequestDtoRequestWrapper.getRequest()));
        return responseDtoResponseWrapper;
    }
}
