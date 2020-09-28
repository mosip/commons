package io.mosip.commons.packet.test.impl;

import io.mosip.commons.packet.impl.OfflinePacketCryptoServiceImpl;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.core.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.PropertySource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZipUtils.class, IOUtils.class, JsonUtils.class})
@PropertySource("classpath:application-test.properties")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class OfflinePacketCryptoServiceTest {

    @InjectMocks
    private OfflinePacketCryptoServiceImpl offlinePacketCryptoService;

    @Test
    public void signTest() {
        byte[] packet = new byte[0];

        byte[] result = offlinePacketCryptoService.sign(packet);
        assertTrue(ArrayUtils.isEquals(packet, result));
    }

    @Test
    public void encryptTest() {
        String id = "1234";
        byte[] packet = "packet".getBytes();

        byte[] result = offlinePacketCryptoService.encrypt(id, packet);
        assertEquals(packet, result);
    }

    @Test
    public void decryptTest() {
        String id = "1234";
        byte[] packet = "packet".getBytes();

        byte[] result = offlinePacketCryptoService.decrypt(id, packet);
        assertEquals(packet, result);
    }

    @Test
    public void verifyTest() {
        byte[] signature = "1234".getBytes();
        byte[] packet = "packet".getBytes();

        boolean result = offlinePacketCryptoService.verify(packet, signature);
        assertTrue(result);
    }
}
