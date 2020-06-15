package test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.validation.constraints.AssertTrue;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;

import io.mosip.kernel.core.exception.FileNotFoundException;
import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import io.mosip.kernel.packetmanager.exception.PacketDecryptionFailureException;
import io.mosip.kernel.packetmanager.util.IdSchemaUtils;
import io.mosip.kernel.packetmanager.util.ZipUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({IdSchemaUtils.class})
public class ZipUtilsTest {
	
	
	private InputStream inputStream;
	
	 @Before
	 public void  setup() throws IOException, PacketDecryptionFailureException, ApiNotAccessibleException {
		 
		 	File initialFile = new File("src/test/resources/unziptest.zip");
		 	inputStream = new FileInputStream(initialFile);
	  }
	 
	 @Test
	 public void testUnzipAndGetFile() throws IOException {
		 
		 InputStream input=ZipUtils.unzipAndGetFile(inputStream, "unziptest/File11");
		 assertNotNull(input);
	}
	 @Test
	 public void testUnzipAndGetFileForNUll() throws IOException {
		 
		 InputStream input=ZipUtils.unzipAndGetFile(inputStream, "test");
		 assertNull(input);
	}
	 @Test
	 public void testUnzipAndCheckIsFileExist() throws IOException {

		 boolean flag=ZipUtils.unzipAndCheckIsFileExist(inputStream, "unziptest/File11");	
		 assertTrue(flag);
	}
	
	 @Test
	 public void testUnZipFromInputStream() throws IOException {
		 ZipUtils.unZipFromInputStream(inputStream, "src/test/resources/");
	}
	 @Test
	 public void testUnZipFromInputStream1() throws IOException {
		 
		 //File initialFile = new File("src/test/resources/84071493960000320190110145452.zip");
		 //inputStream = new FileInputStream(initialFile);
		// when(FileUtils.getFile(Mockito.any())).thenReturn("src/test/resources/");
		 ZipUtils.unZipFromInputStream(inputStream, anyString());
	}
	
	

}
