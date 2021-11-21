package io.mosip.kernel.biometrics.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import io.mosip.kernel.biometrics.commons.BiometricsSignatureHelper;
import io.mosip.kernel.biometrics.commons.CbeffValidator;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIRInfo;
import io.mosip.kernel.biometrics.entities.Entry;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.biometrics.entities.SingleAnySubtypeType;
import io.mosip.kernel.biometrics.entities.VersionType;
import io.mosip.kernel.biometrics.spi.CbeffUtil;
import io.mosip.kernel.core.cbeffutil.common.CbeffISOReader;
import io.mosip.kernel.core.cbeffutil.exception.CbeffException;
import io.mosip.kernel.core.exception.ArithmeticException;
import io.mosip.kernel.core.exception.BiometricSignatureValidationException;
import io.mosip.kernel.core.exception.IllegalArgumentException;
import io.mosip.kernel.core.exception.NullPointerException;
import io.mosip.kernel.core.util.CalendarUtils;

public class CbeffValidatorTest {

	private List<BIR> createList;
	private List<BIR> updateList;
	private List<BIR> exceptionList;
	private String localpath = "./src/test/resources";
	byte[] rindexFinger = null;
	byte[] rmiddleFinger = null;
	byte[] rringFinger = null;
	byte[] rlittleFinger = null;
	byte[] rightthumb = null;
	byte[] lindexFinger = null;
	byte[] lmiddleFinger = null;
	byte[] lringFinger = null;
	byte[] llittleFinger = null;
	byte[] leftthumb = null;

	@Before
	public void setUp() throws Exception {

		rindexFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Index.iso", "Finger");
		rmiddleFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Middle.iso",
				"Finger");
		rringFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Ring.iso",
				"Finger");
		rlittleFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Little.iso",
				"Finger");
		rightthumb = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintRight_Thumb.iso",
				"Finger");
		lindexFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Index.iso",
				"Finger");
		lmiddleFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Middle.iso",
				"Finger");
		lringFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Ring.iso", "Finger");
		llittleFinger = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Little.iso",
				"Finger");
		leftthumb = CbeffISOReader.readISOImage(localpath + "/images/" + "FingerPrintLeft_Thumb.iso", "Finger");
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
		BIR rIndexFinger = new BIR.BIRBuilder().withBdb(rindexFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right IndexFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rIndexFinger);

		BIR rMiddleFinger = new BIR.BIRBuilder().withBdb(rmiddleFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("MiddleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rMiddleFinger);

		BIR rRingFinger = new BIR.BIRBuilder().withBdb(rringFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right RingFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rRingFinger);

		BIR rLittleFinger = new BIR.BIRBuilder().withBdb(rlittleFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right LittleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rLittleFinger);

		BIR lIndexFinger = new BIR.BIRBuilder().withBdb(lindexFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left IndexFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(lIndexFinger);

		BIR lMiddleFinger = new BIR.BIRBuilder().withBdb(lmiddleFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left MiddleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(lMiddleFinger);

		BIR lRightFinger = new BIR.BIRBuilder().withBdb(lringFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left RingFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(lRightFinger);

		BIR lLittleFinger = new BIR.BIRBuilder().withBdb(llittleFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left LittleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(lLittleFinger);

		BIR rightThumb = new BIR.BIRBuilder().withBdb(rightthumb).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right Thumb"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(rightThumb);

		BIR leftThumb = new BIR.BIRBuilder().withBdb(leftthumb).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left Thumb"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		createList.add(leftThumb);
	}

	@Test
	public void validateXMLTest() throws CbeffException {
		BIR bir = new BIR();
		bir.setBirs(createList);
		assertThat(CbeffValidator.validateXML(bir), is(true));
	}

	@Test(expected = CbeffException.class)
	public void validateXMLBIRNullTest() throws CbeffException {
		CbeffValidator.validateXML(null);
	}

	@Test(expected = CbeffException.class)
	public void validateXMLBDBNullTest() throws CbeffException {
		List<BIR> birs = new ArrayList<>(); 
		birs.add(new BIR.BIRBuilder().build());
		BIR bir = new BIR();
		bir.setBirs(birs);
		CbeffValidator.validateXML(bir);
	}

	@Test(expected = CbeffException.class)
	public void validateXMLBDBInfoNullTest() throws CbeffException {
		List<BIR> birs = new ArrayList<>(); 
		BIR nullBDBInfoBIR=new BIR.BIRBuilder().withBdb(lindexFinger).build();
		birs.add(nullBDBInfoBIR);
		BIR bir = new BIR();
		bir.setBirs(birs);
		CbeffValidator.validateXML(bir);
	}
	
	@Test(expected = CbeffException.class)
	public void validateXMLBDBInfoBiometricTypeNULLTest() throws CbeffException {
		List<BIR> birs = new ArrayList<>(); 
		BIR nullBDBInfoBIR=new BIR.BIRBuilder().withBdb(lindexFinger).withBdbInfo(new BDBInfo.BDBInfoBuilder().withType(null)
	            .withSubtype(Arrays.asList("Right MiddleFinger"))
				.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
				.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build()).build();
		birs.add(nullBDBInfoBIR);
		BIR bir = new BIR();
		bir.setBirs(birs);
		CbeffValidator.validateXML(bir);
	}
	
	@Test(expected = CbeffException.class)
	public void validateXMLBDBInfoBiometricTypeEmptyTest() throws CbeffException {
		List<BiometricType> bdbTypes = new ArrayList<>(); 
		List<BIR> birs = new ArrayList<>(); 
		BIR emptyBDBInfoBIR=new BIR.BIRBuilder().withBdb(lindexFinger).withBdbInfo(new BDBInfo.BDBInfoBuilder().withType(bdbTypes)
	            .withSubtype(Arrays.asList("Right MiddleFinger"))
				.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
				.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build()).build();
		birs.add(emptyBDBInfoBIR);
		BIR bir = new BIR();
		bir.setBirs(birs);
		CbeffValidator.validateXML(bir);
	}

	@Test(expected = CbeffException.class)
	public void validateXMLBDBInfoInvalidFormatTest() throws CbeffException {
		RegistryIDType format = new RegistryIDType();
		format.setOrganization("257");
		format.setType("1");
		List<BIR> birs = new ArrayList<>(); 
		BIR invalidFormatBDBInfoBIR=new BIR.BIRBuilder().withBdb(rindexFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right IndexFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();
		birs.add(invalidFormatBDBInfoBIR);
		BIR bir = new BIR();
		bir.setBirs(birs);
		CbeffValidator.validateXML(bir);
	}
	@Test
	public void createXMLBytesTest() throws IOException, Exception {
		BIR bir = new BIR();
		VersionType type = new VersionType(1, 1);
		BIRInfo birInfo = new BIRInfo();
		birInfo.setIntegrity(false);
		bir.setBirInfo(birInfo);
		bir.setCbeffversion(type);
		bir.setBirs(createList);
		assertThat(CbeffValidator.createXMLBytes(bir, readXSD("updatedcbeff")), isA(byte[].class));
	}
	
	@Test(expected = CbeffException.class)
	public void createXMLSAXExceptionBytesTest() throws IOException, Exception {
		BIR bir = new BIR();
		VersionType type = new VersionType(1, 1);
		BIRInfo birInfo = new BIRInfo();
		birInfo.setIntegrity(false);
		bir.setBirInfo(birInfo);
		bir.setCbeffversion(type);
		bir.setBirs(createList);
		assertThat(CbeffValidator.createXMLBytes(bir, readXSD("cbeff")), isA(byte[].class));
	}
	@Test
	public void getBIRFromXMLTest() throws IOException, Exception {
		BIR bir = CbeffValidator.getBIRFromXML(readCreatedXML("createCbeffLatest"));
		assertThat(bir.getVersion().getMajor(), is(1));
		assertThat(bir.getVersion().getMinor(), is(1));
	}

	@Test
	public void getBDBBasedOnTypeAndSubTypeSubTypeNULLTest() throws IOException, Exception {
		BIR bir = new BIR();
		bir.setBirs(createList);
		Map<String, String> bdbMap = CbeffValidator.getBDBBasedOnTypeAndSubType(bir, "Finger", null);
		assertThat(bdbMap.size(), is(10));
	}
	
	@Test
	public void getBDBBasedOnTypeAndSubTypeAllNULLTest() throws IOException, Exception {
		BIR bir = new BIR();
		bir.setBirs(createList);
		Map<String, String> bdbMap = CbeffValidator.getBDBBasedOnTypeAndSubType(bir, null, null);
		assertThat(bdbMap.size(), is(10));
	}
	
	@Test
	public void getBDBBasedOnTypeAndSubTypeTypeNULLTest() throws IOException, Exception {
		BIR bir = new BIR();
		bir.setBirs(createList);
		Map<String, String> bdbMap = CbeffValidator.getBDBBasedOnTypeAndSubType(bir, null, "MiddleFinger");
		assertThat(bdbMap.size(), is(1));
	}
	
	@Test
	public void getBDBBasedOnTypeAndSubTypeTest() throws IOException, Exception {
		BIR bir = new BIR();
		bir.setBirs(createList);
		Map<String, String> bdbMap = CbeffValidator.getBDBBasedOnTypeAndSubType(bir, "Finger", "MiddleFinger");
		assertThat(bdbMap.size(), is(1));
	}

	@Test
	public void isInEnumTest() throws IOException, Exception {
		assertThat(CbeffValidator.isInEnum("LEFT", SingleAnySubtypeType.class), is(true));
	}

	@Test
	public void getAllBDBDataSubTypeNULLTest() throws IOException, Exception {
		BIR bir = new BIR();
		bir.setBirs(createList);
		Map<String, String> bdbMap = CbeffValidator.getAllBDBData(bir, "Finger", null);
		assertThat(bdbMap.size(), is(10));
	}
	
	@Test
	public void getAllBDBDataTypeNullTest() throws IOException, Exception {
		BIR bir = new BIR();
		bir.setBirs(createList);
		Map<String, String> bdbMap = CbeffValidator.getAllBDBData(bir, null, "MiddleFinger");
		assertThat(bdbMap.size(), is(1));
	}
	
	@Test
	public void getAllBDBDataTest() throws IOException, Exception {
		BIR bir = new BIR();
		bir.setBirs(createList);
		Map<String, String> bdbMap = CbeffValidator.getAllBDBData(bir, "Finger", "MiddleFinger");
		assertThat(bdbMap.size(), is(1));
	}

	@Test
	public void getBIRDataFromXMLTypeTest() throws IOException, Exception {
		BIR bir = new BIR();
		bir.setBirs(createList);
		List<BIR> birs = CbeffValidator.getBIRDataFromXMLType(readCreatedXML("createCbeffLatest"), "Finger");
		assertThat(birs.size(), is(3));
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