package io.mosip.kernel.core.util;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.util.constant.JsonUtilConstants;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.kernel.core.util.exception.JsonProcessingException;

/**
 * Jackson JSON serialize/deserialize helpers with MOSIP checked exceptions.
 * <p>
 * Contract: static helpers only; this class is not instantiable. Uses a shared
 * {@link ObjectMapper} with JavaTime and Afterburner modules. {@code jsonString}
 * must be non-null valid JSON. Does not perform MOSIP HTTP.
 * </p>
 *
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
public class JsonUtils {

	private static ObjectMapper objectMapper;

	static {
		objectMapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	}

	/**
	 * Prevents instantiation of this utility.
	 */
	private JsonUtils() {

	}

	/**
	 * This function converts a java object and stores the processed JSON in a file
	 * 
	 * @param className object of the class to be converted to JSON File
	 * @param location  location where to store the generated JSON file
	 * @return true if file is successfully generated,false if file is not generated
	 * @throws JsonGenerationException when JSON is not properly generated
	 * @throws JsonMappingException    when JSON is not properly mapped
	 * @throws IOException             when file is not found
	 */
	/*
	 * public static boolean javaObjectToJsonFile(Object className, String location)
	 * throws JsonGenerationException, JsonMappingException, IOException {
	 * ObjectMapper objectMapper = new ObjectMapper();
	 * objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	 * objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
	 * 
	 * File file = new File(location); try { objectMapper.writeValue(file,
	 * className); } catch (com.fasterxml.jackson.core.JsonGenerationException e) {
	 * throw new
	 * JsonGenerationException(JsonUtilConstants.MOSIP_JSON_GENERATION_ERROR_CODE.
	 * getErrorCode(),
	 * JsonUtilConstants.MOSIP_JSON_GENERATION_ERROR_CODE.getErrorMessage(),
	 * e.getCause()); } catch (com.fasterxml.jackson.databind.JsonMappingException
	 * e) { throw new
	 * JsonMappingException(JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.
	 * getErrorCode(),
	 * JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.getErrorMessage(),
	 * e.getCause()); } catch (java.io.IOException e) { throw new
	 * IOException(JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorCode(),
	 * JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorMessage(),
	 * e.getCause()); } return (file.exists());
	 * 
	 * }
	 */

	/**
	 * Serializes {@code className} to an indented JSON string.
	 *
	 * @param className never-null Java value to serialize
	 * @return never-null JSON text
	 * @throws JsonProcessingException when Jackson cannot serialize {@code className}
	 */
	public static String javaObjectToJsonString(Object className) throws JsonProcessingException {
		String outputJson = null;

		try {
			outputJson = objectMapper.writeValueAsString(className);
		} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
			throw new JsonProcessingException(JsonUtilConstants.MOSIP_JSON_PROCESSING_EXCEPTION.getErrorCode(),
					JsonUtilConstants.MOSIP_JSON_PROCESSING_EXCEPTION.getErrorMessage(), e.getCause());
		}

		return (outputJson);

	}

	/**
	 * Deserializes {@code jsonString} into an instance of {@code className}.
	 *
	 * @param className  never-null target type
	 * @param jsonString never-null JSON object text
	 * @return never-null mapped instance
	 * @throws JsonParseException   when the text is not valid JSON
	 * @throws JsonMappingException when the JSON does not match {@code className}
	 * @throws IOException          when Jackson I/O fails
	 */
	public static Object jsonStringToJavaObject(Class<?> className, String jsonString)
			throws JsonParseException, JsonMappingException, IOException {
		Object returnObject = null;

		try {
			returnObject = objectMapper.readValue(jsonString, className);
		} catch (com.fasterxml.jackson.core.JsonParseException e) {
			throw new JsonParseException(JsonUtilConstants.MOSIP_JSON_PARSE_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_JSON_PARSE_ERROR_CODE.getErrorMessage(), e.getCause());
		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			throw new JsonMappingException(JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.getErrorMessage(), e.getCause());
		} catch (java.io.IOException e) {
			throw new IOException(JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorMessage(), e.getCause());
		}

		return returnObject;

	}

	/**
	 * This method converts the JSON file input and maps it to the java object
	 * 
	 * @param className    class name to which the JSON file is to be mapped
	 * @param fileLocation location of the JSON file
	 * @return JSON mapped object
	 * @throws JsonParseException   when JSON is not properly parsed
	 * @throws JsonMappingException when JSON is not properly mapped
	 * @throws IOException          when file is not found
	 */
	/*
	 * public static Object jsonFileToJavaObject(Class<?> className, String
	 * fileLocation) throws JsonParseException, JsonMappingException, IOException {
	 * ObjectMapper objectMapper = new ObjectMapper();
	 * objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	 * objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS); Object
	 * returnObject = null; try { returnObject = objectMapper.readValue(new
	 * File(fileLocation), className); } catch
	 * (com.fasterxml.jackson.core.JsonParseException e) { throw new
	 * JsonParseException(JsonUtilConstants.MOSIP_JSON_PARSE_ERROR_CODE.getErrorCode
	 * (), JsonUtilConstants.MOSIP_JSON_PARSE_ERROR_CODE.getErrorMessage(),
	 * e.getCause()); } catch (com.fasterxml.jackson.databind.JsonMappingException
	 * e) { throw new
	 * JsonMappingException(JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.
	 * getErrorCode(),
	 * JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.getErrorMessage(),
	 * e.getCause()); } catch (java.io.IOException e) { throw new
	 * IOException(JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorCode(),
	 * JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorMessage(),
	 * e.getCause()); } return returnObject; }
	 */
	/**
	 * Returns the text value of {@code key} from {@code jsonString}.
	 *
	 * @param jsonString never-null JSON object text
	 * @param key        never-null field name
	 * @return field text; may be empty
	 * @throws IOException when the text cannot be parsed as JSON
	 */
	public static String jsonToJacksonJson(String jsonString, String key) throws IOException {
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(jsonString);
		} catch (java.io.IOException e) {
			throw new IOException(JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorMessage(), e.getCause());
		}
		return (jsonNode.get(key).asText());
	}

	/**
	 * Deserializes a JSON array into a list of untyped objects.
	 *
	 * @param jsonArray never-null JSON array text
	 * @return never-null list; may be empty
	 * @throws JsonParseException   when the text is not valid JSON
	 * @throws JsonMappingException when the JSON cannot be mapped to a list
	 * @throws IOException          when Jackson I/O fails
	 */
	public static List<Object> jsonStringToJavaList(String jsonArray)
			throws JsonParseException, JsonMappingException, IOException {
		List<Object> javaList = null;
		try {
			javaList = objectMapper.readValue(jsonArray, new TypeReference<List<Object>>() {
			});
		} catch (com.fasterxml.jackson.core.JsonParseException e) {
			throw new JsonParseException(JsonUtilConstants.MOSIP_JSON_PARSE_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_JSON_PARSE_ERROR_CODE.getErrorMessage(), e.getCause());
		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			throw new JsonMappingException(JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.getErrorMessage(), e.getCause());
		} catch (java.io.IOException e) {
			throw new IOException(JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorMessage(), e.getCause());
		}
		return javaList;
	}

	/**
	 * Deserializes a JSON object into an untyped string-to-object map.
	 *
	 * @param jsonString never-null JSON object text
	 * @return never-null map; may be empty
	 * @throws JsonParseException   when the text is not valid JSON
	 * @throws JsonMappingException when the JSON cannot be mapped to a map
	 * @throws IOException          when Jackson I/O fails
	 */
	public static Map<String, Object> jsonStringToJavaMap(String jsonString)
			throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> javaMap = null;
		try {
			javaMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {
			});
		} catch (com.fasterxml.jackson.core.JsonParseException e) {
			throw new JsonParseException(JsonUtilConstants.MOSIP_JSON_PARSE_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_JSON_PARSE_ERROR_CODE.getErrorMessage(), e.getCause());
		} catch (com.fasterxml.jackson.databind.JsonMappingException e) {
			throw new JsonMappingException(JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_JSON_MAPPING_ERROR_CODE.getErrorMessage(), e.getCause());
		} catch (java.io.IOException e) {
			throw new IOException(JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorCode(),
					JsonUtilConstants.MOSIP_IO_EXCEPTION_ERROR_CODE.getErrorMessage(), e.getCause());
		}
		return javaMap;
	}

}