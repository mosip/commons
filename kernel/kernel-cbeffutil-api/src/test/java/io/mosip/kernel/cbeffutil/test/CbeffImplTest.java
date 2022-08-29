package io.mosip.kernel.cbeffutil.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.mosip.kernel.biometrics.commons.CbeffValidator;
import io.mosip.kernel.biometrics.constant.*;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIRInfo;
import io.mosip.kernel.biometrics.entities.Entry;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.biometrics.entities.VersionType;
import io.mosip.kernel.biometrics.spi.CbeffUtil;
import io.mosip.kernel.cbeffutil.impl.CbeffImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.kernel.core.cbeffutil.common.CbeffISOReader;
import io.mosip.kernel.core.cbeffutil.entity.BIRVersion;


@RunWith(PowerMockRunner.class)
@PrepareForTest({URL.class, CbeffValidator.class})
public class CbeffImplTest {

	@InjectMocks
	private CbeffUtil cbeffUtilImpl = new CbeffImpl();

	@Mock
	private InputStream inputStream;

	@Mock
	private URL mockURL;

	private List<BIR> createList;
	private List<BIR> updateList;
	private List<BIR> exceptionList;
	private static final String localpath = "./src/main/resources";

	@Before
	public void setUp() throws Exception {
		PowerMockito.whenNew(URL.class).withArguments(Mockito.anyString()).thenReturn(mockURL);
		when(mockURL.openStream()).thenReturn(inputStream);


		byte[] rindexFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Index.iso",
				"Finger");
		byte[] rmiddleFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Middle.iso",
				"Finger");
		byte[] rringFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Ring.iso",
				"Finger");
		byte[] rlittleFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Little.iso",
				"Finger");
		byte[] rightthumb = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Thumb.iso",
				"Finger");
		byte[] lindexFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Index.iso",
				"Finger");
		byte[] lmiddleFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Middle.iso",
				"Finger");
		byte[] lringFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Ring.iso", "Finger");
		byte[] llittleFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Little.iso",
				"Finger");
		byte[] leftthumb = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Thumb.iso", "Finger");
		// byte[] irisImg1 = CbeffISOReader.readISOImage(localpath + "/images/" +
		// "IrisImageRight.iso", "Iris");
		// byte[] irisImg2 = CbeffISOReader.readISOImage(localpath + "/images/" +
		// "IrisImageLeft.iso", "Iris");
		// byte[] faceImg = CbeffISOReader.readISOImage(localpath + "/images/" +
		// "faceImage.iso", "Face");
		RegistryIDType format = new RegistryIDType();
		format.setOrganization("257");
		format.setType("7");
		QualityType Qtype = new QualityType();
		Qtype.setScore(new Long(100));
		RegistryIDType algorithm = new RegistryIDType();
		algorithm.setOrganization("HMAC");
		algorithm.setType("SHA-256");
		Qtype.setAlgorithm(algorithm);
		createList = new ArrayList<>();
		BIR rIndexFinger = new BIR.BIRBuilder().withBdb(rindexFinger)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right IndexFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rIndexFinger);

		BIR rMiddleFinger = new BIR.BIRBuilder().withBdb(rmiddleFinger)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right MiddleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rMiddleFinger);

		BIR rRingFinger = new BIR.BIRBuilder().withBdb(rringFinger)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right RingFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rRingFinger);

		BIR rLittleFinger = new BIR.BIRBuilder().withBdb(rlittleFinger)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right LittleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rLittleFinger);

		BIR lIndexFinger = new BIR.BIRBuilder().withBdb(lindexFinger)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left IndexFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(lIndexFinger);

		BIR lMiddleFinger = new BIR.BIRBuilder().withBdb(lmiddleFinger)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left MiddleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(lMiddleFinger);

		BIR lRightFinger = new BIR.BIRBuilder().withBdb(lringFinger)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left RingFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(lRightFinger);

		BIR lLittleFinger = new BIR.BIRBuilder().withBdb(llittleFinger)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left LittleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(lLittleFinger);

		BIR rightThumb = new BIR.BIRBuilder().withBdb(rightthumb)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right Thumb"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rightThumb);

		BIR leftThumb = new BIR.BIRBuilder().withBdb(leftthumb)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left Thumb"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(leftThumb);

//		BIR face = new BIR.BIRBuilder().withBdb(faceImg)
//				.withVersion(new VersionType(1, 1))
//				.withCbeffversion(new VersionType(1, 1))
//				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
//				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format)
//						.withQuality(Qtype).withType(Arrays.asList(BiometricType.FACE)).withPurpose(PurposeType.ENROLL)
//						.withLevel(ProcessedLevelType.RAW).withCreationDate(LocalDateTime.now(ZoneId.of("UTC")))
//						.build())
//				.build();
//		
//		createList.add(face);
//
//		BIR leftIris = new BIR.BIRBuilder().withBdb(irisImg1)
//				.withVersion(new VersionType(1, 1))
//				.withCbeffversion(new VersionType(1, 1))
//				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
//				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format)
//						.withQuality(Qtype).withType(Arrays.asList(BiometricType.IRIS)).withSubtype(Arrays.asList("Right"))
//						.withPurpose(PurposeType.ENROLL).withCreationDate(LocalDateTime.now(ZoneId.of("UTC")))
//						.withLevel(ProcessedLevelType.RAW).build())
//				.build();
//		
//		createList.add(leftIris);
//
//		BIR rightIris = new BIR.BIRBuilder().withBdb(irisImg2)
//				.withVersion(new VersionType(1, 1))
//				.withCbeffversion(new VersionType(1, 1))
//				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
//				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format)
//						.withQuality(Qtype).withType(Arrays.asList(BiometricType.IRIS)).withSubtype(Arrays.asList("Left"))
//						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
//						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
//				.build();
//		
//		createList.add(rightIris);

		exceptionList = new ArrayList<>();
		BIR validThumb = new BIR.BIRBuilder().withBdb(leftthumb)
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left Thumb"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();
		Entry entry=new Entry(OtherKey.EXCEPTION, "true");
		BIR exceptionThumb = new BIR.BIRBuilder().withBdb(new byte[0])
				.withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left Thumb"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.withOthers(OtherKey.EXCEPTION, "true")
				.build();
		exceptionList.add(validThumb);
		exceptionList.add(exceptionThumb);
	}

	// @Test
	public void testCreateXML() throws Exception {
		byte[] createXml = cbeffUtilImpl.createXML(createList);
		createXMLFile(createXml, "createCbeffLatest");
		assertEquals(new String(createXml), new String(readCreatedXML("createCbeffLatest")));

	}

	@Test
	public void testCreateXMLFromLocal() throws Exception {
		PowerMockito.mockStatic(CbeffValidator.class);
		when(CbeffValidator.createXMLBytes(any(), any())).thenReturn(readCreatedXML("createCbeffLatest2"));
		byte[] createXml = cbeffUtilImpl.createXML(createList, readXSD("updatedcbeff"));
		createXMLFile(createXml, "createCbeffLatest2");
		assertEquals(new String(createXml), new String(readCreatedXML("createCbeffLatest2")));

	}

	@Test
	public void testCreateExceptionXMLFromLocal() throws Exception {
		PowerMockito.mockStatic(CbeffValidator.class);
		cbeffUtilImpl.createXML(exceptionList, readXSD("cbeff"));
	}

	private byte[] readCreatedXML(String name) throws IOException {
		byte[] fileContent = Files.readAllBytes(Paths.get(localpath + "/schema/" + name + ".xml"));
		return fileContent;
	}

	private byte[] readXSD(String name) throws IOException {
		byte[] fileContent = Files.readAllBytes(Paths.get(localpath + "/schema/" + name + ".xsd"));
		return fileContent;
	}

	private static void createXMLFile(byte[] updatedXmlBytes, String name) throws Exception {
		File tempFile = new File(localpath + "/schema/" + name + ".xml");
		FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(updatedXmlBytes);
		fos.close();
	}

	// @Test
	public void testUpdateXML() throws Exception {
		byte[] updateXml = cbeffUtilImpl.updateXML(updateList, readCreatedXML("createCbeff"));
		createXMLFile(updateXml, "updateCbeff");
		assertEquals(new String(updateXml), new String(readCreatedXML("updateCbeff")));
	}

	private byte[] readbytesFromStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		// this is storage overwritten on each iteration with bytes
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		// we need to know how may bytes were read to write them to the byteBuffer
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		// and then we can return your byte array.
		return byteBuffer.toByteArray();

	}


}