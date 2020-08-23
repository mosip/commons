package io.mosip.kernel.keymanagerservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.keymanagerservice.entity.KeyPolicy;

/**
 * This interface extends BaseRepository which provides with the methods for
 * several CRUD operations.
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Repository
public interface KeyPolicyRepository extends JpaRepository<KeyPolicy, String> {

	/**
	 * Function to find KeyPolicy by applicationId
	 * 
	 * @param applicationId applicationId
	 * @return KeyPolicy
	 */
	Optional<KeyPolicy> findByApplicationId(String applicationId);


	/**
	 * Function to find KeyPolicy by applicationId
	 * 
	 * @param applicationId applicationId
	 * @return KeyPolicy
	 */
	Optional<KeyPolicy> findByApplicationIdAndIsActive(String applicationId, boolean isActive);

}
