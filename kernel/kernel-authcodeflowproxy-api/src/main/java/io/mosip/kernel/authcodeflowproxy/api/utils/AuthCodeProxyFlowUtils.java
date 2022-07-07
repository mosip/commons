package io.mosip.kernel.authcodeflowproxy.api.utils;

import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

public class AuthCodeProxyFlowUtils {
	
	private AuthCodeProxyFlowUtils() {
		
	}

	static Map<String,DecodedJWT> decodedJWTCache = new HashMap<>();
	
	public static String getissuer(String token) {
		DecodedJWT decodedJWT  = null;
		if(decodedJWTCache.get(token)!=null)
			decodedJWT = decodedJWTCache.get(token);
		else{
			decodedJWT = JWT.decode(token);
			decodedJWTCache.put(token, decodedJWT);
		}	
		return decodedJWT.getClaim("iss").asString();
	}
}
