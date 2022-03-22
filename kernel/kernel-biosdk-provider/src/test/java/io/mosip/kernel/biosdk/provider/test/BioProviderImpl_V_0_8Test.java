package io.mosip.kernel.biosdk.provider.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.ProcessedLevelType;
import io.mosip.kernel.biometrics.constant.PurposeType;
import io.mosip.kernel.biometrics.constant.QualityType;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.BIRInfo;
import io.mosip.kernel.biometrics.entities.RegistryIDType;
import io.mosip.kernel.biometrics.entities.VersionType;
import io.mosip.kernel.biometrics.model.SDKInfo;
import io.mosip.kernel.biosdk.provider.impl.BioProviderImpl_V_0_8;
import io.mosip.kernel.biosdk.provider.spi.iBioProviderApi;
import io.mosip.kernel.biosdk.provider.util.ProviderConstants;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.cbeffutil.common.CbeffISOReader;
import io.mosip.kernel.core.cbeffutil.exception.CbeffException;


public class BioProviderImpl_V_0_8Test  {

	private List<BIR> record;
	private List<BIR> updateList;
	private List<BIR> sample;
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
		record = new ArrayList<>();
		sample = new ArrayList<>();
		BIR rIndexFinger = new BIR.BIRBuilder().withBdb(rindexFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right IndexFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(rIndexFinger);

		BIR rMiddleFinger = new BIR.BIRBuilder().withBdb(rmiddleFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("MiddleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(rMiddleFinger);

		BIR rRingFinger = new BIR.BIRBuilder().withBdb(rringFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right RingFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(rRingFinger);

		BIR rLittleFinger = new BIR.BIRBuilder().withBdb(rlittleFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right LittleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(rLittleFinger);

		BIR lIndexFinger = new BIR.BIRBuilder().withBdb(lindexFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left IndexFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(lIndexFinger);

		BIR lMiddleFinger = new BIR.BIRBuilder().withBdb(lmiddleFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left MiddleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(lMiddleFinger);

		BIR lRightFinger = new BIR.BIRBuilder().withBdb(lringFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left RingFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(lRightFinger);

		BIR lLittleFinger = new BIR.BIRBuilder().withBdb(llittleFinger).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left LittleFinger"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(lLittleFinger);

		BIR rightThumb = new BIR.BIRBuilder().withBdb(rightthumb).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Right Thumb"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(rightThumb);

		BIR leftThumb = new BIR.BIRBuilder().withBdb(leftthumb).withVersion(new VersionType(1, 1))
				.withCbeffversion(new VersionType(1, 1))
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format).withQuality(Qtype)
						.withType(Arrays.asList(BiometricType.FINGER)).withSubtype(Arrays.asList("Left Thumb"))
						.withPurpose(PurposeType.ENROLL).withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC"))).build())
				.build();

		record.add(leftThumb);
		sample.addAll(record);
		
		 SDKInfo sdkInfo = new SDKInfo("0.8", "1", "MOCKVendor2", "test2");
	        sdkInfo.withSupportedMethod(BiometricFunction.MATCH, BiometricType.IRIS);
	        sdkInfo.withSupportedMethod(BiometricFunction.EXTRACT, BiometricType.IRIS);
	        sdkInfo.withSupportedMethod(BiometricFunction.QUALITY_CHECK, BiometricType.IRIS);

	        sdkInfo.withSupportedMethod(BiometricFunction.MATCH, BiometricType.FACE);
	        sdkInfo.withSupportedMethod(BiometricFunction.EXTRACT, BiometricType.FACE);
	        sdkInfo.withSupportedMethod(BiometricFunction.QUALITY_CHECK, BiometricType.FACE);
	}
	
	@Test
	public void initTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceOne0_8");
		input.put(BiometricType.FINGER, modalityParams);
		Map<BiometricType, List<BiometricFunction>> map=bioProviderImpl_V_0_8.init(input);
		assertNotNull(map.get(BiometricType.FINGER));
		List<BiometricFunction> biometricFunctions = map.get(BiometricType.FINGER);
		assertTrue(biometricFunctions.contains(BiometricFunction.MATCH));
		assertTrue(biometricFunctions.contains(BiometricFunction.EXTRACT));
		assertTrue(biometricFunctions.contains(BiometricFunction.QUALITY_CHECK));		
	}
	
	@Test(expected = BiometricException.class)
	public void initBiometricExceptionTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.SDKInstanceException");
		input.put(BiometricType.FINGER, modalityParams);
		Map<BiometricType, List<BiometricFunction>> map=bioProviderImpl_V_0_8.init(input);
		assertNotNull(map.get(BiometricType.FINGER));
		List<BiometricFunction> biometricFunctions = map.get(BiometricType.FINGER);
		assertTrue(biometricFunctions.contains(BiometricFunction.MATCH));
		assertTrue(biometricFunctions.contains(BiometricFunction.EXTRACT));
		assertTrue(biometricFunctions.contains(BiometricFunction.QUALITY_CHECK));		
	}

	
	@Test
	public void verifyTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceOne0_8");
		input.put(BiometricType.FINGER, modalityParams);
		bioProviderImpl_V_0_8.init(input);
		assertTrue(bioProviderImpl_V_0_8.verify(sample, record, BiometricType.FINGER, modalityParams));
	}
	
	@Test
	public void verifyIdentifyTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceOne0_8");
		input.put(BiometricType.FINGER, modalityParams);
		bioProviderImpl_V_0_8.init(input);
		Map<String, List<BIR>> gallery= new HashMap<String, List<BIR>>();
		gallery.put("check", record);
		Map<String, Boolean>result= bioProviderImpl_V_0_8.identify(sample, gallery, BiometricType.FINGER, modalityParams);
        assertTrue(result.get("check"));
	}
	
	@Test
	public void verifyFalseTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceTwo0_8");
		input.put(BiometricType.FINGER, modalityParams);
		bioProviderImpl_V_0_8.init(input);
		assertFalse(bioProviderImpl_V_0_8.verify(sample, record, BiometricType.FINGER, modalityParams));
	}
	
	@Test
	public void getSegmentQualityTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceOne0_8");
		input.put(BiometricType.FINGER, modalityParams);
		bioProviderImpl_V_0_8.init(input);
		Map<String, List<BIR>> gallery= new HashMap<String, List<BIR>>();
		BIR[] smp = new BIR[sample.size()];
		smp=sample.toArray(smp);
		gallery.put("check", record);
		float[] result= bioProviderImpl_V_0_8.getSegmentQuality(smp,modalityParams);
        assertThat(result[0],is(90.0F));
	}
	
	@Test
	public void getModalityQualityTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceOne0_8");
		input.put(BiometricType.FINGER, modalityParams);
		bioProviderImpl_V_0_8.init(input);
		Map<String, List<BIR>> gallery= new HashMap<String, List<BIR>>();
		BIR[] smp = new BIR[sample.size()];
		smp=sample.toArray(smp);
		gallery.put("check", record);
		Map<BiometricType, Float> result= bioProviderImpl_V_0_8.getModalityQuality(smp,modalityParams);
        assertThat(result.get(BiometricType.FINGER),is(90.0F));
	}
	
	@Test
	public void getSegmentQualityFalseTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceTwo0_8");
		input.put(BiometricType.FINGER, modalityParams);
		bioProviderImpl_V_0_8.init(input);
		Map<String, List<BIR>> gallery= new HashMap<String, List<BIR>>();
		BIR[] smp = new BIR[sample.size()];
		smp=sample.toArray(smp);
		gallery.put("check", record);
		float[] result= bioProviderImpl_V_0_8.getSegmentQuality(smp,modalityParams);
        assertThat(result[0],is(0F));
	}
	
	@Test
	public void getModalityQualityFalseTest() throws Exception {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceTwo0_8");
		input.put(BiometricType.FINGER, modalityParams);
		bioProviderImpl_V_0_8.init(input);
		Map<String, List<BIR>> gallery= new HashMap<String, List<BIR>>();
		BIR[] smp = new BIR[sample.size()];
		smp=sample.toArray(smp);
		gallery.put("check", record);
		Map<BiometricType, Float> result= bioProviderImpl_V_0_8.getModalityQuality(smp,modalityParams);
        assertThat(result.get(BiometricType.FINGER),is(0F));
	}
	
	@Test
	public void extractTemplateTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceOne0_8");
		input.put(BiometricType.FINGER, modalityParams);
		bioProviderImpl_V_0_8.init(input);
		Map<String, List<BIR>> gallery= new HashMap<String, List<BIR>>();
		gallery.put("check", record);
		List<BIR> result= bioProviderImpl_V_0_8.extractTemplate(sample,modalityParams);
        assertThat(result.size(),is(sample.size()));
	}
	
	@Test
	public void extractTemplateFalseTest() throws CbeffException, BiometricException {
		iBioProviderApi bioProviderImpl_V_0_8 = new BioProviderImpl_V_0_8();
		Map<BiometricType,Map<String, String>> input = new HashMap<>();
		Map<String, String> modalityParams = new HashMap<>();
		modalityParams.put(ProviderConstants.VERSION,"0.8");
		modalityParams.put(ProviderConstants.CLASSNAME,"io.mosip.kernel.biosdk.provider.test.dto.SDKInstanceTwo0_8");
		input.put(BiometricType.FINGER, modalityParams);
		bioProviderImpl_V_0_8.init(input);
		Map<String, List<BIR>> gallery= new HashMap<String, List<BIR>>();
		gallery.put("check", record);
		List<BIR> result= bioProviderImpl_V_0_8.extractTemplate(sample,modalityParams);
        assertTrue(result.stream().filter(x -> x!=null).count()==0);
	}


}