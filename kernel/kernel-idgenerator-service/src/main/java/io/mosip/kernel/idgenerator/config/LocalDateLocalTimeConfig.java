package io.mosip.kernel.idgenerator.config;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;


/**
 * Jackson configuration that serializes {@link LocalDate}, {@link LocalTime}, and {@link LocalDateTime}
 * with MOSIP date/time patterns.
 *
 * @author Sagar Mahapatra
 * @author Urvil Joshi
 *
 */
@Configuration
public class LocalDateLocalTimeConfig {
	/**
	 * Time pattern {@code HH:mm:ss}.
	 */
	public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
	/**
	 * Date pattern {@code yyyy-MM-dd}.
	 */
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	/**
	 * UTC date-time pattern {@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}.
	 */
	public static final DateTimeFormatter UTC_DATE_TIME_FORMAT = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	/**
	 * Primary {@link ObjectMapper} with Afterburner and Java Time modules registered.
	 *
	 * @return mapper used by Vert.x routers and health handlers
	 */
	@Bean
	@Primary
	public ObjectMapper serializingObjectMapper() {
		ObjectMapper objectMapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
		JavaTimeModule javaTimeModule = new JavaTimeModule();
		javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer());
		javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer());
		javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer());
		javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
		javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
		javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
		objectMapper.registerModule(javaTimeModule);
		return objectMapper;
	}

	/**
	 * Deserializes {@link LocalTime} from {@link #TIME_FORMAT}.
	 */
	public static class LocalTimeDeserializer extends JsonDeserializer<LocalTime> {
		/**
		 * Parses the JSON string as {@link LocalTime}.
		 *
		 * @param jsonParser JSON parser positioned at the value
		 * @param ctxt       Jackson deserialization context
		 * @return parsed local time
		 * @throws IOException when the parser cannot read the value
		 */
		@Override
		public LocalTime deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
			return LocalTime.parse(jsonParser.getValueAsString(), TIME_FORMAT);
		}

	}

	/**
	 * Serializes {@link LocalTime} with {@link #TIME_FORMAT}.
	 */
	public static class LocalTimeSerializer extends JsonSerializer<LocalTime> {
		/**
		 * Writes {@code localTime} as a formatted string.
		 *
		 * @param localTime           value to write
		 * @param jsonGenerator       JSON generator
		 * @param serializerProvider  Jackson serializer provider
		 * @throws IOException when writing fails
		 */
		@Override
		public void serialize(LocalTime localTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException {
			jsonGenerator.writeString(localTime.format(TIME_FORMAT));
		}
	}

	/**
	 * Deserializes {@link LocalDate} from {@link #DATE_FORMAT}.
	 */
	public static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
		/**
		 * Parses the JSON string as {@link LocalDate}.
		 *
		 * @param jsonParser JSON parser positioned at the value
		 * @param ctxt       Jackson deserialization context
		 * @return parsed local date
		 * @throws IOException when the parser cannot read the value
		 */
		@Override
		public LocalDate deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
			return LocalDate.parse(jsonParser.getValueAsString(), DATE_FORMAT);
		}

	}

	/**
	 * Serializes {@link LocalDate} with {@link #DATE_FORMAT}.
	 */
	public static class LocalDateSerializer extends JsonSerializer<LocalDate> {
		/**
		 * Writes {@code localDate} as a formatted string.
		 *
		 * @param localDate           value to write
		 * @param jsonGenerator       JSON generator
		 * @param serializerProvider  Jackson serializer provider
		 * @throws IOException when writing fails
		 */
		@Override
		public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
				throws IOException {
			jsonGenerator.writeString(localDate.format(DATE_FORMAT));
		}
	}

	/**
	 * Serializes {@link LocalDateTime} with {@link #UTC_DATE_TIME_FORMAT}.
	 */
	public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
		/**
		 * Writes {@code localDateTime} as a UTC formatted string.
		 *
		 * @param localDateTime       value to write
		 * @param jsonGenerator       JSON generator
		 * @param serializerProvider  Jackson serializer provider
		 * @throws IOException when writing fails
		 */
		@Override
		public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator,
				SerializerProvider serializerProvider) throws IOException {
			jsonGenerator.writeString(localDateTime.format(UTC_DATE_TIME_FORMAT));
		}
	}

	/**
	 * Deserializes {@link LocalDateTime} from {@link #UTC_DATE_TIME_FORMAT}.
	 */
	public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
		/**
		 * Parses the JSON string as {@link LocalDateTime}.
		 *
		 * @param jsonParser JSON parser positioned at the value
		 * @param ctxt       Jackson deserialization context
		 * @return parsed local date-time
		 * @throws IOException when the parser cannot read the value
		 */
		@Override
		public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
			return LocalDateTime.parse(jsonParser.getValueAsString(), UTC_DATE_TIME_FORMAT);
		}
	}
}
