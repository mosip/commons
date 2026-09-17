package io.mosip.kernel.uingenerator.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.uingenerator.entity.UinEntity;
import io.mosip.kernel.uingenerator.entity.UinEntityAssigned;

/**
 * Repository for assigned UINs in {@code kernel.uin_assigned}.
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Repository
public interface UinRepositoryAssigned extends JpaRepository<UinEntityAssigned, String> {


}