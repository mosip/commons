package io.mosip.commons.khazana.test.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.mosip.commons.khazana.impl.PosixAdapter;
import io.mosip.kernel.core.util.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({File.class, FileInputStream.class, ZipInputStream.class, ZipEntry.class,
        PosixAdapter.class, IOUtils.class, FileUtils.class})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.management.*"})
@PropertySource("classpath:application-test.properties")
public class PosixAdapterTest {

    private static final String account = "acc";
    private static final String container = "reg123";
    private static final String source = "source";
    private static final String process = "process";
    private static final String objectName = "id";
    private static final String ZIP = ".zip";
    private static final String JSON = ".json";
    private static final String SEPARATOR = "/";

    @InjectMocks
    private PosixAdapter posixAdapter = new PosixAdapter();

    @Mock
    private File file;

    @Mock
    private FileInputStream fileInputStream;

    @Mock
    private ZipInputStream zipInputStream;

    @Mock
    private ZipEntry zipEntry;

    @Mock
    private ZipOutputStream zipOutputStream;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JSONObject jsonObject;

    @Before
    public void setup() throws Exception {
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(file);
        when(file.exists()).thenReturn(true);

        PowerMockito.whenNew(FileInputStream.class).withAnyArguments().thenReturn(fileInputStream);

        PowerMockito.whenNew(ZipInputStream.class).withAnyArguments().thenReturn(zipInputStream);

        when(zipInputStream.getNextEntry()).thenReturn(zipEntry).thenReturn(null);
        when(zipEntry.getName()).thenReturn(source + SEPARATOR + process + SEPARATOR + objectName + ZIP);

        PowerMockito.mockStatic(IOUtils.class);
        PowerMockito.mockStatic(FileUtils.class);
        byte[] data = "123".getBytes();
        PowerMockito.when(IOUtils.class, "toByteArray", any()).thenReturn(data);
        PowerMockito.doNothing().when(FileUtils.class, "copyToFile", any(), any());
        PowerMockito.whenNew(ZipOutputStream.class).withAnyArguments().thenReturn(zipOutputStream);
        doNothing().when(zipOutputStream).putNextEntry(any());
        doNothing().when(zipOutputStream).write(any());
        when(objectMapper.writeValueAsString(any())).thenReturn("string");

    }

    @Test
    public void testGetObject() throws Exception {

        InputStream is = posixAdapter.getObject(account, container, source, process, objectName);
        assertNotNull("Get object should not be null", is);
    }

    @Test
    public void testExists() throws Exception {

        boolean result = posixAdapter.exists(account, container, source, process, objectName);
        assertTrue("Get object should not be present", result);
    }

    @Test
    public void testPutObject() throws Exception {

        boolean result = posixAdapter.putObject(account, container, source, process, objectName, fileInputStream);
        assertTrue("Put object should not be false", result);
    }

    @Test
    public void testAddObjectMetaData() throws Exception {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("obj1", new String("obj"));

        Map<String, Object> result = posixAdapter.addObjectMetaData(account, container, source, process, objectName, metadata);
        assertTrue("Put object should not be false", result.size() == 1);
    }

    @Test
    public void testAddObjectMetaData1() throws Exception {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("obj1", new String("obj"));
        when(zipEntry.getName()).thenReturn(objectName + JSON);

        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(jsonObject).thenReturn(metadata);

        Map<String, Object> result = posixAdapter.addObjectMetaData(account, container, source, process, objectName, "obj", "obj1");
        assertTrue("Put object should not be false", result.size() == 1);
    }

    @Test
    public void testGetMetaData() throws Exception {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("obj1", new String("obj"));
        when(zipEntry.getName()).thenReturn(objectName + JSON);

        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(jsonObject).thenReturn(metadata);

        Map<String, Object> result = posixAdapter.getMetaData(account, container, source, process, objectName);
        assertTrue("Put object should not be false", result.size() == 1);
    }

    @Test
    public void testException() throws Exception {
        PowerMockito.when(FileUtils.class, "copyToFile", any(), any()).thenThrow(new io.mosip.kernel.core.exception.IOException("", "exception occured"));

        boolean result = posixAdapter.putObject(account, container, source, process, objectName, fileInputStream);
        assertFalse("Put object should be false", result);
    }

    @Test
    public void testFileNotFoundInDestinationException() throws Exception {
        when(file.exists()).thenReturn(false);
        InputStream result = posixAdapter.getObject(account, container, source, process, objectName);
        assertNull("Put object should be null", result);
    }

}
