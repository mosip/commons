package io.mosip.kernel.masterdata.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.Holiday;

/**
 * Repository class for Holiday data
 * 
 * @author Abhishek Kumar
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
@Repository
public interface HolidayRepository extends BaseRepository<Holiday, Integer> {

	/**
	 * Get all the holidays for a specific id
	 * 
	 * @param id holiday id input from user
	 * @return list of holidays for a particular id
	 */
	@Query("FROM Holiday where holidayId=?1 and (isDeleted = false or isDeleted is null) and isActive = true")
	List<Holiday> findAllById(int id);

	/**
	 * Fetch all the non deleted holidays
	 * 
	 * @return list of {@link Holiday}
	 */
	@Query("FROM Holiday WHERE (isDeleted = false or isDeleted is null) and isActive = true")
	List<Holiday> findAllNonDeletedHoliday();

	/**
	 * Get all the holidays for a specific location code
	 * 
	 * @param locationCode - location code Eg: IND
	 * @param langCode     - language code Eg:ENG
	 * @param year         - Eg:1971
	 * @return list of holidays
	 */

	@Query(value = "select id, location_code, holiday_date, holiday_name, holiday_desc, lang_code, is_active, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes from master.loc_holiday WHERE location_code = ?1 and lang_code = ?2 and extract(year from holiday_date) = ?3 and (is_deleted = false  or is_deleted is null) and is_active = true", nativeQuery = true)
	List<Holiday> findAllByLocationCodeYearAndLangCode(String locationCode, String langCode, int year);

	/**
	 * Get specific holiday by holiday id and language code
	 * 
	 * @param holidayId input from user
	 * @param langCode  input from user
	 * @return list of holidays for the particular holiday id and language code
	 */
	@Query("FROM Holiday where holidayId=?1 and langCode = ?2 and (isDeleted = false or isDeleted is null) and isActive = true")
	List<Holiday> findHolidayByIdAndHolidayIdLangCode(int holidayId, String langCode);

	/**
	 * Method to get the list of holiday by name,date and location code
	 * 
	 * @param holidayName  name of the holiday to be search
	 * @param holidayDate  date of the holiday to be search
	 * @param locationCode location code of the holiday to be search
	 * @return list of holidays
	 */
	@Query("FROM Holiday WHERE holidayName = ?1 AND holidayDate = ?2 AND locationCode = ?3 AND (isDeleted is null or isDeleted=false) AND isActive = true")
	List<Holiday> findHolidayByHolidayIdAndByIsDeletedFalseOrIsDeletedNull(String holidayName, LocalDate holidayDate,
			String locationCode);

	/**
	 * Fetch the holiday by id and location code
	 * 
	 * @param id           id of the holiday
	 * @param locationCode location code of the holiday
	 * @return {@link Holiday}
	 */
	@Query("FROM Holiday where holidayId=?1 and locationCode = ?2 and (isDeleted = false or isDeleted is null) and isActive = true")
	Holiday findHolidayByIdAndHolidayIdLocationCode(int id, String locationCode);

	/**
	 * Method to delete the holiday
	 * 
	 * @param deletedTime  input for deleted timeStamp
	 * @param holidayName  name of the holiday to be deleted
	 * @param holidayDate  date of the holiday to be deleted
	 * @param locationCode location of the holiday to be deleted
	 * @return no. of rows deleted
	 */
	@Modifying
	@Transactional
	@Query("UPDATE Holiday  SET isDeleted=true ,deletedDateTime =?1 WHERE holidayName = ?2 AND holidayDate = ?3 AND locationCode = ?4 AND (isDeleted = false OR isDeleted IS NULL)")
	int deleteHolidays(LocalDateTime deletedTime, String holidayName, LocalDate holidayDate, String locationCode);

	/**
	 * Fetch the holiday by id and location code
	 * 
	 * @param id           id of the holiday
	 * @param locationCode location code of the holiday
	 * @return {@link Holiday}
	 */
	@Query("FROM Holiday where locationCode = ?1 and (isDeleted = false or isDeleted is null) and isActive = true")
	List<Holiday> findHolidayByHolidayIdLocationCode(String locationCode);

	/**
	 * Fetch the holiday by id and location code
	 * 
	 * @param id           id of the holiday
	 * @param locationCode location code of the holiday
	 * @return {@link Holiday}
	 */
	@Query("FROM Holiday where locationCode = ?1 and langCode=?2 and (isDeleted = false or isDeleted is null) and isActive = true")
	List<Holiday> findHolidayByLocationCode(String locationCode, String langCode);

	@Query(value = "select  holiday_date from master.loc_holiday WHERE location_code = ?1 and lang_code = ?2", nativeQuery = true)
	List<LocalDate> findHolidayByLocationCode1(String locationCode, String langCode);

	@Query(value = "SELECT * FROM loc_holiday where lang_code=?2 and location_code IN (SELECT code  FROM location where hierarchy_level <=?1 and lang_code=?2)", nativeQuery = true)
	List<Holiday> findHoildayByLocationCodeAndLangCode(int level, String langCode);

}
