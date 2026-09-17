package io.mosip.kernel.idgenerator.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.mosip.kernel.vidgenerator.constant.VIDGeneratorConstant;
import io.mosip.kernel.vidgenerator.exception.VidGeneratorServiceException;

/**
 * Utility class to fetch config server related URLs.
 * <p>
 * Resolution order for each key is JVM system property, then environment
 * variable, then {@code bootstrap.properties}.
 * </p>
 *
 * @author Sagar Mahapatra
 * @author Raj Jha
 * @since 1.0.0
 *
 */
public class ConfigUrlsBuilder {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ConfigUrlsBuilder() {

	}

	/**
	 * Static field value for the property file name.
	 */
	private static String propertyFileName = "bootstrap.properties";

	/**
	 * Builds Spring Cloud Config URLs for each application name in {@code spring.cloud.config.name}.
	 *
	 * @return config-server URLs of the form {@code uri/name/profile/label}
	 */
	public static List<String> getURLs() {
		List<String> urlS = new ArrayList<>();
		getConfigNames().forEach(config -> {
			String url = getProperty(VIDGeneratorConstant.SPRING_CLOUD_CONFIG_URI);
			url = url + VIDGeneratorConstant.FORWARD_SLASH + config + VIDGeneratorConstant.FORWARD_SLASH
					+ getProperty(VIDGeneratorConstant.SPRING_PROFILES_ACTIVE) + VIDGeneratorConstant.FORWARD_SLASH
					+ getProperty(VIDGeneratorConstant.SPRING_CLOUD_CONFIG_LABEL);
			urlS.add(url);
		});
		return urlS;
	}

	/**
	 * Resolves {@code key} from a JVM property, environment variable, then {@code bootstrap.properties}.
	 *
	 * @param key property name
	 * @return resolved value, which may be {@code null} if absent from the file
	 * @throws VidGeneratorServiceException when {@code bootstrap.properties} cannot be read
	 */
	private static String getProperty(String key) {
		String value = System.getProperty(key);
		if (value != null)
			return value;
		else {
			value = System.getenv(key);
			if (value != null)
				return value;
		}
		Properties prop = new Properties();
		try (InputStream input = ConfigUrlsBuilder.class.getClassLoader().getResourceAsStream(propertyFileName)) {
			if (input == null) {
				throw new VidGeneratorServiceException("", "File Not Available : " + propertyFileName);
			}
			prop.load(input);
			value = prop.getProperty(key);
		} catch (IOException ex) {
			throw new VidGeneratorServiceException("", "Failed to read properties from : " + propertyFileName);
		}
		return value;
	}

	/**
	 * Splits {@code spring.cloud.config.name} on commas into config-server application names.
	 *
	 * @return ordered application names sent to config-server
	 */
	public static List<String> getConfigNames() {
		String names = System.getProperty(VIDGeneratorConstant.SPRING_CLOUD_CONFIG_NAME);
		if (names == null) {
			names = getProperty(VIDGeneratorConstant.SPRING_CLOUD_CONFIG_NAME);
		}
		return Stream.of(names.split(VIDGeneratorConstant.COMMA)).collect(Collectors.toList());
	}
}
