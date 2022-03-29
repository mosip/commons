/**
 * 
 */
package io.mosip.kernel.core.cbeffutil.common;

import java.io.ByteArrayInputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * @author M1049825
 *
 */
public class CbeffXSDValidator {

	public static boolean validateXML(byte[] xsdBytes, byte[] xmlBytes) throws Exception {
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		Schema schema = factory.newSchema(new StreamSource(new ByteArrayInputStream(xsdBytes)));
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(new ByteArrayInputStream(xmlBytes)));
		return true;
	}

}
