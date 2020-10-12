package io.mosip.commons.khazana.util;

import io.mosip.commons.khazana.constant.KhazanaConstant;

public class EncryptionUtil {

    public static byte[] mergeEncryptedData(byte[] encryptedData, byte[] nonce, byte[] aad) {
        byte[] finalEncData = new byte[encryptedData.length + KhazanaConstant.GCM_AAD_LENGTH + KhazanaConstant.GCM_NONCE_LENGTH];
        System.arraycopy(nonce, 0, finalEncData, 0, nonce.length);
        System.arraycopy(aad, 0, finalEncData, nonce.length, aad.length);
        System.arraycopy(encryptedData, 0, finalEncData, nonce.length + aad.length,	encryptedData.length);
        return finalEncData;
    }
}
