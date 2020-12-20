package io.mosip.commons.khazana.util;

import io.mosip.commons.khazana.constant.KhazanaConstant;
import io.mosip.commons.khazana.constant.KhazanaErrorCodes;
import io.mosip.commons.khazana.exception.ObjectStoreAdapterException;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.service.CryptomanagerService;
import io.mosip.kernel.cryptomanager.service.impl.CryptomanagerServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class OfflineEncryptionUtil {
    public static final String APPLICATION_ID = "REGISTRATION";

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${mosip.utc-datetime-pattern:yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}")
    private String DATETIME_PATTERN;

    /** The cryptomanager service. */
    private CryptomanagerService cryptomanagerService = null;

    /** The sign applicationid. */
    @Value("${mosip.sign.applicationid:KERNEL}")
    private String signApplicationid;

    /** The sign refid. */
    @Value("${mosip.sign.refid:SIGN}")
    private String signRefid;

    @Value("${mosip.kernel.registrationcenterid.length:5}")
    private int centerIdLength;

    @Value("${mosip.kernel.machineid.length:5}")
    private int machineIdLength;

    @Value("${crypto.PrependThumbprint.enable:true}")
    private boolean isPrependThumbprintEnabled;

    public byte[] encrypt(String id, byte[] packet) {
        String centerId = id.substring(0, centerIdLength);
        String machineId = id.substring(centerIdLength, centerIdLength + machineIdLength);
        String refId = centerId + "_" + machineId;
        String packetString = CryptoUtil.encodeBase64String(packet);
        CryptomanagerRequestDto cryptomanagerRequestDto = new CryptomanagerRequestDto();
        cryptomanagerRequestDto.setApplicationId(APPLICATION_ID);
        cryptomanagerRequestDto.setData(packetString);
        cryptomanagerRequestDto.setPrependThumbprint(isPrependThumbprintEnabled);
        cryptomanagerRequestDto.setReferenceId(refId);

        SecureRandom sRandom = new SecureRandom();
        byte[] nonce = new byte[KhazanaConstant.GCM_NONCE_LENGTH];
        byte[] aad = new byte[KhazanaConstant.GCM_AAD_LENGTH];
        sRandom.nextBytes(nonce);
        sRandom.nextBytes(aad);
        cryptomanagerRequestDto.setAad(CryptoUtil.encodeBase64String(aad));
        cryptomanagerRequestDto.setSalt(CryptoUtil.encodeBase64String(nonce));
        // setLocal Date Time
        if (id.length() > 14) {
            String packetCreatedDateTime = id.substring(id.length() - 14);
            String formattedDate = packetCreatedDateTime.substring(0, 8) + "T"
                    + packetCreatedDateTime.substring(packetCreatedDateTime.length() - 6);

            cryptomanagerRequestDto.setTimeStamp(LocalDateTime.parse(formattedDate, DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")));
        } else {
            throw new ObjectStoreAdapterException(KhazanaErrorCodes.ENCRYPTION_FAILURE.getErrorCode(), KhazanaErrorCodes.ENCRYPTION_FAILURE.getErrorMessage());
        }
        byte[] encryptedData = CryptoUtil.decodeBase64(getCryptomanagerService().encrypt(cryptomanagerRequestDto).getData());
        return EncryptionUtil.mergeEncryptedData(encryptedData, nonce, aad);
    }

    private CryptomanagerService getCryptomanagerService() {
        if (cryptomanagerService == null)
            cryptomanagerService = applicationContext.getBean(CryptomanagerServiceImpl.class);
        return cryptomanagerService;
    }
}
