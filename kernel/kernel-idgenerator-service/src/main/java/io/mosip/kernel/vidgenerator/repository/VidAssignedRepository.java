package io.mosip.kernel.vidgenerator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.mosip.kernel.vidgenerator.entity.VidAssignedEntity;

public interface VidAssignedRepository extends JpaRepository<VidAssignedEntity, String> {

	@Query(value = "select v.vid, v.cr_by, v.cr_dtimes, v.del_dtimes, v.is_deleted, v.upd_by, v.upd_dtimes, v.vid_status from kernel.vid_assigned v where v.vid_status=:status limit :limit ", nativeQuery = true)
	List<VidAssignedEntity> findByStatusAndIsDeletedFalse(@Param("status") String status, @Param("limit") int limit);

}