package test;

import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.packetmanager.constants.PacketManagerConstants;
import io.mosip.kernel.packetmanager.dto.CryptomanagerResponseDto;
import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.exception.FileNotFoundInDestinationException;
import io.mosip.kernel.packetmanager.exception.PacketDecryptionFailureException;
import io.mosip.kernel.packetmanager.impl.PacketReaderServiceImpl;
import io.mosip.kernel.packetmanager.spi.PacketDecryptor;
import io.mosip.kernel.packetmanager.spi.PacketReaderService;
import io.mosip.kernel.packetmanager.util.IdSchemaUtils;
import io.mosip.kernel.packetmanager.util.ZipUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ZipUtils.class, IdSchemaUtils.class})
public class PacketReaderServiceTest {

    @Mock
    private FileSystemAdapter fileSystemAdapter;

    @Mock
    private InputStream inputStream;

    @Mock
    private PacketDecryptor decryptor;
    
    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    PacketReaderService service = new PacketReaderServiceImpl();
    
	private String str="{\"id\":\"84071493960000320190110145452\",\"version\":\"1.0\",\"metadata\":null,\"data\":\"asdf\",\"response\":{\"id\":\"IDSchemaVersion\",\"description\":\"ID Schema Version\",\"labelName\":\"IDSchemaVersion\",\"type\":\"number\",\"data\":\"asdf\"},\"responseTime\":\"2020-05-02T02:50:12.208Z\",\"errors\":null}";


    @Before
    public void  setup() throws IOException, PacketDecryptionFailureException, ApiNotAccessibleException {
        PowerMockito.mockStatic(ZipUtils.class);
        PowerMockito.when(ZipUtils.unzipAndGetFile(any(), anyString()))
                .thenReturn(new ByteArrayInputStream(new String("abc").getBytes()));

        when(decryptor.decrypt(any(), anyString())).thenReturn(new ByteArrayInputStream(new String("abc").getBytes()));
    }


    @Test
    public void testFileExistence() throws PacketDecryptionFailureException, ApiNotAccessibleException, IOException {

        when(fileSystemAdapter.getPacket(anyString())).thenReturn(inputStream);
        PowerMockito.when(ZipUtils.unzipAndCheckIsFileExist(any(), anyString())).thenReturn(Boolean.TRUE);

        boolean result = service.checkFileExistence("test", PacketManagerConstants.IDENTITY_FILENAME_WITH_EXT, "id");

        assertTrue(result);

    }
    
    @Test
    public void testGetFile() throws PacketDecryptionFailureException, ApiNotAccessibleException, IOException, io.mosip.kernel.core.exception.IOException {

        when(fileSystemAdapter.getPacket(anyString())).thenReturn(inputStream);
        PowerMockito.when(ZipUtils.unzipAndCheckIsFileExist(any(), anyString())).thenReturn(Boolean.TRUE);

       InputStream result= service.getFile("testfile", PacketManagerConstants.IDENTITY_FILENAME_WITH_EXT, "id");
       assertNotNull(result);
    
    }
    
    @Test(expected =NullPointerException.class)
    public void testGetIdSchemaVersionFromPacket() throws PacketDecryptionFailureException, ApiNotAccessibleException, IOException, io.mosip.kernel.core.exception.IOException {

        when(fileSystemAdapter.getPacket(anyString())).thenReturn(inputStream);
        PowerMockito.when(ZipUtils.unzipAndCheckIsFileExist(any(), anyString())).thenReturn(Boolean.TRUE);

        Double result=service.getIdSchemaVersionFromPacket(anyString()).doubleValue();   
        assertNull(result);

    }

    @Test
    public void testGetCompleteIdObject() throws PacketDecryptionFailureException, ApiNotAccessibleException, IOException, io.mosip.kernel.core.exception.IOException {

    	String rid="84071493960000320190110145452";
    	PowerMockito.mockStatic(ZipUtils.class);
        when(fileSystemAdapter.getPacket(anyString())).thenReturn(inputStream);
        ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(new String("{\"identity\":{\"id\":\"individualBiometrics\",\"description\":\"\",\"labelName\":\"individualBiometrics\",\"type\":\"biometricsType\",\"minimum\":0,\"maximum\":0,\"controlType\":\"biometrics\",\"fieldType\":\"default\",\"format\":\"none\",\"fieldCategory\":\"pvt\"}}").getBytes());
        ByteArrayInputStream byteArrayInputStream1=new ByteArrayInputStream(new String("{\"identity\":{\"id\":\"individualBiometrics\",\"description\":\"\",\"labelName\":\"individualBiometrics\",\"type\":\"biometricsType\",\"minimum\":0,\"maximum\":0,\"controlType\":\"biometrics\",\"fieldType\":\"default\",\"format\":\"none\",\"fieldCategory\":\"pvt\"}}").getBytes());
    	PowerMockito.when(ZipUtils.unzipAndGetFile(any(), anyString()))
           .thenReturn(byteArrayInputStream, byteArrayInputStream1);  	
        PowerMockito.when(ZipUtils.unzipAndCheckIsFileExist(any(), anyString())).thenReturn(Boolean.TRUE);
        Object result=service.getCompleteIdObject("84071493960000320190110145452","test1,test2");
        assertNotNull(result);

    }

 

    @Test
    public void testGetCompleteIdObjectWithSinglePacket() throws PacketDecryptionFailureException, ApiNotAccessibleException, IOException, io.mosip.kernel.core.exception.IOException, JSONException {
    	
    	JSONObject jsonObject = new JSONObject();
    	jsonObject.put("aDataSort", true);
    	jsonObject.put("aTargets", true);
    
        when(fileSystemAdapter.getPacket(anyString())).thenReturn(inputStream);
    	PowerMockito.when(ZipUtils.unzipAndGetFile(any(), anyString()))
           .thenReturn(new ByteArrayInputStream(new String("{\"id\":true,\"version\":\"1.0\",\"data\":\"val123\"}").getBytes()));  
	
        PowerMockito.when(ZipUtils.unzipAndCheckIsFileExist(any(), anyString())).thenReturn(Boolean.TRUE);
        service.getCompleteIdObject("84071493960000320190110145452","test1"); 

    }
    

    @Test(expected = FileNotFoundInDestinationException.class)
    public void testFileNotFoundExceptionForUnzipAndGetFile() throws PacketDecryptionFailureException, ApiNotAccessibleException, IOException {

    	
        when(fileSystemAdapter.getPacket(anyString())).thenReturn(inputStream);

        when(ZipUtils.unzipAndGetFile(any(), anyString()))
        .thenReturn(null);
        boolean result = service.checkFileExistence("test", PacketManagerConstants.IDENTITY_FILENAME_WITH_EXT, "id");

    }
    @Test(expected = PacketDecryptionFailureException.class)
    public void testPacketDecryptionFailureException() throws PacketDecryptionFailureException, ApiNotAccessibleException, IOException {

    	
        when(fileSystemAdapter.getPacket(anyString())).thenReturn(inputStream);

        PowerMockito.when(ZipUtils.unzipAndCheckIsFileExist(any(), anyString())).thenReturn(Boolean.TRUE);
        when(decryptor.decrypt(any(), anyString())).thenReturn(null);
       
        boolean result = service.checkFileExistence("test", PacketManagerConstants.IDENTITY_FILENAME_WITH_EXT, "id");

    }

    @Test(expected = FileNotFoundInDestinationException.class)
    public void testFileNotFoundException() throws PacketDecryptionFailureException, ApiNotAccessibleException, IOException {

    	
        when(fileSystemAdapter.getPacket(anyString())).thenReturn(null);

        boolean result = service.checkFileExistence("test", PacketManagerConstants.IDENTITY_FILENAME_WITH_EXT, "id");

    }
    @Test
    public void testGetEncryptedSourcePacket() throws IOException{
    	InputStream inputStream=null;
    	PowerMockito.when(ZipUtils.unzipAndGetFile(any(), anyString()))
        .thenReturn(new ByteArrayInputStream(new String("{\"id\":true,\"version\":\"1.0\",\"data\":\"val123\"}").getBytes())); 
        InputStream inputStream1 = service.getEncryptedSourcePacket("id", inputStream, "");
        assertNotNull(inputStream1);

    }
    
}


