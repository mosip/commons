package io.mosip.kernel.keymanagerservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.keymanagerservice.entity.KeyStore;

/**
 * This interface extends BaseRepository which provides with the methods for
 * several CRUD operations.
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Repository
public interface KeyStoreRepository extends JpaRepository<KeyStore, String> {

	/**
	 * Function to find KeyStore by alias
	 * 
	 * @param alias alias
	 * @return KeyStore
	 */
	Optional<KeyStore> findByAlias(String alias);


	/**
	 * Function to find all KeyStore objects by masterAlias
	 * 
	 * @param masterAlias master Alias
	 * @return List of KeyStore
	 */
	List<KeyStore> findByMasterAlias(String masterAlias);
}
