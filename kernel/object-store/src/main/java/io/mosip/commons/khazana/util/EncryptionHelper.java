package io.mosip.commons.khazana.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionHelper {

    private static final String CRYPTO = "OfflinePacketCryptoServiceImpl";

    @Value("${objectstore.crypto.name:OfflinePacketCryptoServiceImpl}")
    private String cryptoName;

    @Autowired
    private OfflineEncryptionUtil offlineEncryptionUtil;

    @Autowired
    private OnlineCryptoUtil onlineCryptoUtil;


    public byte[] encrypt(String id, byte[] packet) {
        if (cryptoName.equalsIgnoreCase(CRYPTO))
            return offlineEncryptionUtil.encrypt(id, packet);
        else
            return onlineCryptoUtil.encrypt(id, packet);
    }

}
