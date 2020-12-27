package io.mosip.kernel.vidgenerator.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import io.mosip.kernel.vidgenerator.entity.VidAssignedEntity;

public interface VidAssignedRepository extends JpaRepository<VidAssignedEntity, String> {

	List<VidAssignedEntity> findByStatusAndIsDeletedFalse(String status);

}