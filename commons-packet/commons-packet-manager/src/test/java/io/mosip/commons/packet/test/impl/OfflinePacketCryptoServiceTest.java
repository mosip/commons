package io.mosip.commons.packet.test.impl;

import io.mosip.commons.packet.constants.CryptomanagerConstant;
import io.mosip.commons.packet.impl.OfflinePacketCryptoServiceImpl;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.clientcrypto.dto.TpmSignResponseDto;
import io.mosip.kernel.clientcrypto.dto.TpmSignVerifyResponseDto;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoManagerService;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerResponseDto;
import io.mosip.kernel.cryptomanager.service.impl.CryptomanagerServiceImpl;
import io.mosip.kernel.signature.dto.ValidatorResponseDto;
import io.mosip.kernel.signature.service.SignatureService;
import io.mosip.kernel.signature.service.impl.SignatureServiceImpl;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZipUtils.class, IOUtils.class, JsonUtils.class})
@PropertySource("classpath:application-test.properties")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class OfflinePacketCryptoServiceTest {

    @InjectMocks
    private OfflinePacketCryptoServiceImpl offlinePacketCryptoService;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private CryptomanagerServiceImpl cryptomanagerService;

    @Mock
    private ClientCryptoManagerService clientCryptoManagerService;

    @Mock
    private SignatureServiceImpl signatureService;

    @Before
    public void setup() {
        Mockito.when(applicationContext.getBean(CryptomanagerServiceImpl.class)).thenReturn(cryptomanagerService);
        Mockito.when(applicationContext.getBean(ClientCryptoManagerService.class)).thenReturn(clientCryptoManagerService);
        Mockito.when(applicationContext.getBean(SignatureService.class)).thenReturn(signatureService);
        ReflectionTestUtils.setField(offlinePacketCryptoService, "DATETIME_PATTERN", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    }

    @Test
    public void signTest() {
        String packetSignature = "signature";
        TpmSignResponseDto signatureResponse = new TpmSignResponseDto();
        signatureResponse.setData(CryptoUtil.encodeBase64(packetSignature.getBytes(StandardCharsets.UTF_8)));

        Mockito.when(clientCryptoManagerService.csSign(any())).thenReturn(signatureResponse);

        byte[] result = offlinePacketCryptoService.sign(packetSignature.getBytes());
        assertTrue(ArrayUtils.isEquals(packetSignature.getBytes(), result));
    }

    @Test
    public void encryptTest() {
        String id = "10001100770000320200720092256";
        String response = "packet";
        byte[] packet = "packet".getBytes();
        CryptomanagerResponseDto cryptomanagerResponseDto = new CryptomanagerResponseDto();
        cryptomanagerResponseDto.setData(response);
        Mockito.when(cryptomanagerService.encrypt(any())).thenReturn(cryptomanagerResponseDto);

        byte[] result = offlinePacketCryptoService.encrypt(id, packet);
        assertNotNull(result);
    }

    @Test
    public void decryptTest() {
        String id = "10001100770000320200720092256";
        String response = "10001100770000320200720092256_packetwithsignatureandaad";
        byte[] packet = "10001100770000320200720092256_packetwithsignatureandaad".getBytes();
        CryptomanagerResponseDto cryptomanagerResponseDto = new CryptomanagerResponseDto();
        cryptomanagerResponseDto.setData(response);
        Mockito.when(cryptomanagerService.decrypt(any())).thenReturn(cryptomanagerResponseDto);

        byte[] result = offlinePacketCryptoService.decrypt(id, packet);
        assertNotNull(result);
    }

    @Test
    public void verifyTest() {
        String packetSignature = "signature";

        TpmSignVerifyResponseDto tpmSignVerifyResponseDto = new TpmSignVerifyResponseDto();
        tpmSignVerifyResponseDto.setVerified(true);
        Mockito.when(clientCryptoManagerService.csVerify(any())).thenReturn(tpmSignVerifyResponseDto);

        boolean result = offlinePacketCryptoService.verify("12345","packet".getBytes(), packetSignature.getBytes());
        assertTrue(result);
    }
}
