package io.mosip.commons.packet.impl;

import io.mosip.commons.packet.spi.IPacketCryptoService;
import io.mosip.kernel.core.crypto.spi.CryptoCoreSpec;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
@Qualifier("OfflinePacketCryptoServiceImpl")
public class OfflinePacketCryptoServiceImpl implements IPacketCryptoService {

    @Value("${mosip.kernel.data-key-splitter:KEY_SPLITTER}")
    private String KEY_SPLITTER;

    @Autowired
    private KeyGenerator keyGenerator;

    @Autowired
    private CryptoCoreSpec<byte[], byte[], SecretKey, PublicKey, PrivateKey, String> cryptoCore;

    @Override
    public byte[] sign(byte[] packet) {
        // TODO : To be implemented
        return new byte[0];
    }

    @Override
    public byte[] encrypt(String id, byte[] packet) {
        return packet;
    }

    @Override
    public byte[] decrypt(String id, byte[] packet) {
        // TODO : To be implemented
        return packet;
    }

    @Override
    public boolean verify(byte[] packet) {
        // TODO : To be implemented
        return true;
    }
}
