package io.mosip.kernel.packetmanager.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditDto {
	
	private String uuid;
	private LocalDateTime createdAt;
	private String eventId;
	private String eventName;
	private String eventType;	
	private String hostName;
	private String hostIp;
	private String applicationId;
	private String applicationName;
	private String sessionUserId;
	private String sessionUserName;
	private String id;
	private String idType;
	private String createdBy;
	private String moduleName;
	private String moduleId;
	private String description;
	private LocalDateTime actionTimeStamp;
	
}
