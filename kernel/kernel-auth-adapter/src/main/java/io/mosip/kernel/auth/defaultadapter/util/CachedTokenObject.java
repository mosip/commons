package io.mosip.kernel.auth.defaultadapter.util;

import lombok.Getter;

public class CachedTokenObject<T> {

	@Getter
	private T token;

	public void setToken(T token) {
		this.token = token;
	}

	public void removeToken() {
		this.token = null;
	}

}