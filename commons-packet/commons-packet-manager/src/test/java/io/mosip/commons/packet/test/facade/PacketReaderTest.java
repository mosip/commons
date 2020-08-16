package io.mosip.commons.packet.test.facade;

import io.mosip.commons.packet.test.TestBootApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestBootApplication.class)
@AutoConfigureMockMvc
public class PacketReaderTest {

    @Test
    @WithUserDetails("reg-processor")
    public void testGetField() {

    }
}
