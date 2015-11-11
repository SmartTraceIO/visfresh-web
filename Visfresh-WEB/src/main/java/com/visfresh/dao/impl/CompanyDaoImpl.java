/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.constants.CompanyConstants;
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

    private static final String NAME_FIELD = "name";
    public static final String ID_FIELD = "id";
    public static final String TABLE = "companies";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public CompanyDaoImpl() {
        super();
        propertyToDbFields.put(CompanyConstants.PROPERTY_ID, ID_FIELD);
        propertyToDbFields.put(CompanyConstants.PROPERTY_NAME, NAME_FIELD);
        propertyToDbFields.put(CompanyConstants.PROPERTY_DESCRIPTION, DESCRIPTION_FIELD);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Company> S save(final S entity) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;

        if (entity.getId() == null) {
            //insert
            paramMap.put("id", entity.getId());
            sql = "insert into " + TABLE + " (" + combine(NAME_FIELD, DESCRIPTION_FIELD) + ")"
                    + " values(:name, :description)";
        } else {
            //update
            sql = "update " + TABLE + " set "
                + NAME_FIELD + "=:name,"
                + DESCRIPTION_FIELD + "=:description"
                + " where id = :" + ID_FIELD
                ;
        }

        paramMap.put("id", entity.getId());
        paramMap.put("name", entity.getName());
        paramMap.put("description", entity.getDescription());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            entity.setId(keyHolder.getKey().longValue());
        }

        return entity;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected Company createEntity(final Map<String, Object> map) {
        final Company c = new Company();
        c.setId(((Number) map.get(ID_FIELD)).longValue());
        c.setName((String) map.get(NAME_FIELD));
        c.setDescription((String) map.get(DESCRIPTION_FIELD));
        return c;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final Company t, final Map<String, Object> map,
            final Map<String, Object> cache) {
        // nothing to resolve
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbFields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE;
    }
}
