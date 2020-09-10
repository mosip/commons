package io.mosip.kernel.clientcrypto.test.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mosip.kernel.clientcrypto.dto.PublicKeyRequestDto;
import io.mosip.kernel.clientcrypto.test.ClientCryptoTestBootApplication;
import io.mosip.kernel.core.http.RequestWrapper;
import org.junit.Before;
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

    private static final String ID = "mosip.crypto.service";
    private static final String VERSION = "V1.0";

    @Before
    public void init() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @WithUserDetails("test")
    @Test
    public void getSigningPublicKeyTestWithTpm() throws Exception {

        RequestWrapper<PublicKeyRequestDto> wrapper = new RequestWrapper<>();
        PublicKeyRequestDto publicKeyRequestDto = new PublicKeyRequestDto();
        publicKeyRequestDto.setServerProfile("DEV");
        wrapper.setRequest(publicKeyRequestDto);

        //TODO
        /*MvcResult result = mockMvc.perform(post("/tpmpublickey").contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(wrapper)))
                .andExpect(status().is(200)).andReturn();*/
    }
    
 }
