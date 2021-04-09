package io.mosip.kernel.biometrics.spi;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.biometrics.entities.BIR;

/**
 * @author 

 * 
 *         Interface for Cbeff Interface
 *
 */
public interface CbeffUtil {

	public byte[] createXML(List<BIR> cbeffPack) throws Exception;

	public byte[] updateXML(List<BIR> cbeffPackList, byte[] fileBytes) throws Exception;

	public boolean validateXML(byte[] xmlBytes, byte[] xsdBytes) throws Exception;

	public boolean validateXML(byte[] xmlBytes) throws Exception;

	public Map<String, String> getBDBBasedOnType(byte[] fileBytes, String type,String subType) throws Exception;

	public List<BIR> getBIRDataFromXML(byte[] xmlBytes) throws Exception;

	public Map<String, String> getAllBDBData(byte[] xmlBytes, String type, String subType) throws Exception;

	public byte[] createXML(List<BIR> birList, byte[] xsd) throws Exception;

	public List<BIR> getBIRDataFromXMLType(byte[] xmlBytes, String type) throws Exception;

}
