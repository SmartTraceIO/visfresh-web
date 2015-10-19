/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.CompanyDao;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CompanyDaoImpl extends DaoImplBase<Company, Long> implements CompanyDao {
    /**
     * Description field.
     */
    private static final String DESCRIPTION_FIELD = "description";
    /**
     * Name field.
     */
    private static final String NAME_FIELD = "name";
    /**
     * ID field.
     */
    public static final String ID_FIELD = "id";
    /**
     * Table name.
     */
    public static final String TABLE = "companies";

    /**
     * Default constructor.
     */
    public CompanyDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Company> S save(final S entity) {
        final String namePlaceHolder = "name";
        final String descriptionPlaceHolder = "description";
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (entity.getId() == null) {
            //insert
            paramMap.put("id", entity.getId());
            sql = "insert into " + TABLE + " (" + combine(NAME_FIELD, DESCRIPTION_FIELD) + ")"
                    + " values(:" + namePlaceHolder + ", :" + descriptionPlaceHolder + ")";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + NAME_FIELD + "=:" + namePlaceHolder + ","
                + DESCRIPTION_FIELD + "=:" + descriptionPlaceHolder
                + " where id = :" + ID_FIELD
                ;
        }

        paramMap.put(namePlaceHolder, entity.getName());
        paramMap.put(descriptionPlaceHolder, entity.getDescription());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            entity.setId(keyHolder.getKey().longValue());
        }

        return entity;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findOne(java.io.Serializable)
     */
    @Override
    public Company findOne(final Long id) {
        if (id == null) {
            return null;
        }

        final String entityName = "c";
        final String resultPrefix = "c_";

        final List<Map<String, Object>> list = runSelect(id, entityName, resultPrefix);
        return list.size() == 0 ? null : createCompany(resultPrefix, list.get(0));
    }

    /**
     * @param id
     * @param entityName
     * @param resultPrefix
     * @return
     */
    private List<Map<String, Object>> runSelect(final Long id,
            final String entityName, final String resultPrefix) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_FIELD, id);

        final Map<String, String> fields = createSelectAsMapping(entityName, resultPrefix);
        params.putAll(fields);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select "
                + buildSelectAs(fields)
                + " from " + TABLE + " " + entityName
                + (id == null ? "": " where " + entityName + "." + ID_FIELD + " = :id"),
                params);
        return list;
    }

    /**
     * @param resultPrefix
     * @return
     */
    protected static Map<String, String> createSelectAsMapping(
            final String entityName, final String resultPrefix) {
        final Map<String, String> map = new HashMap<String, String>();
        map.put(entityName + "." + ID_FIELD, resultPrefix + ID_FIELD);
        map.put(entityName + "." + NAME_FIELD, resultPrefix + NAME_FIELD);
        map.put(entityName + "." + DESCRIPTION_FIELD, resultPrefix + DESCRIPTION_FIELD);
        return map ;
    }
    /**
     * @param namePrefix field name prefix.
     * @param map result map.
     * @return company.
     */
    protected static Company createCompany(final String namePrefix, final Map<String, Object> map) {
        final Company c = new Company();
        c.setId(((Number) map.get(namePrefix + ID_FIELD)).longValue());
        c.setName((String) map.get(namePrefix + NAME_FIELD));
        c.setDescription((String) map.get(namePrefix + DESCRIPTION_FIELD));
        return c;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#findAll()
     */
    @Override
    public List<Company> findAll() {
        final String entityName = "c";
        final String resultPrefix = "c_";

        final List<Map<String, Object>> list = runSelect(null, entityName, resultPrefix);

        final List<Company> result = new LinkedList<Company>();
        for (final Map<String,Object> map : list) {
            result.add(createCompany(resultPrefix, map));
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#delete(java.io.Serializable)
     */
    @Override
    public void delete(final Long id) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", id);
        jdbc.update("delete from " + TABLE + " where " + ID_FIELD + " = :id", paramMap);
    }
}
