package io.mosip.commons.packet.controller;

import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.facade.PacketWriter;
import io.mosip.commons.packetmanager.PacketServiceApp;
import io.mosip.commons.packetmanager.dto.FieldDto;
import io.mosip.kernel.auth.adapter.config.SecurityConfig;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.JsonUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PacketServiceApp.class)
@AutoConfigureMockMvc
public class PacketManagerControllerTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PacketReader packetReader;

    @MockBean
    private CbeffImpl cbeff;

    @MockBean
    private PacketWriter packetWriter;

    private RequestWrapper<Object> request = new RequestWrapper<>();

    FieldDto fieldDto = new FieldDto();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        fieldDto.setField("name");
        fieldDto.setBypassCache(false);
        fieldDto.setId("id");
        fieldDto.setProcess("NEW");
        fieldDto.setSource("REGISTRATION");
    }


    @Test
    @WithUserDetails("reg-processor")
    @Ignore
    public void testSuccess() throws Exception {
        String value = "value";

        Mockito.when(
                packetReader.getField(anyString(), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(value);

        request.setRequest(fieldDto);

        this.mockMvc.perform(post("/searchField").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }
}
