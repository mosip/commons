package io.mosip.kernel.keymanagerservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.keymanagerservice.entity.DataEncryptKeystore;

/**
 * The Interface DataEncryptKeystoreRepository.
 *
 * @author Mahammed Taheer
 */
@Repository
public interface DataEncryptKeystoreRepository extends JpaRepository<DataEncryptKeystore, String>{

	/**
	 * Find key by id.
	 *
	 * @param id the id
	 * @return the string
	 */
	@Query("SELECT d.key from DataEncryptKeystore d where d.id = :id")
	String findKeyById(@Param("id") Integer id);

	/**
	 * Gets the ids by key status.
	 *
	 * @param status the status
	 * @return the ids by key status
	 */
	@Query("SELECT d.id from DataEncryptKeystore d where d.keyStatus = :status")
	List<Integer> getIdsByKeyStatus(@Param("status") String status);

	/**
	 * Get the max id.
	 * 
	 * @return the max id
	 */
	@Query("SELECT MAX(d.id) from DataEncryptKeystore d")
	Long findMaxId();
}