package io.mosip.kernel.clientcrypto.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mosip.kernel.clientcrypto.dto.PublicKeyRequestDto;
import io.mosip.kernel.clientcrypto.service.impl.ClientCryptoFacade;
import io.mosip.kernel.clientcrypto.service.spi.ClientCryptoService;
import io.mosip.kernel.clientcrypto.test.ClientCryptoTestBootApplication;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.CryptoUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = { ClientCryptoTestBootApplication.class })
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class ClientCryptoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper mapper;

    @Autowired
    ClientCryptoFacade clientCryptoFacade;

    private static final String ID = "mosip.crypto.service";
    private static final String VERSION = "V1.0";

    private static final String public_key = "AAEACwACAHIAIINxl2dEhLP4GpDMjUal1yT9UtduBlILZPKh2hszFGmqABAAFwALCAAAAQABAQDiSa/AdVmDrj+ypFywexe/eSaSsrIoO5Ns0jp7niMu4hiFIwsFT7yWx2aQUQcdX5OjyXjv/XJctGxFcphLXke5fwAoW6BsbeM//1Mlhq9YvdMKlwMjhKcd+7MHHAXPUKGVmMjIJe6kWwUWh7FaZyu5hDymM5MJyYZRxz5fRos/N9ykiBxjWKZK06ZpIYI6Tj9rUNZ6HAdbJH2RmBHuO0knpbXdB+lnnVhvArAt3KWoyH3YzodHeOLJRe/Y8a+p8zRZb5h1tqlcLgshpNAqb+WJgyq2xDb0RJwzuyjjHPmJrDqlBMXHestz+ADRwXQL44iVb84LcuMbQTQ1hGcawtBj";
    private static final String dataToEncrypt = "HeloolHelloHelloHello";

    @Before
    public void init() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }


    @Test
    @Ignore
    public void getEncryptDecryptWithTpm() throws Exception {
        byte[] cipher = clientCryptoFacade.encrypt(CryptoUtil.decodeBase64(public_key), dataToEncrypt.getBytes());

        ClientCryptoService clientCryptoService = clientCryptoFacade.getClientSecurity();
        Assert.assertNotNull(clientCryptoService);

        byte[] plain = clientCryptoFacade.decrypt(cipher);
        Assert.assertNotNull(plain);
        Assert.assertArrayEquals(dataToEncrypt.getBytes(), plain);
    }

    @Test
    @Ignore
    public void getSignVerifyWithTPM() throws Exception {
        ClientCryptoService clientCryptoService = clientCryptoFacade.getClientSecurity();
        Assert.assertNotNull(clientCryptoService);

        byte[] localPubKey = clientCryptoService.getSigningPublicPart();

        byte[] sigBytes = clientCryptoFacade.getClientSecurity().signData(dataToEncrypt.getBytes());
        Assert.assertNotNull(sigBytes);

        boolean valid = clientCryptoFacade.validateSignature(localPubKey, sigBytes, dataToEncrypt.getBytes());
        Assert.assertTrue(valid);
    }

    @Test
    @Ignore
    public void getEncryptDecryptWithLocal() throws Exception {
        ClientCryptoService clientCryptoService = clientCryptoFacade.getClientSecurity();
        Assert.assertNotNull(clientCryptoService);

        byte[] localPubKey = clientCryptoService.getEncryptionPublicPart();

        byte[] cipher = clientCryptoFacade.encrypt(localPubKey, dataToEncrypt.getBytes());

        byte[] plain = clientCryptoFacade.decrypt(cipher);
        Assert.assertNotNull(plain);
        Assert.assertArrayEquals(dataToEncrypt.getBytes(), plain);
    }

    @Test
    @Ignore
    public void getSignVerifyWithLocal() throws Exception {
        ClientCryptoService clientCryptoService = clientCryptoFacade.getClientSecurity();
        Assert.assertNotNull(clientCryptoService);

        byte[] localPubKey = clientCryptoService.getSigningPublicPart();

        byte[] sigBytes = clientCryptoFacade.getClientSecurity().signData(dataToEncrypt.getBytes());
        Assert.assertNotNull(sigBytes);

        boolean valid = clientCryptoFacade.validateSignature(localPubKey, sigBytes, dataToEncrypt.getBytes());
        Assert.assertTrue(valid);
    }
    
 }
