package io.mosip.kernel.core.cbeffutil.spi;

import java.util.List;
import java.util.Map;

import io.mosip.kernel.core.cbeffutil.entity.BIR;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;

/**
 * Creates, updates, validates, and extracts CBEFF XML biometric records.
 * <p>
 * Contract: implementations perform XML/JAXB processing in-memory (no HTTP).
 * Byte arrays and BIR lists must be non-null; empty lists yield empty CBEFF.
 * Deprecated since 1.1.7 in favor of bio-utils; kept for downstream MOSIP
 * modules that still compile against this SPI.
 * </p>
 *
 * @author Ramadurai Pandian
 * @deprecated since 1.1.7; biometric CBEFF handling lives in bio-utils
 */
@Deprecated(since = "1.1.7")
public interface CbeffUtil {

	/**
	 * Builds CBEFF XML from the given BIR list using the default XSD.
	 *
	 * @param cbeffPack never-null list of BIRs; may be empty
	 * @return never-null XML bytes
	 * @throws Exception when JAXB marshalling or schema validation fails
	 */
	public byte[] createXML(List<BIR> cbeffPack) throws Exception;

	/**
	 * Merges additional BIRs into existing CBEFF XML.
	 *
	 * @param cbeffPackList never-null BIRs to add
	 * @param fileBytes     never-null existing CBEFF XML
	 * @return never-null updated XML bytes
	 * @throws Exception when parse, merge, or marshalling fails
	 */
	public byte[] updateXML(List<BIR> cbeffPackList, byte[] fileBytes) throws Exception;

	/**
	 * Validates CBEFF XML against the supplied XSD.
	 *
	 * @param xmlBytes never-null CBEFF XML
	 * @param xsdBytes never-null XSD schema
	 * @return {@code true} if valid
	 * @throws Exception when XML is invalid or I/O fails
	 */
	public boolean validateXML(byte[] xmlBytes, byte[] xsdBytes) throws Exception;

	/**
	 * Validates CBEFF XML against the default MOSIP CBEFF XSD.
	 *
	 * @param xmlBytes never-null CBEFF XML
	 * @return {@code true} if valid
	 * @throws Exception when XML is invalid or I/O fails
	 */
	public boolean validateXML(byte[] xmlBytes) throws Exception;

	/**
	 * Extracts BDB payloads keyed by identifier for the given type and subtype.
	 *
	 * @param fileBytes never-null CBEFF XML
	 * @param type      biometric type such as Finger or Iris; may be null for all
	 * @param subType   subtype such as Left; may be null for all
	 * @return never-null map of identifier to BDB (typically Base64); may be empty
	 * @throws Exception when parse fails
	 */
	public Map<String, String> getBDBBasedOnType(byte[] fileBytes, String type, String subType) throws Exception;

	/**
	 * Unmarshals CBEFF XML into JAXB {@link BIRType} records.
	 *
	 * @param xmlBytes never-null CBEFF XML
	 * @return never-null list; may be empty
	 * @throws Exception when unmarshalling fails
	 */
	public List<BIRType> getBIRDataFromXML(byte[] xmlBytes) throws Exception;

	/**
	 * Extracts every BDB matching type and subtype from CBEFF XML.
	 *
	 * @param xmlBytes never-null CBEFF XML
	 * @param type     biometric type; may be null for all
	 * @param subType  subtype; may be null for all
	 * @return never-null map of identifier to BDB; may be empty
	 * @throws Exception when parse fails
	 */
	public Map<String, String> getAllBDBData(byte[] xmlBytes, String type, String subType) throws Exception;

	/**
	 * Builds CBEFF XML from BIRs validated against the given XSD.
	 *
	 * @param birList never-null BIR list; may be empty
	 * @param xsd     never-null XSD bytes
	 * @return never-null XML bytes
	 * @throws Exception when marshalling or validation fails
	 */
	public byte[] createXML(List<BIR> birList, byte[] xsd) throws Exception;

	/**
	 * Converts JAXB {@link BIRType} records to kernel {@link BIR} entities.
	 *
	 * @param birType never-null JAXB list; may be empty
	 * @return never-null converted list; same size as input
	 */
	public List<BIR> convertBIRTypeToBIR(List<BIRType> birType);

	/**
	 * Unmarshals CBEFF XML and returns only BIRs of the given biometric type.
	 *
	 * @param xmlBytes never-null CBEFF XML
	 * @param type     never-null biometric type filter
	 * @return never-null filtered JAXB list; may be empty
	 * @throws Exception when unmarshalling fails
	 */
	public List<BIRType> getBIRDataFromXMLType(byte[] xmlBytes, String type) throws Exception;

}
