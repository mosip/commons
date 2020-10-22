package io.mosip.commons.packet.test.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.packet.dto.packet.CryptomanagerResponseDto;
import io.mosip.commons.packet.dto.packet.DecryptResponseDto;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import io.mosip.commons.packet.exception.PacketDecryptionFailureException;
import io.mosip.commons.packet.exception.SignatureException;
import io.mosip.commons.packet.impl.OnlinePacketCryptoServiceImpl;
import io.mosip.commons.packet.util.ZipUtils;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ZipUtils.class, IOUtils.class, JsonUtils.class})
@PropertySource("classpath:application-test.properties")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
public class OnlinePacketCryptoServiceTest {

    private static final String ID = "10001100770000320200720092256";

    @InjectMocks
    private OnlinePacketCryptoServiceImpl onlinePacketCryptoService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper mapper;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(onlinePacketCryptoService, "DATETIME_PATTERN", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        ReflectionTestUtils.setField(onlinePacketCryptoService, "APPLICATION_VERSION", "v1");
        ReflectionTestUtils.setField(onlinePacketCryptoService, "centerIdLength", 5);
        ReflectionTestUtils.setField(onlinePacketCryptoService, "cryptomanagerDecryptUrl", "http://localhost");
        ReflectionTestUtils.setField(onlinePacketCryptoService, "machineIdLength", 5);
        ReflectionTestUtils.setField(onlinePacketCryptoService, "cryptomanagerEncryptUrl", "http://localhost");
        ReflectionTestUtils.setField(onlinePacketCryptoService, "DATETIME_PATTERN", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    }

    @Test
    public void signTest() throws IOException {
        String expected = "signature";
        byte[] packet = "packet".getBytes();
        LinkedHashMap submap = new LinkedHashMap();
        submap.put("signature", expected);
        LinkedHashMap responseMap = new LinkedHashMap();
        responseMap.put("response", submap);
        ReflectionTestUtils.setField(onlinePacketCryptoService, "keymanagerSignUrl", "localhost");
        ResponseEntity<String> response = new ResponseEntity<>("hello", HttpStatus.OK);

        Mockito.when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(response);
        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(responseMap);

        byte[] result = onlinePacketCryptoService.sign(packet);
        assertTrue(Arrays.equals(expected.getBytes(), result));
    }

    @Test(expected = SignatureException.class)
    public void signExceptionTest() throws IOException {
        String expected = "signature";
        byte[] packet = "packet".getBytes();
        LinkedHashMap submap = new LinkedHashMap();
        submap.put("signature", expected);
        LinkedHashMap responseMap = new LinkedHashMap();
        responseMap.put("response", submap);
        ReflectionTestUtils.setField(onlinePacketCryptoService, "keymanagerSignUrl", "localhost");
        ResponseEntity<String> response = new ResponseEntity<>("hello", HttpStatus.OK);

        Mockito.when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(response);
        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenThrow(new IOException("exception"));

        byte[] result = onlinePacketCryptoService.sign(packet);
    }

    @Test
    public void encryptTest() throws IOException {
        byte[] packet = "10001100770000320200720092256_packetwithsignatureandaad".getBytes();
        CryptomanagerResponseDto cryptomanagerResponseDto = new CryptomanagerResponseDto();
        cryptomanagerResponseDto.setErrors(null);
        DecryptResponseDto decryptResponseDto = new DecryptResponseDto("packet");
        cryptomanagerResponseDto.setResponse(decryptResponseDto);


        ReflectionTestUtils.setField(onlinePacketCryptoService, "cryptomanagerEncryptUrl", "localhost");
        ResponseEntity<String> response = new ResponseEntity<>("hello", HttpStatus.OK);

        Mockito.when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(response);
        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(cryptomanagerResponseDto);

        byte[] result = onlinePacketCryptoService.encrypt(ID, packet);
        assertNotNull(result);
    }

    @Test(expected = PacketDecryptionFailureException.class)
    public void encryptExceptionTest() throws IOException {
        String expected = "signature";
        byte[] packet = "packet".getBytes();

        ReflectionTestUtils.setField(onlinePacketCryptoService, "cryptomanagerEncryptUrl", "localhost");

        Mockito.when(restTemplate.exchange(anyString(), any(HttpMethod.class),
                any(HttpEntity.class), any(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        onlinePacketCryptoService.encrypt(ID, packet);
    }

    @Test
    public void decryptTest() throws IOException {
        byte[] packet = "10001100770000320200720092256_packetwithsignatureandaad".getBytes();
        CryptomanagerResponseDto cryptomanagerResponseDto = new CryptomanagerResponseDto();
        cryptomanagerResponseDto.setErrors(null);
        DecryptResponseDto decryptResponseDto = new DecryptResponseDto(CryptoUtil.encodeBase64("packet".getBytes()));
        cryptomanagerResponseDto.setResponse(decryptResponseDto);


        ReflectionTestUtils.setField(onlinePacketCryptoService, "cryptomanagerDecryptUrl", "localhost");
        ResponseEntity<String> response = new ResponseEntity<>("hello", HttpStatus.OK);

        Mockito.when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(response);
        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(cryptomanagerResponseDto);

        byte[] result = onlinePacketCryptoService.decrypt(ID, packet);
        assertNotNull(result);
    }

    @Test(expected = PacketDecryptionFailureException.class)
    public void decryptExceptionTest() throws IOException {
        String expected = "signature";
        byte[] packet = "packet".getBytes();

        ReflectionTestUtils.setField(onlinePacketCryptoService, "cryptomanagerDecryptUrl", "localhost");

        Mockito.when(restTemplate.exchange(anyString(), any(HttpMethod.class),
                any(HttpEntity.class), any(Class.class))).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        onlinePacketCryptoService.decrypt(ID, packet);
    }

    @Test
    public void verifyTest() throws IOException {
        String expected = "signature";
        byte[] packet = "packet".getBytes();
        LinkedHashMap submap = new LinkedHashMap();
        submap.put("status", "success");
        LinkedHashMap responseMap = new LinkedHashMap();
        responseMap.put("response", submap);
        ReflectionTestUtils.setField(onlinePacketCryptoService, "keymanagerValidateUrl", "localhost");
        ResponseEntity<String> response = new ResponseEntity<>("hello", HttpStatus.OK);

        Mockito.when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(response);
        Mockito.when(mapper.readValue(anyString(), any(Class.class))).thenReturn(responseMap);

        boolean result = onlinePacketCryptoService.verify(packet, expected.getBytes());
        assertTrue(result);
    }
}
