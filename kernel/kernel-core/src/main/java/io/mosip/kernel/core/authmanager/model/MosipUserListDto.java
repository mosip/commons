package io.mosip.kernel.core.authmanager.model;

import java.util.List;

/**
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
public class MosipUserListDto {
	List<MosipUserDto> mosipUserDtoList;

	public List<MosipUserDto> getMosipUserDtoList() {
		return mosipUserDtoList;
	}

	public void setMosipUserDtoList(List<MosipUserDto> mosipUserDtoList) {
		this.mosipUserDtoList = mosipUserDtoList;
	}
}
