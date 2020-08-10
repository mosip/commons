package io.mosip.common.packet.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.packet.config.PacketManagerConfig;
import io.mosip.commons.packet.facade.PacketReader;
import io.mosip.commons.packet.impl.PacketReaderImpl;
import io.mosip.commons.packet.spi.IPacketReader;
import io.mosip.commons.packet.util.PacketHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest(value = { PacketHelper.class, URL.class })
@Import(PacketManagerConfig.class)
@TestPropertySource({ "classpath:application.properties" })
public class PacketReaderTest {

    @InjectMocks
    private PacketReader packetReader = new PacketReader();

    @Mock
    private InputStream inputStream;

    @Mock
    private URL mockURL;

    @MockBean
    private PacketReaderImpl reader;

    private static final String ID = "id";
    private static final String SOURCE = "source";
    private static final String PROCESS = "process";

    @Before
    public void setup() throws Exception {
        PowerMockito.whenNew(URL.class).withArguments(Mockito.anyString()).thenReturn(mockURL);
        when(mockURL.openStream()).thenReturn(inputStream);
        PowerMockito.mockStatic(PacketHelper.class);
        PowerMockito.when(PacketHelper.class, "isSourceAndProcessPresent", anyString(), anyString(), anyString(), Mockito.any()).thenReturn(true);
    }

    @Test
    @Ignore
    public void getFieldTest() {
        Map<String, Object> allMap = new HashMap<>();
        when(reader.getAll(anyString(), anyString())).thenReturn(allMap);
        packetReader.getField(ID, "field", SOURCE, PROCESS, false);


    }
}
