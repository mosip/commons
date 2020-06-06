/**
 *
 */
package io.mosip.kernel.packetmanager.util;

import io.mosip.kernel.packetmanager.constants.IDschemaConstants;
import io.mosip.kernel.packetmanager.constants.PacketManagerConstants;
import io.mosip.kernel.packetmanager.exception.ApiNotAccessibleException;
import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class IdSchemaUtils.
 *
 * @author Sowmya
 */

/**
 * Instantiates a new id schema utils.
 */
@Component
public class IdSchemaUtils {

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private Environment env;

    private Map<Double, String> idschema = null;

    @Value("${packet.default.source}")
    private String defaultSource;

    @Value("${schema.default.fieldCategory}")
    private String defaultFieldCategory;

    /**
     * Gets the source field category from id schema
     *
     * @param id the id
     * @return the source
     * @throws IOException
     */
    public String getSource(String id, Double idschemaVersion) throws IOException, ApiNotAccessibleException {
        String idSchema = getIdSchema(idschemaVersion);
        JSONObject properties = getJSONObjFromStr(idSchema, IDschemaConstants.PROPERTIES);
        JSONObject identity = getJSONObj(properties, IDschemaConstants.IDENTITY);
        JSONObject property = getJSONObj(identity, IDschemaConstants.PROPERTIES);
        JSONObject value = getJSONObj(property, id);
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
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(env.getProperty(IDschemaConstants.IDSCHEMA_URL));
        if (version != null)
            builder.queryParam(PacketManagerConstants.SCHEMA_VERSION_QUERY_PARAM,version);
        UriComponents uriComponents = builder.build(false).encode();

        String response = restUtil.getApi(uriComponents.toUri(), String.class);
        String responseString = null;
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject respObj = (JSONObject) jsonObject.get(IDschemaConstants.RESPONSE);
            responseString = respObj != null ? (String) respObj.get(IDschemaConstants.SCHEMA_JSON) : null;
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
     * Gets the json.
     *
     * @param configServerFileStorageURL the config server file storage URL
     * @param uri                        the uri
     * @return the json
     */
    public static String getJson(String configServerFileStorageURL, String uri) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(configServerFileStorageURL + uri, String.class);
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
            fieldCategory = jsonObject != null ? jsonObject.getString(IDschemaConstants.FIELDCATEGORY) : null;
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
