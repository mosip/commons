package io.mosip.commons.packetmanager.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.commons-packet")
@Getter
@Setter
public class AuthorizedRolesDto {

//PacketReaderController

    private List<String> postsearchfield;
    
    private List<String> postsearchfields;

    private List<String> postdocument;
	
	private List<String> postbiometrics;
	
	private List<String> postmetainfo;
	
	private List<String> postaudits;
	
	private List<String> postvalidatepacket;
	
	private List<String> postgettags;
	
	private List<String> postinfo;

	private List<String> postcreatepacket;

	private List<String> postaddtag;

	private List<String> postaddorupdatetag;

	private List<String> postdeletetag;

}