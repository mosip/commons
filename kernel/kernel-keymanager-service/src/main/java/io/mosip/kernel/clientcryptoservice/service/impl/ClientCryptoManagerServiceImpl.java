package io.mosip.kernel.clientcryptoservice.service.impl;

import io.mosip.kernel.clientcryptoservice.service.spi.ClientCryptoManagerService;

/**
 * @since 1.2.0
 */
public class ClientCryptoManagerServiceImpl implements ClientCryptoManagerService {

    @Override
    public byte[] csSign(byte[] data) {
        return new byte[0];
    }

    @Override
    public boolean csVerify(byte[] data, byte[] signature) {
        return true;
    }

    @Override
    public byte[] csEncrypt(String refId, byte[] data) {
        return data;
    }

    @Override
    public byte[] csDecrypt(String refId, byte[] cipher) {
        return cipher;
    }

    @Override
    public byte[] csEncrypt(byte[] data) {
        return data;
    }

    @Override
    public byte[] csDecrypt(byte[] cipher) {
        return cipher;
    }
}
