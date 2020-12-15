package com.mw.plugins.jdbc;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;

import com.mw.base.BaseConsts;
import com.mw.base.exception.DaoException;
import com.mw.base.model.BaseListResult;

public class JdbcDao {
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	public JdbcDao(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}
	
	public <T> int updOne(String sql, T t) {
		SqlParameterSource param = new BeanPropertySqlParameterSource(t);
		int result = namedParameterJdbcTemplate.update(sql, param);
		if (result != 1) {
			logger.error("JdbcDao updOne t->sql={}, params={}, result={}", sql, t, result);
			throw new DaoException(BaseConsts.ERROR_CODE.DB_NORECORD_UPDATED, "");
		}
		return result;
	}

	public int updOne(Finder finder) {
		int result = namedParameterJdbcTemplate.update(finder.getSql(), finder.getParams());
		if (result != 1) {
			logger.error("JdbcDao updOne finder->sql={}, params={}, result={}", finder.getSql(), finder.getParams(), result);
			throw new DaoException(BaseConsts.ERROR_CODE.DB_NORECORD_UPDATED, "");
		}
		return result;
	}

	public int upd(Finder finder) {
		int result = namedParameterJdbcTemplate.update(finder.getSql(), finder.getParams());
		// if (result == 0) {
		// logger.error("JdbcDao upd finder->sql={}, params={}, result={}",
		// finder.getSql(), finder.getParams(), result);
		// throw new DaoException(BaseConsts.ERROR_CODE.DB_NORECORD_UPDATED, "");
		// }
		return result;
	}

	public <T> int[] batchUpd(String sql, Object[][] params) {
		SqlParameterSource[] batchParams = SqlParameterSourceUtils.createBatch(params);
		return namedParameterJdbcTemplate.batchUpdate(sql, batchParams);
	}

	public Integer getInt(Finder finder) {
		return getInt(finder.getQuerySql(), finder.getParams());
	}

	public Integer getInt(String sql) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, new HashMap<String, Object>(), Integer.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public Integer getInt(String sql, Map<String, Object> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public Long getLong(Finder finder) {
		return getLong(finder.getQuerySql(), finder.getParams());
	}

	public Long getLong(String sql, Map<String, Object> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public Float getFloat(Finder finder) {
		return getFloat(finder.getQuerySql(), finder.getParams());
	}

	public Float getFloat(String sql, Map<String, Object> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, Float.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public Double getDouble(Finder finder) {
		return getDouble(finder.getQuerySql(), finder.getParams());
	}

	public Double getDouble(String sql, Map<String, Object> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, Double.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public String getString(Finder finder) {
		return getString(finder.getQuerySql(), finder.getParams());
	}

	public String getString(String sql, Map<String, Object> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public Date getDate(Finder finder) {
		return getDate(finder.getQuerySql(), finder.getParams());
	}

	public Date getDate(String sql, Map<String, Object> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, Date.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public byte[] getBlob(Finder finder) {
		return getBlob(finder.getQuerySql(), finder.getParams());
	}

	public byte[] getBlob(String sql, Map<String, Object> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, byte[].class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public String getClob(Finder finder) {
		return getClob(finder.getQuerySql(), finder.getParams());
	}

	public String getClob(String sql, Map<String, Object> params) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public <T> T getOne(Finder finder, Class<T> mappedClass) {
		return getOne(finder.getQuerySql(), finder.getParams(), mappedClass);
	}

	public <T> T getOne(Finder finder, RowMapper<T> rowMapper) {
		return getOne(finder.getQuerySql(), finder.getParams(), rowMapper);
	}

	public <T> T getOne(String sql, Map<String, Object> params, Class<T> mappedClass) {
		return getOne(sql, params, new BeanPropertyRowMapper<T>(mappedClass));
	}

	public <T> T getOne(String sql, Map<String, Object> params, RowMapper<T> rowMapper) {
		try {
			return namedParameterJdbcTemplate.queryForObject(sql, params, rowMapper);
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public <T> T getFirstOne(Finder finder, Class<T> mappedClass) {
		List<T> list = getList(finder, mappedClass);
		if (list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	public <T> T getLastOne(Finder finder, Class<T> mappedClass) {
		List<T> list = getList(finder, mappedClass);
		if (list.size() == 0) {
			return null;
		} else {
			return list.get(list.size() - 1);
		}
	}

	public Map<String, Object> getOne(Finder finder) {
		try {
			return namedParameterJdbcTemplate.queryForMap(finder.getQuerySql(), finder.getParams());
		} catch (IncorrectResultSizeDataAccessException e) {
			return null;
		}
	}

	public Map<String, Object> getOne(String sql, Map<String, Object> params) {
		return namedParameterJdbcTemplate.queryForMap(sql, params);
	}

	public <T> List<T> getSingleColumnList(Finder finder, Class<T> mappedClass) {
		return getSingleColumnList(finder.getSql(), finder.getParams(), mappedClass);
	}

	public <T> List<T> getSingleColumnList(String sql, Map<String, Object> params, Class<T> mappedClass) {
		return namedParameterJdbcTemplate.query(sql, params, new SingleColumnRowMapper<T>(mappedClass));
	}

	public <T> List<T> getList(Finder finder, RowMapper<T> rowMapper) {
		return getList(finder.getQuerySql(), finder.getParams(), rowMapper);
	}

	public <T> List<T> getList(String sql, Map<String, Object> params, RowMapper<T> rowMapper) {
		return namedParameterJdbcTemplate.query(sql, params, rowMapper);
	}

	public <T> List<T> getList(Finder finder, Class<T> mappedClass) {
		return getList(finder, new BeanPropertyRowMapper<T>(mappedClass));
	}

	public <T> List<T> getList(String sql, Map<String, Object> params, Class<T> mappedClass) {
		return getList(sql, params, new BeanPropertyRowMapper<T>(mappedClass));
	}

	public List<Map<String, Object>> getList(Finder finder) {
		return getList(finder.getQuerySql(), finder.getParams());
	}

	public List<Map<String, Object>> getList(String sql, Map<String, Object> params) {
		return namedParameterJdbcTemplate.queryForList(sql, params);
	}

	public <T> BaseListResult<T> getPage(Finder finder, RowMapper<T> rowMapper) {
		List<T> list = getList(finder.getQuerySql(), finder.getParams(), rowMapper);
		long total = getLong(finder.getCountSql(), finder.getParams());

		return new BaseListResult<T>(list, total);
	}

	public <T> BaseListResult<T> getPage(Finder finder, Class<T> mappedClass) {
		return getPage(finder, new BeanPropertyRowMapper<T>(mappedClass));
	}

	public <T> T getOne(Finder finder, ResultSetExtractor<T> resultSetExtractor) {
		return namedParameterJdbcTemplate.query(finder.getQuerySql(), finder.getParams(), resultSetExtractor);
	}

	public <T> List<T> getList(Finder finder, ResultSetExtractor<List<T>> resultSetExtractor) {
		return namedParameterJdbcTemplate.query(finder.getQuerySql(), finder.getParams(), resultSetExtractor);
	}

	private final static Logger logger = LoggerFactory.getLogger(JdbcDao.class);
	
	public int update(String sql,SqlParameterSource paramSource) {
		int result = namedParameterJdbcTemplate.update(sql, paramSource);
		if (result != 1) {
			logger.error("JdbcDao update finder->sql={}, params={}, result={}", sql,paramSource, result);
			throw new DaoException(BaseConsts.ERROR_CODE.DB_NORECORD_UPDATED, "");
		}
		return result;
	}
}
