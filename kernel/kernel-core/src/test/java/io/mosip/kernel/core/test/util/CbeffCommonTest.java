package io.mosip.kernel.core.test.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import io.mosip.kernel.core.cbeffutil.common.Base64Adapter;
import io.mosip.kernel.core.cbeffutil.common.CbeffISOReader;
import io.mosip.kernel.core.cbeffutil.common.CbeffXSDValidator;
import io.mosip.kernel.core.cbeffutil.common.DateAdapter;

public class CbeffCommonTest {
	

	private String localpath = "./src/test/resources";

	
	private Base64Adapter base64Adapter;
	
	private DateAdapter dateAdapter;
	
	@Before
	public void init() throws Exception {
		base64Adapter = new Base64Adapter();
		dateAdapter = new DateAdapter();
	}

	@Test
	public void testBase64AdapterUnmarshal() throws Exception {
		assertThat(new String(base64Adapter.unmarshal(base64Adapter.marshal("testdata".getBytes()))), is("testdata"));
	}
	
	@Test
	public void testReadISOImage() throws Exception {
		assertThat(CbeffISOReader.readISOImage("src/test/resources/ISOImage.iso", null), isA(byte[].class));
	}
	
	@Test
	public void testCbeffXSDValidator() throws Exception {
		assertThat(CbeffXSDValidator.validateXML(readXSD("updatedcbeff"), readCreatedXML("createCbeffLatest2")), is(true));
	}
	
	@Test
	public void testDateAdapter() throws Exception {
		LocalDateTime dateTime = LocalDateTime.now();
		assertThat(dateAdapter.unmarshal(dateAdapter.marshal(dateTime)), is(dateTime));
	}
	
	private byte[] readXSD(String name) throws IOException {
		byte[] fileContent = Files.readAllBytes(Paths.get(localpath + "/schema/" + name + ".xsd"));
		return fileContent;
	}
	
	private byte[] readCreatedXML(String name) throws IOException {
		byte[] fileContent = Files.readAllBytes(Paths.get(localpath + "/schema/" + name + ".xml"));
		return fileContent;
	}

}
