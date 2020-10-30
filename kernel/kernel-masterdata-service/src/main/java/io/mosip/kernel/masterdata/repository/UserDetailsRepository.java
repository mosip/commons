package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.UserDetails;

@Repository
public interface UserDetailsRepository extends BaseRepository<UserDetails, String> {
	
	@Query(value = "select count(*) from  master.user_detail where regcntr_id=?1 and (is_deleted is null or is_deleted=false);", nativeQuery = true)
	public Long countCenterUsers(String centerId);
	
	@Query("FROM UserDetails WHERE regCenterId=?1 and (isDeleted is null or isDeleted =false) and isActive = true")
	public List<UserDetails> findByRegIdAndIsDeletedFalseOrIsDeletedIsNull(String id);
	

}
