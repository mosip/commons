package io.mosip.commons.packet.controller;

import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.TestBootApplication;
import io.mosip.commons.packet.dto.FieldDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.util.JsonUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class PacketManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PacketReader packetReader;

    private RequestWrapper<Object> request = new RequestWrapper<>();

    FieldDto fieldDto = new FieldDto();

    @Before
    public void setup() {
        fieldDto.setField("name");
        fieldDto.setBypassCache(false);
        fieldDto.setId("id");
        fieldDto.setProcess("NEW");
        fieldDto.setSource("REGISTRATION");
    }


    @Test
    @WithUserDetails("reg-processor")
    @Ignore
    public void testpdfSuccess() throws Exception {
        String value = "value";

        Mockito.when(
                packetReader.getField(anyString(), anyString(), anyString(), anyString(), anyBoolean())).thenReturn(value);

        request.setRequest(fieldDto);

        this.mockMvc.perform(post("/searchField").contentType(MediaType.APPLICATION_JSON).content(JsonUtils.javaObjectToJsonString(request)))
                .andExpect(status().isOk());
    }
}
