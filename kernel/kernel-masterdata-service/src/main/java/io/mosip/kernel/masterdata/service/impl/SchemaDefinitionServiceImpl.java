package io.mosip.kernel.masterdata.service.impl;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import io.mosip.kernel.masterdata.service.SchemaDefinitionService;

import java.nio.charset.StandardCharsets;


@Service
public class SchemaDefinitionServiceImpl implements SchemaDefinitionService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaDefinitionService.class);
	
	private static final String FILE_NAME = "definition.json";
	

	@Override
	public JSONObject getAllSchemaDefinitions() {
		JSONObject definition = null;
		try {
			ClassPathResource resource = new ClassPathResource(FILE_NAME);
			StringWriter writer = new StringWriter();
			IOUtils.copy(resource.getInputStream(), writer, StandardCharsets.UTF_8);
			return new JSONObject(writer.toString());
			
		} catch (IOException | JSONException e) {
			LOGGER.error("Exception while getting all schema defs", e);
		}		
		return definition;
	}

}
