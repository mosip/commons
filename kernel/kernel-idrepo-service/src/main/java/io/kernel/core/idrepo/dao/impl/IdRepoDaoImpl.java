package io.kernel.core.idrepo.dao.impl;

import java.util.Date;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.kernel.core.idrepo.constant.IdRepoErrorConstants;
import io.kernel.core.idrepo.dao.IdRepoDao;
import io.kernel.core.idrepo.entity.Uin;
import io.kernel.core.idrepo.entity.UinDetail;
import io.kernel.core.idrepo.exception.IdRepoAppException;
import io.kernel.core.idrepo.shard.ShradResolver;

@Repository
public class IdRepoDaoImpl implements IdRepoDao {

    private static final String UPD_BY = "upd_by";

    private static final String CR_BY = "cr_by";

    @Autowired
    private Environment env;

    private static final String UIN_INSERT =
	    "INSERT INTO uin.uin(uin_ref_id, uin, status_code, cr_by, cr_dtimes, upd_by, upd_dtimes, "
		    + "is_deleted, del_dtimes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UIN_H_INSERT = "INSERT INTO uin.uin_h("
	    + "uin_ref_id, eff_dtimes, uin, status_code, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)"
	    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UIN_DETAIL_INSERT =
	    "INSERT INTO uin.uin_detail(uin_ref_id, uin_data, cr_by, cr_dtimes, upd_by, upd_dtimes, "
		    + "is_deleted, del_dtimes)	VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UIN_DETAIL_H_INSERT = "INSERT INTO uin.uin_detail_h("
	    + "uin_ref_id, eff_dtimes, uin_data, cr_by, cr_dtimes, upd_by, upd_dtimes, is_deleted, del_dtimes)"
	    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UIN_SELECT =
	    "SELECT uin_ref_id, trim(uin) as uin, status_code, cr_by as created_by, cr_dtimes as "
		    + "created_date_time, upd_by as updated_by, upd_dtimes as "
		    + "updated_date_time, is_deleted, del_dtimes as deleted_date_time FROM uin.uin where uin = ?";

    private static final String UIN_SELECT_REF_ID = "SELECT uin_ref_id FROM uin.uin where uin = ?";

    private static final String UIN_DETAIL_SELECT =
	    "SELECT uin_ref_id, uin_data, cr_by as created_by, cr_dtimes as created_date_time, upd_by as "
		    + "updated_by, upd_dtimes as updated_date_time, is_deleted, "
		    + "del_dtimes as deleted_date_time	FROM uin.uin_detail where uin_ref_id = ?";

    private static final String UIN_UPDATE = "UPDATE uin.uin SET status_code=? WHERE uin = ?";

    private static final String UIN_DETAILS_UPDATE = "UPDATE uin.uin_detail SET uin_data=? WHERE uin_ref_id = ?";

    @Autowired
    private ShradResolver shardResolver;

    public JdbcTemplate getJdbcTemplate(String uin) {
	JdbcTemplate jdbcTemplate = new JdbcTemplate();
	DataSource dataSource = shardResolver.getShrad(uin);
	jdbcTemplate.setDataSource(dataSource);
	return jdbcTemplate;
    }

    public Uin addIdentity(String uin, String uinRefId, byte[] identityInfo) throws IdRepoAppException {
	try {
	    JdbcTemplate jdbcTemplate = getJdbcTemplate(uin);
	    OptionalInt result = IntStream.of(
		    jdbcTemplate.update(UIN_INSERT, uinRefId, uin, env.getProperty("mosip.idrepo.status.registered"),
			    CR_BY, new Date(), UPD_BY, new Date(), false, new Date()),
		    jdbcTemplate.update(UIN_H_INSERT, uinRefId, new Date(), uin,
			    env.getProperty("mosip.idrepo.status.registered"), CR_BY, new Date(), UPD_BY, new Date(),
			    false, new Date()),
		    jdbcTemplate.update(UIN_DETAIL_INSERT, uinRefId, identityInfo, CR_BY, new Date(), UPD_BY,
			    new Date(), false, new Date()),
		    jdbcTemplate.update(UIN_DETAIL_H_INSERT, uinRefId, new Date(), identityInfo, CR_BY, new Date(),
			    UPD_BY, new Date(), false, new Date()))
		    .filter(i -> (i != 1)).findFirst();

	    if (!result.isPresent()) {
		return retrieveIdentity(uin);
	    } else {
		// FIXME rollback
		throw new IdRepoAppException(IdRepoErrorConstants.DATABASE_ACCESS_ERROR);
	    }
	} catch (DuplicateKeyException e) {
	    // FIXME rollback
	    throw new IdRepoAppException(IdRepoErrorConstants.RECORD_EXISTS, e);
	} catch (DataAccessException e) {
	    // FIXME rollback
	    throw new IdRepoAppException(IdRepoErrorConstants.DATABASE_ACCESS_ERROR, e);
	}
    }

    public Uin retrieveIdentity(String uin) throws IdRepoAppException {
	try {
	    JdbcTemplate jdbcTemplate = getJdbcTemplate(uin);

	    Uin uinObject = jdbcTemplate.queryForObject(UIN_SELECT, new Object[] { uin },
		    new BeanPropertyRowMapper<>(Uin.class));

	    uinObject.setUinDetail(jdbcTemplate.queryForObject(UIN_DETAIL_SELECT,
		    new Object[] { uinObject.getUinRefId() }, new BeanPropertyRowMapper<>(UinDetail.class)));

	    return uinObject;
	} catch (EmptyResultDataAccessException e) {
	    throw new IdRepoAppException(IdRepoErrorConstants.UNKNOWN_ERROR, e);
	}
    }

    public Uin updateIdenityInfo(String uin, byte[] identityInfo) throws IdRepoAppException {
	JdbcTemplate jdbcTemplate = getJdbcTemplate(uin);

	String uinRefId = jdbcTemplate
		.queryForObject(UIN_SELECT_REF_ID, new Object[] { uin }, new BeanPropertyRowMapper<>(Uin.class))
		.getUinRefId();

	if (Objects.nonNull(uinRefId)) {
	    OptionalInt result = IntStream
		    .of(jdbcTemplate.update(UIN_DETAILS_UPDATE, identityInfo, uinRefId),
			    jdbcTemplate.update(UIN_DETAIL_H_INSERT, uinRefId, new Date(), identityInfo, CR_BY,
				    new Date(), UPD_BY, new Date(), false, new Date()))
		    .filter(i -> (i != 1)).findFirst();

	    if (!result.isPresent()) {
		return retrieveIdentity(uin);
	    } else {
		// FIXME rollback
		throw new IdRepoAppException(IdRepoErrorConstants.UNKNOWN_ERROR);
	    }
	} else {
	    throw new IdRepoAppException(IdRepoErrorConstants.INTERNAL_SERVER_ERROR);
	}
    }

    public Uin updateUinStatus(String uin, String statusCode) throws IdRepoAppException {
	JdbcTemplate jdbcTemplate = getJdbcTemplate(uin);

	if (jdbcTemplate.update(UIN_UPDATE, statusCode, uin) == 1) {
	    return retrieveIdentity(uin);
	} else {
	    throw new IdRepoAppException(IdRepoErrorConstants.UNKNOWN_ERROR);
	}
    }

}
