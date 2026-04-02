package io.mosip.kernel.vidgenerator.generator;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.mosip.kernel.vidgenerator.service.VidService;

/**
 * Responsible for persisting VIDs in the database.
 * Supports both single and bulk persistence operations by delegating to {@link VidService}.
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Component
public class VidWriter {

	@Autowired
	private VidService vidService;

	/**
	 * Persist a single VID in the database.
	 *
	 * @param vid the {@link VidEntity} to be saved
	 * @return {@code true} if the VID was saved successfully, {@code false} otherwise
	 */
	public boolean persistVids(VidEntity vid) {
		return this.vidService.saveVID(vid);
	}

	/**
	 * Persist a list of VIDs in bulk.
	 * Filters out any VIDs already present in the system before insertion.
	 *
	 * @param vids list of {@link VidEntity} to be saved
	 * @return the number of VIDs successfully persisted
	 */
	public int persistVidsInBulk(List<VidEntity> vids) {
		return this.vidService.saveVIDsInBulk(vids);
	}
}