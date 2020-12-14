package io.mosip.commons.packet.audit;

import io.mosip.commons.packet.constants.LoggerFileConstant;
import io.mosip.commons.packet.dto.packet.AuditRequestDto;
import io.mosip.commons.packet.util.PacketManagerLogger;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class AuditLogEntry {

	/** The logger. */
	private final Logger LOGGER = PacketManagerLogger.getLogger(AuditLogEntry.class);

	@Autowired
	@Lazy
	private RestTemplate restTemplate;

	@Autowired
	private Environment env;

	@Value("${AUDIT_URL:null}")
	private String auditLogUrl;

	private static final String AUDIT_SERVICE_ID = "mosip.commons.packet.manager";
	private static final String APPLICATION_VERSION = "v1";
	private static final String DATETIME_PATTERN = "mosip.utc-datetime-pattern";

	@SuppressWarnings("unchecked")
	public String addAudit(String description, String eventId,
			String eventName, String eventType, String moduleId, String moduleName, String id) {
		LOGGER.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
				id, "AuditLogEntry:: addAudit::entry");

		AuditRequestDto auditRequestDto;
		RequestWrapper<AuditRequestDto> requestWrapper = new RequestWrapper<>();
		ResponseEntity<String> responseWrapper = null;

		try {

			auditRequestDto = new AuditRequestDto();
			auditRequestDto.setDescription(description);
			auditRequestDto.setActionTimeStamp(DateUtils.getUTCCurrentDateTimeString());
			auditRequestDto.setApplicationId(LoggerFileConstant.MOSIP_4.toString());
			auditRequestDto.setApplicationName(LoggerFileConstant.PACKET_MANAGER.toString());
			auditRequestDto.setCreatedBy(LoggerFileConstant.SYSTEM.toString());
			auditRequestDto.setEventId(eventId);
			auditRequestDto.setEventName(eventName);
			auditRequestDto.setEventType(eventType);
			auditRequestDto.setHostIp(ServerUtil.getServerUtilInstance().getServerIp());
			auditRequestDto.setHostName(ServerUtil.getServerUtilInstance().getServerName());
			auditRequestDto.setId(id);
			auditRequestDto.setIdType(LoggerFileConstant.ID.toString());
			auditRequestDto.setModuleId(moduleId);
			auditRequestDto.setModuleName(moduleName);
			auditRequestDto.setSessionUserId(LoggerFileConstant.SYSTEM.toString());
			auditRequestDto.setSessionUserName(null);
			requestWrapper.setId(AUDIT_SERVICE_ID);
			requestWrapper.setMetadata(null);
			requestWrapper.setRequest(auditRequestDto);
			DateTimeFormatter format = DateTimeFormatter.ofPattern(env.getProperty(DATETIME_PATTERN));
			LocalDateTime localdatetime = LocalDateTime
					.parse(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)), format);
			requestWrapper.setRequesttime(localdatetime);
			requestWrapper.setVersion(APPLICATION_VERSION);
			HttpEntity<RequestWrapper<AuditRequestDto>> httpEntity = new HttpEntity<>(requestWrapper);
			responseWrapper = restTemplate.exchange(auditLogUrl, HttpMethod.POST, httpEntity,
					String.class);

		} catch (Exception arae) {
		    LOGGER.error(PacketManagerLogger.SESSIONID, PacketManagerLogger.REGISTRATIONID,
		    		null, ExceptionUtils.getStackTrace(arae));  
		}
		LOGGER.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.ID.toString(),
				id,
				"AuditLogRequestBuilder:: AuditLogEntry::exit");

		return responseWrapper.getBody();
	}

}
