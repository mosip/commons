package io.mosip.kernel.packetmanager.impl;

import io.mosip.kernel.core.cbeffutil.entity.BDBInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.entity.BIRInfo;
import io.mosip.kernel.core.cbeffutil.entity.BIRVersion;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.ProcessedLevelType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.PurposeType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.QualityType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.RegistryIDType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleAnySubtypeType;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.SingleType;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.packetmanager.constants.Biometric;
import io.mosip.kernel.packetmanager.constants.PacketManagerConstants;
import io.mosip.kernel.packetmanager.spi.BiometricDataBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Component
public class CbeffBIRBuilder implements BiometricDataBuilder {
	
	@Value("${mosip.kernel.packetmanager.cbeff_only_unique_tags:Y}")
	private String uniqueTagsEnabled;
	
	private static SecureRandom random = new SecureRandom(String.valueOf(5000).getBytes());
	
	@Override
	public BIR buildBIR(byte[] bdb, double qualityScore, SingleType singleType, String bioAttribute) {

		// Format
		RegistryIDType birFormat = new RegistryIDType();
		birFormat.setOrganization(PacketManagerConstants.CBEFF_DEFAULT_FORMAT_ORG);
		birFormat.setType(String.valueOf(Biometric.getFormatType(singleType)));

		// Algorithm
		RegistryIDType birAlgorithm = new RegistryIDType();
		birAlgorithm.setOrganization(PacketManagerConstants.CBEFF_DEFAULT_ALG_ORG);
		birAlgorithm.setType(PacketManagerConstants.CBEFF_DEFAULT_ALG_TYPE);

		// Quality Type
		QualityType qualityType = new QualityType();
		qualityType.setAlgorithm(birAlgorithm);
		qualityType.setScore((long) qualityScore);

		return new BIR.BIRBuilder()
				.withBdb(bdb)
				.withElement(Arrays.asList(getCBEFFTestTag(singleType)))
				.withVersion(new BIRVersion.BIRVersionBuilder().withMajor(1).withMinor(1).build())
				.withCbeffversion(new BIRVersion.BIRVersionBuilder().withMajor(1).withMinor(1).build())
				.withBirInfo(new BIRInfo.BIRInfoBuilder().withIntegrity(false).build())
				.withBdbInfo(new BDBInfo.BDBInfoBuilder()
						.withFormat(birFormat)
						.withQuality(qualityType)
						.withType(Arrays.asList(singleType))
						.withSubtype(getSubTypes(singleType, bioAttribute))
						.withPurpose(PurposeType.ENROLL)
						.withLevel(ProcessedLevelType.RAW)
						.withCreationDate(LocalDateTime.now(ZoneId.of("UTC")))
						.withIndex(UUID.randomUUID().toString())
						.build())
				.build();
	}
	
	private JAXBElement<String> getCBEFFTestTag(SingleType biometricType) {
		String testTagElementName = null;
		String testTagType = "y".equalsIgnoreCase(uniqueTagsEnabled) ? "Unique" : 
			(random.nextInt() % 2 == 0 ? "Duplicate" : "Unique");
			
		switch (biometricType) {
		case FINGER:
			testTagElementName = "TestFinger";
			break;
		case IRIS:
			testTagElementName = "TestIris";
			break;
		case FACE:
			testTagElementName = "TestFace";
			break;
		default:
			break;
		}

		return new JAXBElement<>(new QName("testschema", testTagElementName), String.class, testTagType);
	}
	
	@SuppressWarnings("incomplete-switch")
	private List<String> getSubTypes(SingleType singleType, String bioAttribute) {
		List<String> subtypes = new LinkedList<>();		
		switch (singleType) {
		case FINGER:
			subtypes.add(bioAttribute.contains("left") ? SingleAnySubtypeType.LEFT.value() :
				SingleAnySubtypeType.RIGHT.value());
			if(bioAttribute.toLowerCase().contains("thumb"))
				subtypes.add(SingleAnySubtypeType.THUMB.value());
			else {
				String val = bioAttribute.toLowerCase().replace("left", "").replace("right", "");
				subtypes.add(SingleAnySubtypeType.fromValue(StringUtils.capitalizeFirstLetter(val).concat("Finger")).value());
			}
			break;
		case IRIS:			
			subtypes.add(bioAttribute.contains("left") ? SingleAnySubtypeType.LEFT.value() :
				SingleAnySubtypeType.RIGHT.value());
			break;
		case FACE:
			break;
		}
		return subtypes;
	}

}
