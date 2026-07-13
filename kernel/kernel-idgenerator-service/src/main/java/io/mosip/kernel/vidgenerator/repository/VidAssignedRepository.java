package io.mosip.kernel.vidgenerator.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.mosip.kernel.vidgenerator.entity.VidAssignedEntity;

public interface VidAssignedRepository extends JpaRepository<VidAssignedEntity, String> {

	List<VidAssignedEntity> findByStatusAndIsDeletedFalse(String status);

	@Query("SELECT v.vid FROM VidAssignedEntity v")
	Page<String> findAllVids(Pageable pageable);

}