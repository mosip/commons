package io.mosip.kernel.cbeffutil.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.cbeffutil.container.impl.CbeffContainerImpl;
import io.mosip.kernel.core.cbeffutil.common.CbeffValidator;
import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import io.mosip.kernel.core.cbeffutil.spi.CbeffUtil;

/**
 * This class is used to create,update, validate and search Cbeff data.
 *
 * @author Ramadurai Pandian
 */
@Component
public class CbeffImpl implements CbeffUtil {

	/*
	 * XSD storage path from config server
	 */

	/** The config server file storage URL. */
	@Value("${mosip.kernel.xsdstorage-uri}")
	private String configServerFileStorageURL;

	/*
	 * XSD file name
	 */

	/** The schema name. */
	@Value("${mosip.kernel.xsdfile}")
	private String schemaName;

	/** The xsd. */
	private byte[] xsd;

	/**
	 * Load XSD.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@PostConstruct
	public void loadXSD() throws IOException {
		try (InputStream xsdBytes = new URL(configServerFileStorageURL + schemaName).openStream()) {
			xsd = IOUtils.toByteArray(xsdBytes);
		}
	}

	/**
	 * Method used for creating Cbeff XML.
	 *
	 * @param birList pass List of BIR for creating Cbeff data
	 * @return return byte array of XML data
	 * @throws Exception exception
	 */
	@Override
	public byte[] createXML(List<BIR> birList) throws Exception {
		CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
		BIRType bir = cbeffContainer.createBIRType(birList);
		return CbeffValidator.createXMLBytes(bir, xsd);
	}

	/**
	 * Method used for creating Cbeff XML with xsd.
	 *
	 * @param birList pass List of BIR for creating Cbeff data
	 * @param xsd     byte array of XSD data
	 * @return return byte array of XML data
	 * @throws Exception Exception
	 */

	@Override
	public byte[] createXML(List<BIR> birList, byte[] xsd) throws Exception {
		CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
		BIRType bir = cbeffContainer.createBIRType(birList);
		return CbeffValidator.createXMLBytes(bir, xsd);
	}

	/**
	 * Method used for updating Cbeff XML.
	 *
	 * @param birList   pass List of BIR for creating Cbeff data
	 * @param fileBytes the file bytes
	 * @return return byte array of XML data
	 * @throws Exception Exception
	 */
	@Override
	public byte[] updateXML(List<BIR> birList, byte[] fileBytes) throws Exception {
		CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
		BIRType bir = cbeffContainer.updateBIRType(birList, fileBytes);
		return CbeffValidator.createXMLBytes(bir, xsd);
	}

	/**
	 * Method used for validating XML against XSD.
	 *
	 * @param xmlBytes byte array of XML data
	 * @param xsdBytes byte array of XSD data
	 * @return boolean if data is valid or not
	 * @throws Exception Exception
	 */
	@Override
	public boolean validateXML(byte[] xmlBytes, byte[] xsdBytes) throws Exception {
		CbeffContainerImpl cbeffContainer = new CbeffContainerImpl();
		return cbeffContainer.validateXML(xmlBytes, xsdBytes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.cbeffutil.spi.CbeffUtil#validateXML(byte[])
	 */
	@Override
	public boolean validateXML(byte[] xmlBytes) throws Exception {
		return validateXML(xmlBytes, xsd);
	}

	/**
	 * Method used for validating XML against XSD.
	 *
	 * @param fileBytes byte array of XML data
	 * @param type      to be searched
	 * @param subType   to be searched
	 * @return bdbMap Map of type and String of encoded biometric data
	 * @throws Exception Exception
	 */
	@Override
	public Map<String, String> getBDBBasedOnType(byte[] fileBytes, String type, String subType) throws Exception {
		BIRType bir = CbeffValidator.getBIRFromXML(fileBytes);
		return CbeffValidator.getBDBBasedOnTypeAndSubType(bir, type, subType);
	}

	/**
	 * Method used for getting list of BIR from XML bytes.
	 *
	 * @param xmlBytes byte array of XML data
	 * @return List of BIR data extracted from XML
	 * @throws Exception Exception
	 */
	@Override
	public List<BIRType> getBIRDataFromXML(byte[] xmlBytes) throws Exception {
		BIRType bir = CbeffValidator.getBIRFromXML(xmlBytes);
		return bir.getBIR();
	}

	/**
	 * Method used for getting Map of BIR from XML bytes with type and subType.
	 *
	 * @param xmlBytes byte array of XML data
	 * @param type     type
	 * @param subType  subType
	 * @return bdbMap Map of BIR data extracted from XML
	 * @throws Exception Exception
	 */
	@Override
	public Map<String, String> getAllBDBData(byte[] xmlBytes, String type, String subType) throws Exception {
		BIRType bir = CbeffValidator.getBIRFromXML(xmlBytes);
		return CbeffValidator.getAllBDBData(bir, type, subType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.cbeffutil.spi.CbeffUtil#convertBIRTypeToBIR(java.util.
	 * List)
	 */
	@Override
	public List<BIR> convertBIRTypeToBIR(List<BIRType> birType) {
		return CbeffValidator.convertBIRTypeToBIR(birType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.cbeffutil.spi.CbeffUtil#getBIRDataFromXMLType(byte[],
	 * java.lang.String)
	 */
	@Override
	public List<BIRType> getBIRDataFromXMLType(byte[] xmlBytes, String type) throws Exception {
		return CbeffValidator.getBIRDataFromXMLType(xmlBytes, type);
	}
	
	//TODO for testing, will be removed later
//	public static void main(String arg[]) throws Exception
//	{
//		Map<String,String> test= new CbeffImpl().getBDBBasedOnType(readCreatedXML(),"Iris",null);
//		System.out.println(test.size());
//		
//	}
//	
//	private static byte[] readCreatedXML() throws IOException {
//		byte[] fileContent = Files.readAllBytes(Paths.get("C:\\Users\\M1046464\\Downloads\\cbeff1.xml"));
//		return fileContent;
//	}

}
