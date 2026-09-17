package io.mosip.kernel.vidgenerator.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.mosip.kernel.vidgenerator.service.VidService;

/**
 * This class have functionality to persists the list of vids in database
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
@Component
public class VidWriter {

	@Autowired
	private VidService vidService;

	/**
	 * Inserts {@code vid} when it is not already present.
	 *
	 * @param vid entity to persist
	 * @return {@code true} when the row was inserted
	 */
	public boolean persistVids(VidEntity vid) {
		return this.vidService.saveVID(vid);
	}
}