package io.mosip.kernel.clientcrypto.service.impl;

import io.mosip.kernel.clientcrypto.dto.*;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoManagerService;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Anusha Sunkada
 * @since 1.1.2
 */
@Service
public class ClientCryptoManagerServiceImpl implements ClientCryptoManagerService {

    private static final Logger LOGGER = KeymanagerLogger.getLogger(ClientCryptoManagerServiceImpl.class);

    @Autowired
    private ClientCryptoFacade clientCryptoFacade;

    @Override
    public TpmSignResponseDto csSign(TpmSignRequestDto tpmSignRequestDto) {
        byte[] signedData = clientCryptoFacade.getClientSecurity().signData(
                CryptoUtil.decodeBase64(tpmSignRequestDto.getData()));
        TpmSignResponseDto tpmSignResponseDto = new TpmSignResponseDto();
        tpmSignResponseDto.setData(CryptoUtil.encodeBase64(signedData));
        return tpmSignResponseDto;
    }

    @Override
    public TpmSignVerifyResponseDto csVerify(TpmSignVerifyRequestDto tpmSignVerifyRequestDto) {
        boolean result = clientCryptoFacade.validateSignature(
                CryptoUtil.decodeBase64(tpmSignVerifyRequestDto.getPublicKey()),
                CryptoUtil.decodeBase64(tpmSignVerifyRequestDto.getSignature()),
                CryptoUtil.decodeBase64(tpmSignVerifyRequestDto.getData()));
        TpmSignVerifyResponseDto tpmSignVerifyResponseDto = new TpmSignVerifyResponseDto();
        tpmSignVerifyResponseDto.setVerified(result);
        return tpmSignVerifyResponseDto;
    }

    @Override
    public TpmCryptoResponseDto csEncrypt(TpmCryptoRequestDto tpmCryptoRequestDto) {
        byte[] cipher = clientCryptoFacade.encrypt(
                CryptoUtil.decodeBase64(tpmCryptoRequestDto.getPublicKey()),
                CryptoUtil.decodeBase64(tpmCryptoRequestDto.getValue()));
        TpmCryptoResponseDto tpmCryptoResponseDto = new TpmCryptoResponseDto();
        tpmCryptoResponseDto.setValue(CryptoUtil.encodeBase64(cipher));
        return tpmCryptoResponseDto;
    }

    @Override
    public TpmCryptoResponseDto csDecrypt(TpmCryptoRequestDto tpmCryptoRequestDto) {
        byte[] plainData = clientCryptoFacade.decrypt(CryptoUtil.decodeBase64(tpmCryptoRequestDto.getValue()));
        TpmCryptoResponseDto tpmCryptoResponseDto = new TpmCryptoResponseDto();
        tpmCryptoResponseDto.setValue(CryptoUtil.encodeBase64(plainData));
        return tpmCryptoResponseDto;
    }

    @Override
    public PublicKeyResponseDto getSigningPublicKey(PublicKeyRequestDto publicKeyRequestDto) {
        PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
        publicKeyResponseDto.setPublicKey(CryptoUtil.encodeBase64(clientCryptoFacade.getClientSecurity().
                getSigningPublicPart()));
        return publicKeyResponseDto;
    }

    @Override
    public PublicKeyResponseDto getEncPublicKey(PublicKeyRequestDto publicKeyRequestDto) {
        PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
        publicKeyResponseDto.setPublicKey(CryptoUtil.encodeBase64(clientCryptoFacade.getClientSecurity().
                getEncryptionPublicPart()));
        return publicKeyResponseDto;
    }
}
