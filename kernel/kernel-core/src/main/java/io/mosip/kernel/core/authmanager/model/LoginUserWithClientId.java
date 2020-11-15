package io.mosip.kernel.core.authmanager.model;


import lombok.Data;

@Data
public class LoginUserWithClientId {
    private String userName;
    private String password;
    private String appId;
    private String clientId;
    private String clientSecret;
}
