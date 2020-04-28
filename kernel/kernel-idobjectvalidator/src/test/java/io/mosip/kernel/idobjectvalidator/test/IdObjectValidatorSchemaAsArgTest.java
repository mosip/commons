package io.mosip.kernel.idobjectvalidator.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;

import io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorSupportedOperations;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.idobjectvalidator.impl.IdObjectSchemaValidator;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("test")
public class IdObjectValidatorSchemaAsArgTest {
	
	
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
	

	
	@Before
	public void setup() {
		ReflectionTestUtils.setField(validator, "mapper", new ObjectMapper());
	}
	
	@Test
	public void testValidSchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		assertEquals(true, validator.validateIdObject(validSchemaJson, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION));
	}
	
	@Test(expected = IdObjectIOException.class)
	public void testInvalidSchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		validator.validateIdObject(schemaJsonWithoutDefinitions, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
	}
	
	@Test(expected = InvalidIdSchemaException.class)
	public void testNullSchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		validator.validateIdObject(null, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
	}
	
	@Test(expected = InvalidIdSchemaException.class)
	public void testNoSchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		validator.validateIdObject("", idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
	}
	
	@Test(expected = InvalidIdSchemaException.class)
	public void testEmptySchemaAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		validator.validateIdObject("{}", idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
	}
	
	@Test(expected = IdObjectValidationFailedException.class)
	public void testValidSchemaAndInValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/invalid-IDObject.json");
		validator.validateIdObject(validSchemaJson, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
	}
	
	@Test
	public void testValidSchemaAndInValidIDObjectWithResponseCheck() throws IOException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/invalid-IDObject.json");		
		try {
			validator.validateIdObject(validSchemaJson, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/IDSchemaVersion", e.getErrorTexts().get(0));			
		}
	}
	
	@Test
	public void testSchemaValidatorsAndValidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/valid-IDObject.json");
		assertEquals(true, validator.validateIdObject(schemaWithValidators, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION));
	}
	
	@Test(expected= IdObjectValidationFailedException.class)
	public void testSchemaValidatorsAndInvalidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject.json");
		validator.validateIdObject(schemaWithValidators, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
	}
	
	@Test
	public void testSchemaValidatorsAndInvalidIDObjectWithResponseCheck() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject.json");
		try {
			validator.validateIdObject(schemaWithValidators, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/dateOfBirth", e.getErrorTexts().get(0));			
		}
	}
	
	@Test
	public void testValidators() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject2.json");
		assertEquals(true, validator.validateIdObject(schemaWithValidators_1, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION));		
	}
	
	@Test
	public void testValidatorsWithInvalidIDObject() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject3.json");		
		try {
			validator.validateIdObject(schemaWithValidators_1, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/fullName/0/value", e.getErrorTexts().get(0));			
		}
	}
	
	@Test
	public void testValidatorsWithInvalidIDObject_2() throws IOException, IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		JsonNode idObject = JsonLoader.fromResource("/validatorcheck-IDObject4.json");		
		try {
			validator.validateIdObject(schemaWithValidators_1, idObject, IdObjectValidatorSupportedOperations.NEW_REGISTRATION);
		} catch (IdObjectValidationFailedException e) {
			assertEquals("Invalid input parameter - identity/referenceIdentityNumber", e.getErrorTexts().get(0));			
		}
	}

}
