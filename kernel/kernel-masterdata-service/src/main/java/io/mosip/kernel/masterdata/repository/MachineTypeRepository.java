package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.MachineType;

/**
 * Repository to perform CRUD operations on MachineType.
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@Repository
public interface MachineTypeRepository extends BaseRepository<MachineType, String> {

	@Query("FROM MachineType m where m.code = ?1 and m.langCode =?2 and (m.isDeleted = true) AND m.isActive = true")
	MachineType findMachineTypeByIdAndByLangCodeIsDeletedtrue(String code, String langCode);
	
	@Query("FROM MachineType m where m.code = ?1 and m.langCode =?2 and (isDeleted is null OR isDeleted = false) AND m.isActive = true")
	MachineType findMachineTypeByCodeAndByLangCode(String code, String langCode);
	
	@Query("FROM MachineType m where m.code = ?1 and m.langCode =?2 and (isDeleted is null OR isDeleted = false)")
	MachineType findtoUpdateMachineTypeByCodeAndByLangCode(String code, String langCode);

	@Query("FROM MachineType m where (isDeleted is null OR isDeleted = false) AND isActive = true")
	List<MachineType> findAllMachineTypeByIsActiveAndIsDeletedFalseOrNull();

}
