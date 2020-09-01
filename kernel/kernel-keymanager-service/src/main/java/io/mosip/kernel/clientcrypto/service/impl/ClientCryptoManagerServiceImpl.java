package io.mosip.kernel.clientcrypto.service.impl;

import io.mosip.kernel.clientcrypto.constant.ClientCryptoErrorConstants;
import io.mosip.kernel.clientcrypto.constant.ClientCryptoManagerConstant;
import io.mosip.kernel.clientcrypto.dto.*;
import io.mosip.kernel.clientcrypto.exception.ClientCryptoException;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoManagerService;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Anusha Sunkada
 * @since 1.2.0
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
    public boolean csVerify(byte[] data, byte[] signature) {
        return clientCryptoFacade.getClientSecurity().validateSignature(signature, data);
    }

    @Override
    public byte[] csEncrypt(String refId, byte[] data) {
        //TODO
        return data;
    }

    @Override
    public byte[] csDecrypt(String refId, byte[] cipher) {
        //TODO
        return cipher;
    }

    @Override
    public TpmCryptoResponseDto csEncrypt(TpmCryptoRequestDto tpmCryptoRequestDto) {
        byte[] cipher = clientCryptoFacade.getClientSecurity().asymmetricEncrypt(
                tpmCryptoRequestDto.getValue().getBytes());

        TpmCryptoResponseDto tpmCryptoResponseDto = new TpmCryptoResponseDto();
        tpmCryptoResponseDto.setValue(CryptoUtil.encodeBase64(cipher));
        return tpmCryptoResponseDto;
    }

    @Override
    public TpmCryptoResponseDto csDecrypt(TpmCryptoRequestDto tpmCryptoRequestDto) {
        byte[] plainData = clientCryptoFacade.getClientSecurity().asymmetricDecrypt(tpmCryptoRequestDto.getValue().getBytes());
        TpmCryptoResponseDto tpmCryptoResponseDto = new TpmCryptoResponseDto();
        tpmCryptoResponseDto.setValue(CryptoUtil.encodeBase64(plainData));
        return tpmCryptoResponseDto;
    }

    @Override
    public PublicKeyResponseDto getSigningPublicKey(PublicKeyRequestDto publicKeyRequestDto) {
        if(ClientCryptoManagerConstant.SERVER_PROD_PROFILE.equalsIgnoreCase(publicKeyRequestDto.getServerProfile())
                && !clientCryptoFacade.getClientSecurity().isTPMInstance()) {
            LOGGER.error(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                    ClientCryptoManagerConstant.EMPTY, "TPM INITIALIZATION IS MANDATORY.");

            throw new ClientCryptoException(ClientCryptoErrorConstants.TPM_REQUIRED.getErrorCode(),
                    ClientCryptoErrorConstants.TPM_REQUIRED.getErrorMessage());
        }

        LOGGER.info(ClientCryptoManagerConstant.SESSIONID, ClientCryptoManagerConstant.INITIALIZATION,
                ClientCryptoManagerConstant.EMPTY, "CURRENT PROFILE : " + publicKeyRequestDto.getServerProfile() != null ?
                        publicKeyRequestDto.getServerProfile() : ClientCryptoManagerConstant.EMPTY);

        PublicKeyResponseDto publicKeyResponseDto = new PublicKeyResponseDto();
        publicKeyResponseDto.setPublicKey(CryptoUtil.encodeBase64(clientCryptoFacade.getClientSecurity().
                getSigningPublicPart()));
        return publicKeyResponseDto;
    }
}
