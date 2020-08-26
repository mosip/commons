/**
 *
 */
package io.mosip.commons.packet.util;

import io.mosip.commons.packet.constants.PacketManagerConstants;
import io.mosip.commons.packet.exception.ApiNotAccessibleException;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.mosip.commons.packet.constants.PacketManagerConstants.FIELDCATEGORY;
import static io.mosip.commons.packet.constants.PacketManagerConstants.IDENTITY;
import static io.mosip.commons.packet.constants.PacketManagerConstants.PROPERTIES;
import static io.mosip.commons.packet.constants.PacketManagerConstants.RESPONSE;
import static io.mosip.commons.packet.constants.PacketManagerConstants.SCHEMA_JSON;

/**
 * The Class IdSchemaUtils.
 */

/**
 * Instantiates a new id schema utils.
 */
@Component
public class IdSchemaUtils {

    private Map<Double, String> idschema = null;

    @Value("${packet.default.source:REGISTRATION_CLIENT}")
    private String defaultSource;

    @Value("${schema.default.fieldCategory:pvt,none}")
    private String defaultFieldCategory;

    @Value("${IDSCHEMAURL:null}")
    private String idschemaUrl;

    @Autowired
    private RestTemplate restTemplate;


    /**
     * Gets the source field category from id schema
     *
     * @param fieldName       the field name in schema
     * @param idschemaVersion : the idschema version used to create packet
     * @return the source
     * @throws IOException
     */
    public String getSource(String fieldName, Double idschemaVersion) throws IOException, ApiNotAccessibleException {
        String idSchema = getIdSchema(idschemaVersion);
        JSONObject properties = getJSONObjFromStr(idSchema, PROPERTIES);
        JSONObject identity = getJSONObj(properties, IDENTITY);
        JSONObject property = getJSONObj(identity, PROPERTIES);
        JSONObject value = getJSONObj(property, fieldName);
        String fieldCategory = getFieldCategory(value);
        return fieldCategory;
    }

    /**
     * Get the id schema from syncdata service
     *
     * @return idschema as string
     * @throws ApiNotAccessibleException
     * @throws IOException
     */
    public String getIdSchema(Double version) throws ApiNotAccessibleException, IOException {
        if (idschema != null && !idschema.isEmpty() && idschema.get(version) != null)
            return idschema.get(version);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(idschemaUrl);
        if (version != null)
            builder.queryParam(PacketManagerConstants.SCHEMA_VERSION_QUERY_PARAM, version);
        UriComponents uriComponents = builder.build(false).encode();

        String response = restTemplate.getForObject(uriComponents.toUri(), String.class);
        String responseString = null;
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject respObj = (JSONObject) jsonObject.get(RESPONSE);
            responseString = respObj != null ? (String) respObj.get(SCHEMA_JSON) : null;
        } catch (JSONException e) {
            throw new IOException(e);
        }

        if (responseString != null) {
            if (idschema == null) {
                idschema = new HashMap<>();
                idschema.put(version, responseString);
            } else
                idschema.put(version, responseString);
        } else
            throw new ApiNotAccessibleException("Could not get id schema");

        return idschema.get(version);
    }

    /**
     * Gets the field category.
     *
     * @param jsonObject the json object
     * @return the field category
     */
    private String getFieldCategory(JSONObject jsonObject) {
        String fieldCategory = null;
        try {
            fieldCategory = jsonObject != null ? jsonObject.getString(FIELDCATEGORY) : null;
        } catch (JSONException e) {
            fieldCategory = null;
        }
        String[] defaultCategories = defaultFieldCategory != null ? defaultFieldCategory.split(",") : null;
        if (fieldCategory != null && defaultCategories != null
                && ArrayUtils.contains(defaultCategories, fieldCategory)) {
            fieldCategory = defaultSource;
        }
        return fieldCategory;
    }

    /**
     * Search a field in json
     *
     * @param jsonObject
     * @param id
     * @return
     */
    private JSONObject getJSONObj(JSONObject jsonObject, String id) {
        try {
            return (jsonObject == null) ? null : (JSONObject) jsonObject.get(id);
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Search a field in json string
     *
     * @param jsonString
     * @param id
     * @return
     */
    private JSONObject getJSONObjFromStr(String jsonString, String id) {
        try {
            return (jsonString == null) ? null : (JSONObject) new JSONObject(jsonString).get(id);
        } catch (JSONException e) {
            return null;
        }
    }
}
