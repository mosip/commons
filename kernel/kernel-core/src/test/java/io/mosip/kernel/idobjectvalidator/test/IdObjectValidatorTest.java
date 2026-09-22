package io.mosip.kernel.idobjectvalidator.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.github.fge.jackson.JsonLoader;

import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.idobjectvalidator.impl.IdObjectSchemaValidator;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class IdObjectValidatorTest {
	
	
	@InjectMocks
	IdObjectSchemaValidator validator;
	
	private String validSchemaJson = "{\"$schema\":\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\",\"description\":\"string\","
			+ "\"additionalProperties\":false,\"title\":\"string\",\"type\":\"object\",\"definitions\":{\"simpleType\":"
			+ "{\"uniqueItems\":true,\"additionalItems\":false,\"type\":\"array\",\"items\":{\"additionalProperties\":false,\"type\":\"object\","
			+ "\"required\":[\"language\",\"value\"],\"properties\":{\"language\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}}},"
			+ "\"documentType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},"
			+ "\"type\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}},\"biometricsType\":{\"additionalProperties\":false,\"type\":\"object\","
			+ "\"properties\":{\"format\":{\"type\":\"string\"},\"version\":{\"type\":\"number\",\"minimum\":0},\"value\":{\"type\":\"string\"}}}},"
			+ "\"properties\":{\"identity\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"IDSchemaVersion\",\"fullName\",\"dateOfBirth\","
			+ "\"gender\",\"addressLine1\",\"proofOfIdentity\",\"individualBiometrics\"],\"properties\":{\"proofOfIdentity\":{\"fieldCategory\":\"evidence\","
			+ "\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"individualBiometrics\":{\"bioAttributes\":"
			+ "[\"leftIris\", \"face\"],\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},"
			+ "\"IDSchemaVersion\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"minimum\":0},\"proofOfAddress\":"
			+ "{\"fieldCategory\":\"optional\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"gender\":"
			+ "{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"parentOrGuardianBiometrics\":"
			+ "{\"bioAttributes\":[\"face\"],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},"
			+ "\"fullName\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},"
			+ "\"addressLine1\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},"
			+ "\"dateOfBirth\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"}}}}}";
	
	private String schemaJsonWithoutDefinitions = "{\"$schema\":\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\",\"description\":\"string\","
			+ "\"additionalProperties\":false,\"title\":\"string\",\"type\":\"object\",\"properties\":{\"identity\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"IDSchemaVersion\",\"fullName\",\"dateOfBirth\","
			+ "\"gender\",\"addressLine1\",\"proofOfIdentity\",\"individualBiometrics\"],\"properties\":{\"proofOfIdentity\":{\"fieldCategory\":\"evidence\","
			+ "\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"individualBiometrics\":{\"bioAttributes\":"
			+ "[\"leftIris\", \"face\"],\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},"
			+ "\"IDSchemaVersion\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"minimum\":0},\"proofOfAddress\":"
			+ "{\"fieldCategory\":\"optional\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"gender\":"
			+ "{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"parentOrGuardianBiometrics\":"
			+ "{\"bioAttributes\":[\"face\"],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},"
			+ "\"fullName\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},"
			+ "\"addressLine1\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},"
			+ "\"dateOfBirth\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"}}}}}";
	
	
	private String schemaWithValidators = "{\"$schema\":\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\",\"description\":\"string\","
			+ "\"additionalProperties\":false,\"title\":\"string\",\"type\":\"object\",\"definitions\":{\"simpleType\":"
			+ "{\"uniqueItems\":true,\"additionalItems\":false,\"type\":\"array\",\"items\":{\"additionalProperties\":false,\"type\":\"object\","
			+ "\"required\":[\"language\",\"value\"],\"properties\":{\"language\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}}},"
			+ "\"documentType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},"
			+ "\"type\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}},\"biometricsType\":{\"additionalProperties\":false,\"type\":\"object\","
			+ "\"properties\":{\"format\":{\"type\":\"string\"},\"version\":{\"type\":\"number\",\"minimum\":0},\"value\":{\"type\":\"string\"}}}},"
			+ "\"properties\":{\"identity\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"IDSchemaVersion\",\"fullName\",\"dateOfBirth\","
			+ "\"gender\",\"addressLine1\",\"proofOfIdentity\",\"individualBiometrics\"],\"properties\":{\"proofOfIdentity\":{\"fieldCategory\":\"evidence\","
			+ "\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"individualBiometrics\":{\"bioAttributes\":"
			+ "[\"leftIris\", \"face\"],\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},"
			+ "\"IDSchemaVersion\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"minimum\":0},\"proofOfAddress\":"
			+ "{\"fieldCategory\":\"optional\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"gender\":"
			+ "{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"parentOrGuardianBiometrics\":"
			+ "{\"bioAttributes\":[\"face\"],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},"
			+ "\"fullName\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},"
			+ "\"addressLine1\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},"
			+ "\"dateOfBirth\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\",\"validators\":"
			+ "[{\"type\": \"regex\",\"validator\":\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])/([0][1-9]|1[0-2])/([0][1-9]|[1-2][0-9]|3[01])$\",\"arguments\":[]}] }}}}}";
	
	private String schemaWithValidators_1 = "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"description\":\"string\",\"additionalProperties\":false,"
			+ "\"title\":\"string\",\"type\":\"object\",\"definitions\":{\"simpleType\":{\"uniqueItems\":true,\"additionalItems\":false,\"type\":\"array\","
			+ "\"items\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"language\",\"value\"],"
			+ "\"properties\":{\"language\":{\"type\":\"string\"},\"value\":{\"type\":\"string\","
			+ "\"validators\":[{\"type\":\"regex\",\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[]}]}}}},"
			+ "\"documentType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"type\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}},\"biometricsType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"version\":{\"type\":\"number\",\"minimum\":0},\"value\":{\"type\":\"string\"}}}},\"properties\":{\"identity\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"IDSchemaVersion\",\"fullName\",\"dateOfBirth\",\"gender\",\"addressLine1\",\"proofOfIdentity\",\"individualBiometrics\"],\"properties\":{\"proofOfIdentity\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#/definitions/documentType\"},\"individualBiometrics\":{\"bioAttributes\":[\"leftIris\",\"face\"],\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#/definitions/biometricsType\"},\"IDSchemaVersion\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"minimum\":0},\"proofOfAddress\":{\"fieldCategory\":\"optional\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#/definitions/documentType\"},\"gender\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#/definitions/simpleType\"},\"parentOrGuardianBiometrics\":{\"bioAttributes\":[\"face\"],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#/definitions/biometricsType\"},\"fullName\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#/definitions/simpleType\"},\"addressLine1\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#/definitions/simpleType\"},\"referenceIdentityNumber\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"validators\":[{\"type\":\"regex\",\"validator\":\"^([0-9]{10,30})$\",\"arguments\":[]}]},\"dateOfBirth\":{\"fieldCategory\":\"Pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\",\"validators\":[{\"type\":\"regex\",\"validator\":\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])/([0][1-9]|1[0-2])/([0][1-9]|[1-2][0-9]|3[01])$\",\"arguments\":[]}]}}}}}";

	
	private String SCHEMA =  "{\"$schema\":\"http:\\/\\/json-schema.org\\/draft-07\\/schema#\",\"description\":\"test mosip id schema\",\"additionalProperties\":false,\"title\":\"mosip id schema\",\"type\":\"object\",\"definitions\":{\"simpleType\":{\"uniqueItems\":true,\"additionalItems\":false,\"type\":\"array\",\"items\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"language\",\"value\"],\"properties\":{\"language\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}}},\"documentType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"type\":{\"type\":\"string\"},\"value\":{\"type\":\"string\"}}},\"biometricsType\":{\"additionalProperties\":false,\"type\":\"object\",\"properties\":{\"format\":{\"type\":\"string\"},\"version\":{\"type\":\"number\",\"minimum\":0},\"value\":{\"type\":\"string\"}}}},\"properties\":{\"identity\":{\"additionalProperties\":false,\"type\":\"object\",\"required\":[\"IDSchemaVersion\",\"UIN\",\"fullName\",\"dateOfBirth\",\"gender\",\"addressLine1\",\"addressLine2\",\"addressLine3\",\"region\",\"province\",\"city\",\"postalCode\",\"phone\",\"email\",\"zone\",\"proofOfIdentity\",\"proofOfRelationship\",\"proofOfDateOfBirth\",\"proofOfException\",\"proofOfException\",\"individualBiometrics\"],\"properties\":{\"proofOfAddress\":{\"fieldCategory\":\"optional\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"gender\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"city\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"postalCode\":{\"validators\":[{\"validator\":\"^[(?i)A-Z0-9]{5}$|^NA$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"individualBiometrics\":{\"bioAttributes\":[\"leftIris\", \"face\"],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"province\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"zone\":{\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfDateOfBirth\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"addressLine1\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"addressLine2\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"residenceStatus\":{\"fieldCategory\":\"kyc\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"referenceIdentityNumber\":{\"fieldCategory\":\"kyc\",\"format\":\"none\",\"fieldType\":\"default\",\"type\":\"string\"},\"addressLine3\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"email\":{\"validators\":[{\"validator\":\"^[a-zA-Z-\\\\+]+(\\\\.[a-zA-Z]+)*@[a-zA-Z-]+(\\\\.[a-zA-Z]+)*(\\\\.[a-zA-Z]{2,})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianRID\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"parentOrGuardianBiometrics\":{\"bioAttributes\":[\"face\"],\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/biometricsType\"},\"fullName\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"dateOfBirth\":{\"validators\":[{\"validator\":\"^(1869|18[7-9][0-9]|19[0-9][0-9]|20[0-9][0-9])\\/([0][1-9]|1[0-2])\\/([0][1-9]|[1-2][0-9]|3[01])$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianUIN\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfIdentity\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"IDSchemaVersion\":{\"fieldCategory\":\"none\",\"format\":\"none\",\"type\":\"number\",\"fieldType\":\"default\",\"minimum\":0},\"proofOfException\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"phone\":{\"validators\":[{\"validator\":\"^([6-9]{1})([0-9]{9})$\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\"},\"parentOrGuardianName\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"},\"proofOfRelationship\":{\"fieldCategory\":\"evidence\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/documentType\"},\"UIN\":{\"fieldCategory\":\"none\",\"format\":\"none\",\"type\":\"string\",\"fieldType\":\"default\",\"minimum\":0},\"region\":{\"validators\":[{\"validator\":\"^(?=.{0,50}$).*\",\"arguments\":[],\"type\":\"regex\"}],\"fieldCategory\":\"pvt\",\"format\":\"none\",\"fieldType\":\"default\",\"$ref\":\"#\\/definitions\\/simpleType\"}}}}}";
	
	@Before
	public void setup() {
		ReflectionTestUtils.setField(validator, "mapper", JsonMapper.builder()
			    .addModule(new AfterburnerModule())
			    .build());
	}
	
	@Test
	public void testValidSchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		assertEquals(true, validator.validateIdObject(validSchemaJson, idObject));
	}
	
	@Test(expected = IdObjectIOException.class)
	public void testInvalidSchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		validator.validateIdObject(schemaJsonWithoutDefinitions, idObject);
	}
	
	@Test(expected = InvalidIdSchemaException.class)
	public void testNullSchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		validator.validateIdObject(null, idObject);
	}
	
	@Test(expected = InvalidIdSchemaException.class)
	public void testNoSchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		validator.validateIdObject("", idObject);
	}
	
	@Test(expected = InvalidIdSchemaException.class)
	public void testEmptySchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		validator.validateIdObject("{}", idObject);
	}
	
	@Test(expected=IdObjectValidationFailedException.class)
	public void testValidSchemaAndInValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-json.json");
		validator.validateIdObject(validSchemaJson, idObject, Collections.singletonList("proofOfIdentity"));
	}
	
	@Test
	public void testValidSchemaAndInValidIDObjectWithResponseCheck() throws IOException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/invalid-IDObject.json");		
		try {
			validator.validateIdObject(validSchemaJson, idObject);
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/IDSchemaVersion", e.getErrorTexts().get(0));			
		}
	}
	
	@Test
	public void testSchemaValidatorsAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		assertEquals(true, validator.validateIdObject(schemaWithValidators, idObject));
	}
	
	@Test(expected= IdObjectValidationFailedException.class)
	public void testSchemaValidatorsAndInvalidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject.json");
		validator.validateIdObject(schemaWithValidators, idObject);
	}
	
	@Test
	public void testSchemaValidatorsAndInvalidIDObjectWithResponseCheck() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject.json");
		try {
			validator.validateIdObject(schemaWithValidators, idObject);
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/dateOfBirth", e.getErrorTexts().get(0));			
		}
	}
	
	@Test
	public void testValidators() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject2.json");
		assertEquals(true, validator.validateIdObject(schemaWithValidators_1, idObject));		
	}
	
	@Test
	public void testValidatorsWithInvalidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject3.json");		
		try {
			validator.validateIdObject(schemaWithValidators_1, idObject);
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/fullName/0/value", e.getErrorTexts().get(0));			
		}
	}
	
	@Test
	public void testValidatorsWithInvalidIDObject_2() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject4.json");		
		try {
			validator.validateIdObject(schemaWithValidators_1, idObject);
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/referenceIdentityNumber", e.getErrorTexts().get(0));			
		}
	}
	
	@Test
	public void testValidatorsForUpdateUIN() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/UINcheck-IDObject5.json");
		assertEquals(true, validator.validateIdObject(SCHEMA, idObject, Collections.singletonList("UIN")));		
	}
	
	@Test
	public void testValidatorsForLostUIN() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/UINcheck-IDObject5.json");
		assertEquals(true, validator.validateIdObject(SCHEMA, idObject, Collections.singletonList("UIN")));		
	}
	
	@Test
	public void testValidatorsForUpdateUINWithNull() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/UINcheck-IDObject6.json");
		try {
			validator.validateIdObject(SCHEMA, idObject,Collections.singletonList("UIN"));
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/UIN", e.getErrorTexts().get(0));			
		}				
	}
	
	@Test
	public void testValidatorsForLostUINWithNull() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/UINcheck-IDObject6.json");
		try {
			validator.validateIdObject(SCHEMA, idObject, Collections.singletonList("uin"));
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/UIN", e.getErrorTexts().get(0));			
		}	
	}
}
