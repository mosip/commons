package io.mosip.commons.packet.impl;

import io.mosip.commons.khazana.util.EncryptionUtil;
import io.mosip.commons.packet.constants.CryptomanagerConstant;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.commons.packet.spi.IPacketCryptoService;
import io.mosip.kernel.clientcrypto.dto.TpmSignRequestDto;
import io.mosip.kernel.clientcrypto.dto.TpmSignVerifyRequestDto;
import io.mosip.kernel.clientcrypto.dto.TpmSignVerifyResponseDto;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoManagerService;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.service.CryptomanagerService;
import io.mosip.kernel.cryptomanager.service.impl.CryptomanagerServiceImpl;
import io.mosip.kernel.signature.dto.TimestampRequestDto;
import io.mosip.kernel.signature.service.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Component
@Qualifier("OfflinePacketCryptoServiceImpl")
public class OfflinePacketCryptoServiceImpl implements IPacketCryptoService {

    public static final String APPLICATION_ID = "REGISTRATION";

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${mosip.utc-datetime-pattern:yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}")
    private String DATETIME_PATTERN;

    /**
     * The cryptomanager service.
     */
    private CryptomanagerService cryptomanagerService = null;

    /**
     * The key manager.
     */
    private SignatureService signatureService = null;

    /**
     * The key manager.
     */
    private ClientCryptoManagerService tpmCryptoService = null;

    @Value("${crypto.PrependThumbprint.enable:true}")
    private boolean isPrependThumbprintEnabled;

    @Override
    public byte[] sign(byte[] packet) {
        TpmSignRequestDto signRequest = new TpmSignRequestDto();
        signRequest.setData(CryptoUtil.encodeBase64(packet));
        return CryptoUtil.decodeBase64(getTpmCryptoService().csSign(signRequest).getData());
    }

    @Override
    public byte[] encrypt(String refId, byte[] packet) {
        String packetString = CryptoUtil.encodeBase64String(packet);
        CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
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

        byte[] encryptedData = CryptoUtil.decodeBase64(getCryptomanagerService().encrypt(cryptomanagerRequestDto).getData());
        return EncryptionUtil.mergeEncryptedData(encryptedData, nonce, aad);
    }

    @Override
    public byte[] decrypt(String refId, byte[] packet) {
        byte[] nonce = Arrays.copyOfRange(packet, 0, CryptomanagerConstant.GCM_NONCE_LENGTH);
        byte[] aad = Arrays.copyOfRange(packet, CryptomanagerConstant.GCM_NONCE_LENGTH,
                CryptomanagerConstant.GCM_NONCE_LENGTH + CryptomanagerConstant.GCM_AAD_LENGTH);
        byte[] encryptedData = Arrays.copyOfRange(packet, CryptomanagerConstant.GCM_NONCE_LENGTH + CryptomanagerConstant.GCM_AAD_LENGTH,
                packet.length);

        CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
        cryptomanagerRequestDto.setApplicationId(APPLICATION_ID);
        cryptomanagerRequestDto.setReferenceId(refId);
        cryptomanagerRequestDto.setAad(CryptoUtil.encodeBase64String(aad));
        cryptomanagerRequestDto.setSalt(CryptoUtil.encodeBase64String(nonce));
        cryptomanagerRequestDto.setData(CryptoUtil.encodeBase64String(encryptedData));
        cryptomanagerRequestDto.setPrependThumbprint(isPrependThumbprintEnabled);
        cryptomanagerRequestDto.setTimeStamp(DateUtils.getUTCCurrentDateTime());

        return CryptoUtil.decodeBase64(getCryptomanagerService().decrypt(cryptomanagerRequestDto).getData());
    }

    @Override
    public boolean verify(String machineId, byte[] packet, byte[] signature) {
        TpmSignVerifyRequestDto tpmSignVerifyRequestDto = new TpmSignVerifyRequestDto();
        tpmSignVerifyRequestDto.setData(CryptoUtil.encodeBase64(packet));
        tpmSignVerifyRequestDto.setSignature(CryptoUtil.encodeBase64(signature));
        //TODO - get public key based on machine Id
        //tpmSignVerifyRequestDto.setPublicKey(<>);
        TpmSignVerifyResponseDto tpmSignVerifyResponseDto = getTpmCryptoService().csVerify(tpmSignVerifyRequestDto);
        return tpmSignVerifyResponseDto.isVerified();
    }

    private CryptomanagerService getCryptomanagerService() {
        if (cryptomanagerService == null)
            cryptomanagerService = applicationContext.getBean(CryptomanagerServiceImpl.class);
        return cryptomanagerService;
    }

    private SignatureService getSignatureService() {
        if (signatureService == null)
            signatureService = applicationContext.getBean(SignatureService.class);
        return signatureService;
    }

    private ClientCryptoManagerService getTpmCryptoService() {
        if (tpmCryptoService == null)
            tpmCryptoService = applicationContext.getBean(ClientCryptoManagerService.class);
        return tpmCryptoService;
    }
}