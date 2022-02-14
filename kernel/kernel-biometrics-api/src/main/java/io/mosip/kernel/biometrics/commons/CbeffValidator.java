/**
 * 
 */
package io.mosip.kernel.biometrics.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.xml.sax.SAXException;

import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biometrics.constant.OtherKey;
import io.mosip.kernel.biometrics.entities.BDBInfo;
import io.mosip.kernel.biometrics.entities.BIR;
import io.mosip.kernel.biometrics.entities.SingleAnySubtypeType;
import io.mosip.kernel.core.cbeffutil.common.CbeffXSDValidator;
import io.mosip.kernel.core.cbeffutil.constant.CbeffConstant;
import io.mosip.kernel.core.cbeffutil.exception.CbeffException;
import io.mosip.kernel.core.util.CryptoUtil;

/**
 * @author Ramadurai Pandian
 * 
 *         An Utility Class to validate the data before generating an valid
 *         CBEFF XML and to get all the data based on Type and SubType
 *
 */
public class CbeffValidator {
	/**
	 * Method used for custom validation of the BIR
	 * 
	 * @param birRoot BIR data
	 * 
	 * @return boolean value if BIR is valid
	 * 
	 * @exception CbeffException when any condition fails
	 * 
	 */
	public static boolean validateXML(BIR birRoot) throws CbeffException {
		if (birRoot == null) {
			throw new CbeffException("BIR value is null");
		}
		List<BIR> birList = birRoot.getBirs();
		for (BIR bir : birList) {
			if (bir != null) {

				boolean isException = bir.getOthers() != null && bir.getOthers().entrySet().stream()
						.anyMatch(e -> OtherKey.EXCEPTION.equals(e.getKey()) && "true".equals((String) e.getValue()));

				if ((bir.getBdb() == null || bir.getBdb().length < 1) && !isException)
					throw new CbeffException("BDB value can't be empty");

				if (bir.getBdbInfo() == null)
					throw new CbeffException("BDB information can't be empty");

				BDBInfo bdbInfo = bir.getBdbInfo();
//					if (!Long.valueOf(bdbInfo.getFormat().getOrganization()).equals(CbeffConstant.FORMAT_OWNER)) {
//						throw new CbeffException("Patron Format Owner should be standard specified of value "
//								+ CbeffConstant.FORMAT_OWNER);
//					}
				List<BiometricType> biometricTypes = bdbInfo.getType();
				if (biometricTypes == null || biometricTypes.isEmpty()) {
					throw new CbeffException("Type value needs to be provided");
				}
				if (!validateFormatType(Long.valueOf(bdbInfo.getFormat().getType()), biometricTypes)) {
					throw new CbeffException("Patron Format type is invalid");
				}
			}
		}
		return true;
	}

	/**
	 * Method used for validation of Format Type
	 * 
	 * @param formatType     format type
	 * 
	 * @param biometricTypes List of types
	 * 
	 * @return boolean value if format type is matching with type
	 * 
	 */
	private static boolean validateFormatType(long formatType, List<BiometricType> biometricTypes) {
		BiometricType biometricType = biometricTypes.get(0);
		switch (biometricType.value()) {
		case "Finger":
			return formatType == CbeffConstant.FORMAT_TYPE_FINGER
					|| formatType == CbeffConstant.FORMAT_TYPE_FINGER_MINUTIAE;
		case "Iris":
			return formatType == CbeffConstant.FORMAT_TYPE_IRIS;
		case "Face":
			return formatType == CbeffConstant.FORMAT_TYPE_FACE;
		case "HandGeometry":
			return formatType == CbeffConstant.FORMAT_TYPE_FACE;
		}

		return false;
	}

	/**
	 * Method used for getting Format Type Id from type string
	 * 
	 * @param type format type
	 * 
	 * @return long format type id
	 * 
	 */
	private static long getFormatType(String type) {
		switch (type.toLowerCase()) {
		case "finger":
			return CbeffConstant.FORMAT_TYPE_FINGER;
		case "iris":
			return CbeffConstant.FORMAT_TYPE_IRIS;
		case "fmr":
			return CbeffConstant.FORMAT_TYPE_FINGER_MINUTIAE;
		case "face":
			return CbeffConstant.FORMAT_TYPE_FACE;
		case "handgeometry":
			return CbeffConstant.FORMAT_TYPE_FACE;
		}
		return 0;
	}

	/**
	 * Method used for creating XML bytes using JAXB
	 * 
	 * @param bir BIR type
	 * @param xsd xml schema definition
	 * @return byte[] byte array of XML data
	 * 
	 * @exception Exception exception
	 * 
	 */
	public static byte[] createXMLBytes(BIR bir, byte[] xsd) throws Exception {
		CbeffValidator.validateXML(bir);
		JAXBContext jaxbContext = JAXBContext.newInstance(BIR.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE); // To
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(baos);
		jaxbMarshaller.marshal(bir, writer);
		byte[] savedData = baos.toByteArray();
		writer.close();
		try {
			CbeffXSDValidator.validateXML(xsd, savedData);
		} catch (SAXException sax) {
			String message = sax.getMessage();
			message = message.substring(message.indexOf(":"));
			throw new CbeffException("XSD validation failed due to attribute " + message);
		}
		return savedData;
	}

	/*
	 * private static byte[] readXSD(String name) throws IOException { byte[]
	 * fileContent = Files.readAllBytes(Paths.get(tempPath + "/schema/" + name +
	 * ".xsd")); return fileContent; }
	 */

	/**
	 * Method used for BIR Type
	 * 
	 * @param fileBytes byte array of XML data
	 * 
	 * @return BIR BIR data
	 * 
	 * @exception Exception exception
	 * 
	 */
	public static BIR getBIRFromXML(byte[] fileBytes) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(BIR.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		JAXBElement<BIR> jaxBir = unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(fileBytes)),
				BIR.class);
		BIR bir = jaxBir.getValue();
		return bir;
	}

	/**
	 * Method used for searching Cbeff data based on type and subtype
	 * 
	 * @param bir     BIR data
	 * 
	 * @param type    format type
	 * 
	 * @param subType format subtype
	 * 
	 * @return bdbMap
	 * 
	 * @exception Exception exception
	 * 
	 */
	public static Map<String, String> getBDBBasedOnTypeAndSubType(BIR bir, String type, String subType)
			throws Exception {

		if (type == null && subType == null) {
			return getAllLatestDatafromBIR(bir);
		}
		BiometricType biometricType = null;
		SingleAnySubtypeType singleAnySubType = null;
		Long formatType = null;
		if (type != null) {
			biometricType = getBiometricType(type);
			formatType = getFormatType(type);
		}
		if (subType != null) {
			singleAnySubType = getSingleAnySubtype(subType);
		}
		Map<String, String> bdbMap = new HashMap<>();
		if (bir.getBirs() != null && !bir.getBirs().isEmpty()) {
			populateBDBMap(bir, biometricType, singleAnySubType, formatType, bdbMap);
		}
		Map<String, String> map = new TreeMap<>(bdbMap);
		Map<String, String> finalMap = new HashMap<>();
		for (Map.Entry<String, String> mapEntry : map.entrySet()) {
			String pattern = mapEntry.getKey().substring(0, mapEntry.getKey().lastIndexOf("_"));
			if (mapEntry.getKey().contains(pattern)) {
				finalMap.put(mapEntry.getKey().substring(0, mapEntry.getKey().lastIndexOf("_")), mapEntry.getValue());
			}
		}
		return finalMap;
	}

	private static void populateBDBMap(BIR birRoot, BiometricType biometricType, SingleAnySubtypeType singleAnySubType,
			Long formatType, Map<String, String> bdbMap) {
		for (BIR bir : birRoot.getBirs()) {
			BDBInfo bdbInfo = bir.getBdbInfo();

			if (bdbInfo != null) {
				List<String> singleSubTypeList = bdbInfo.getSubtype() == null ? List.of() : bdbInfo.getSubtype();
				List<BiometricType> biometricTypes = bdbInfo.getType();
				String bdbFormatType = bdbInfo.getFormat().getType();
				boolean formatMatch = Long.valueOf(bdbFormatType).equals(formatType);
				if (singleAnySubType == null && biometricTypes.contains(biometricType) && formatMatch) {
					bdbMap.put(
							(biometricType != null ? biometricType.toString() : null) + "_"
									+ String.join(" ", singleSubTypeList) + "_" + String.valueOf(bdbFormatType) + "_"
									+ bdbInfo.getCreationDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
							CryptoUtil.encodeBase64String(bir.getBdb()));
				} else if (biometricType == null
						&& singleSubTypeList.contains(singleAnySubType != null ? singleAnySubType.value() : null)) {
					List<String> singleTypeStringList = convertToList(biometricTypes);
					bdbMap.put(
							String.join(" ", singleTypeStringList) + "_" + String.join(" ", singleSubTypeList) + "_"
									+ String.valueOf(bdbFormatType) + "_"
									+ bdbInfo.getCreationDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
							CryptoUtil.encodeBase64String(bir.getBdb()));
				} else if (biometricTypes.contains(biometricType)
						&& singleSubTypeList.contains(singleAnySubType != null ? singleAnySubType.value() : null)
						&& formatMatch) {
					bdbMap.put(
							(biometricType != null ? biometricType.toString() : null) + "_"
									+ (singleAnySubType != null ? singleAnySubType.value() : null) + "_"
									+ String.valueOf(bdbFormatType) + "_"
									+ bdbInfo.getCreationDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
							CryptoUtil.encodeBase64String(bir.getBdb()));
				}
			}
		}
	}

	private static Map<String, String> getAllLatestDatafromBIR(BIR birRoot) throws Exception {
		Map<String, String> bdbMap = new HashMap<>();
		if (birRoot.getBirs() != null && birRoot.getBirs().size() > 0) {
			for (BIR bir : birRoot.getBirs()) {
				BDBInfo bdbInfo = bir.getBdbInfo();

				if (bdbInfo != null) {
					List<String> singleSubTypeList = bdbInfo.getSubtype();
					List<BiometricType> biometricTypes = bdbInfo.getType();
					if (singleSubTypeList.isEmpty()) {
						singleSubTypeList = new ArrayList<>();
						singleSubTypeList.add("No Subtype");
					}
					String bdbFormatType = bdbInfo.getFormat().getType();
					bdbMap.put(
							String.join(" ", biometricTypes.get(0).toString()) + "_"
									+ String.join(" ", singleSubTypeList) + "_" + String.valueOf(bdbFormatType) + "_"
									+ bdbInfo.getCreationDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
							CryptoUtil.encodeBase64String(bir.getBdb()));
				}
			}
		}
		Map<String, String> map = new TreeMap<>(bdbMap);
		Map<String, String> finalMap = new HashMap<>();
		for (Map.Entry<String, String> mapEntry : map.entrySet()) {
			String pattern = mapEntry.getKey().substring(0, mapEntry.getKey().lastIndexOf("_"));
			if (mapEntry.getKey().contains(pattern)) {
				finalMap.put(mapEntry.getKey().substring(0, mapEntry.getKey().lastIndexOf("_")), mapEntry.getValue());
			}
		}
		return finalMap;
	}

	/**
	 * Method to convert single type list to string
	 * 
	 */
	private static List<String> convertToList(List<BiometricType> biometricTypeList) {
		return biometricTypeList.stream().map(Enum::name).collect(Collectors.toList());
	}

	/**
	 * Method to get enum type from string type
	 * 
	 */
	private static BiometricType getBiometricType(String type) {
		if (isInEnum(type, BiometricType.class)) {
			return BiometricType.valueOf(type);
		} else {
			switch (type) {
			case "FMR":
				return BiometricType.FINGER;
			default:
				return BiometricType.fromValue(type);
			}
		}
	}

	public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
		for (E e : enumClass.getEnumConstants()) {
			if (e.name().equals(value)) {
				return true;
			}
		}
		return false;
	}

	public static Map<String, String> getAllBDBData(BIR birRoot, String type, String subType) throws Exception {
		BiometricType biometricType = null;
		SingleAnySubtypeType singleAnySubType = null;
		Long formatType = null;
		if (type != null) {
			biometricType = getBiometricType(type);
		}
		if (subType != null) {
			singleAnySubType = getSingleAnySubtype(subType);
		}
		if (type != null) {
			formatType = getFormatType(type);
		}
		Map<String, String> bdbMap = new HashMap<>();
		List<BIR> birs = birRoot.getBirs();
		if (birs != null && !birs.isEmpty()) {
			for (BIR bir : birs) {
				BDBInfo bdbInfo = bir.getBdbInfo();

				if (bdbInfo != null) {
					List<String> singleSubTypeList = bdbInfo.getSubtype();
					List<BiometricType> singleTypeList = bdbInfo.getType();
					String bdbFormatType = bdbInfo.getFormat().getType();
					boolean formatMatch = Long.valueOf(bdbFormatType).equals(formatType);
					if (singleAnySubType == null && singleTypeList.contains(biometricType) && formatMatch) {
						bdbMap.put(
								(biometricType != null ? biometricType.toString() : null) + "_" + String.join(" ", singleSubTypeList) + "_"
										+ String.valueOf(bdbFormatType) + "_"
										+ bdbInfo.getCreationDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
								new String(bir.getBdb(), "UTF-8"));
					} else if (biometricType == null
							&& singleSubTypeList.contains(singleAnySubType != null ? singleAnySubType.value() : null)) {
						List<String> singleTypeStringList = convertToList(singleTypeList);
						bdbMap.put(
								String.join(" ", singleSubTypeList) + "_" + String.join(" ", singleTypeStringList) + "_"
										+ String.valueOf(bdbFormatType) + "_"
										+ bdbInfo.getCreationDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
								new String(bir.getBdb(), "UTF-8"));
					} else if (singleTypeList.contains(biometricType)
							&& singleSubTypeList.contains(singleAnySubType != null ? singleAnySubType.value() : null)
							&& formatMatch) {
						bdbMap.put(
								(singleAnySubType != null ? singleAnySubType.value() : null) + "_"
										+ (biometricType != null ? biometricType.toString() : null) + "_" + String.valueOf(bdbFormatType) + "_" + bdbInfo
														.getCreationDate().toInstant(ZoneOffset.UTC).toEpochMilli(),
								new String(bir.getBdb(), "UTF-8"));
					}
				}
			}
		}
		return bdbMap;
	}

	/**
	 * Method to get enum sub type from string subtype
	 * 
	 */
	private static SingleAnySubtypeType getSingleAnySubtype(String subType) {
		return subType != null ? SingleAnySubtypeType.fromValue(subType) : null;
	}

	/*
	 * private static List<BIR> getBIRList(List<BIR> birs) { List<BIR> birList = new
	 * ArrayList<>(); for (BIR bir : birs) { RegistryIDType format = new
	 * RegistryIDType();
	 * format.setOrganization(bir.getBdbInfo().getFormat().getOrganization());
	 * format.setType(bir.getBdbInfo().getFormat().getType()); BIR.BIRBuilder
	 * birBuilder = new BIR.BIRBuilder();
	 * birBuilder.withBdb(bir.getBdb()).withOther(bir.getOthers()) .withBirInfo(new
	 * BIRInfo.BIRInfoBuilder().withIntegrity(bir.getBirInfo().getIntegrity()).build
	 * ()) .withBdbInfo(new BDBInfo.BDBInfoBuilder().withFormat(format)
	 * .withQuality(bir.getBdbInfo().getQuality()).withType(bir.getBdbInfo().getType
	 * ()) .withSubtype(bir.getBdbInfo().getSubtype())
	 * .withPurpose(bir.getBdbInfo().getPurpose()).withLevel(bir.getBdbInfo().
	 * getLevel()) .withCreationDate(bir.getBdbInfo().getCreationDate()).build());
	 * 
	 * VersionType versionType = bir.getVersion(); if(versionType != null) {
	 * birBuilder.withVersion(versionType); }
	 * 
	 * VersionType cbeffversionType = bir.getCbeffversion(); if(cbeffversionType !=
	 * null) { birBuilder.withCbeffversion(cbeffversionType); }
	 * 
	 * 
	 * birList.add(birBuilder.build()); } return birList; }
	 */

	public static List<BIR> getBIRDataFromXMLType(byte[] xmlBytes, String type) throws Exception {
		BiometricType biometricType = null;
		List<BIR> updatedBIRList = new ArrayList<>();
		JAXBContext jaxbContext = JAXBContext.newInstance(BIR.class);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		JAXBElement<BIR> jaxBir = unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(xmlBytes)),
				BIR.class);
		BIR birRoot = jaxBir.getValue();
		for (BIR bir : birRoot.getBirs()) {
			if (type != null) {
				biometricType = getBiometricType(type);
				BDBInfo bdbInfo = bir.getBdbInfo();
				if (bdbInfo != null) {
					List<BiometricType> biometricTypes = bdbInfo.getType();
					if (biometricTypes != null && biometricTypes.contains(biometricType)) {
						updatedBIRList.add(bir);
					}
				}
			}
		}
		return updatedBIRList;
	}
}
