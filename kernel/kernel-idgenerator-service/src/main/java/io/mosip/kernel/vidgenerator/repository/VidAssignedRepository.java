package io.mosip.kernel.vidgenerator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.kernel.vidgenerator.entity.VidAssignedEntity;

/**
 * Spring Data repository for {@code kernel.vid_assigned}.
 */
public interface VidAssignedRepository extends JpaRepository<VidAssignedEntity, String> {

	/**
	 * Returns non-deleted assigned VIDs in {@code status}.
	 *
	 * @param status lifecycle status such as {@code ASSIGNED} or {@code EXPIRED}
	 * @return matching entities
	 */
	List<VidAssignedEntity> findByStatusAndIsDeletedFalse(String status);

}
