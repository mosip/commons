package io.mosip.kernel.auth.defaultadapter.model;

import lombok.Getter;

public class TokenHolder<T> {

	@Getter
	private T token;

	public void setToken(T token) {
		this.token = token;
	}

	public void removeToken() {
		this.token = null;
	}

}