package io.mosip.commons.packet.impl;

import io.mosip.commons.packet.spi.IPacketCryptoService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("OfflinePacketCryptoServiceImpl")
public class OfflinePacketCryptoServiceImpl implements IPacketCryptoService {

    /*@Autowired
    ClientCryptoManagerService clientCryptoManagerService;*/

    @Override
    public byte[] sign(byte[] packet) {
        //return clientCryptoManagerService.csSign(packet);
        return new byte[0];
    }

    @Override
    public byte[] encrypt(String id, byte[] packet) {
        //return clientCryptoManagerService.csEncrypt(packet);
        return packet;
    }

    @Override
    public byte[] decrypt(String id, byte[] packet) {
        //return clientCryptoManagerService.csDecrypt(packet);
        return packet;
    }

    @Override
    public boolean verify(byte[] packet, byte[] signature) {
        //return clientCryptoManagerService.csVerify(packet, signature);
        return true;
    }
}
