/**
 * 
 */
package io.mosip.kernel.uingenerator.service;

import io.mosip.kernel.uingenerator.dto.UinResponseDto;
import io.mosip.kernel.uingenerator.dto.UinStatusUpdateReponseDto;
import io.mosip.kernel.uingenerator.entity.UinEntity;
import io.mosip.kernel.uingenerator.exception.UinNotFoundException;
import io.mosip.kernel.uingenerator.exception.UinNotIssuedException;
import io.mosip.kernel.uingenerator.exception.UinStatusNotFoundException;
import io.vertx.ext.web.RoutingContext;

/**
 * Issues unused UINs from {@code kernel.uin} and updates lifecycle status.
 *
 * @author Dharmesh Khandelwal
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
public interface UinService {

	/**
	 * Marks one {@code UNUSED} UIN as {@code ISSUED} and returns it.
	 *
	 * @param routingContext Vert.x routing context of the fetch request
	 * @return issued UIN
	 * @throws UinNotFoundException when no unused UIN remains
	 */

	UinResponseDto getUin(RoutingContext routingContext);

	/**
	 * Updates UIN status from {@code ISSUED} to {@code ASSIGNED} or {@code UNUSED}.
	 *
	 * @param uin            entity carrying the UIN and target status
	 * @param routingContext Vert.x routing context of the update request
	 * @return updated UIN and status
	 * @throws UinNotFoundException        when the UIN is unknown
	 * @throws UinNotIssuedException       when the UIN is not {@code ISSUED}
	 * @throws UinStatusNotFoundException  when the target status is invalid
	 */
	UinStatusUpdateReponseDto updateUinStatus(UinEntity uin, RoutingContext routingContext);

	/**
	 * Moves {@code ASSIGNED} rows from {@code kernel.uin} to {@code kernel.uin_assigned}.
	 */
	void transferUin();

	/**
	 * Returns whether {@code uin} exists in the unused pool.
	 *
	 * @param uin identifier to look up
	 * @return {@code true} when present
	 */
	boolean uinExist(String uin);
	
}
