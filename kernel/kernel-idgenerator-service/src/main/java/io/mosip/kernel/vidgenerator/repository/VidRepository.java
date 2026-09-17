package io.mosip.kernel.vidgenerator.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.mosip.kernel.vidgenerator.entity.VidEntity;

/**
 * Spring Data repository for {@code kernel.vid}.
 */
public interface VidRepository extends JpaRepository<VidEntity, String> {

	/**
	 * Locks and returns one VID with the given status for issue.
	 *
	 * @param status lifecycle status such as {@code AVAILABLE}
	 * @return matching entity, or {@code null} when none remain
	 */
	@Query(value = "select v.vid,v.vid_status,v.expiry_dtimes,v.cr_by, v.cr_dtimes, v.del_dtimes, v.is_deleted, v.upd_by, v.upd_dtimes from kernel.vid v where v.vid_status=? limit 1 FOR UPDATE", nativeQuery = true)
	VidEntity findFirstByStatus(String status);

	/**
	 * Counts non-deleted VIDs in {@code status}.
	 *
	 * @param status lifecycle status
	 * @return row count
	 */
	long countByStatusAndIsDeletedFalse(String status);

	/**
	 * Returns non-deleted VIDs in {@code status}.
	 *
	 * @param status lifecycle status
	 * @return matching entities
	 */
	List<VidEntity> findByStatusAndIsDeletedFalse(String status);

	/**
	 * Updates lifecycle status and audit columns for {@code vid}.
	 *
	 * @param status      new lifecycle status
	 * @param contextUser updated-by user
	 * @param uptimes     update timestamp
	 * @param vid         identifier to update
	 */
	@Modifying
	@Query(value = "UPDATE kernel.vid SET vid_status=:status, upd_by=:contextUser, upd_dtimes=:uptimes where vid=:vid", nativeQuery = true)
	void updateVid(@Param("status") String status, @Param("contextUser") String contextUser,
			@Param("uptimes") LocalDateTime uptimes, @Param("vid") String vid);
}
