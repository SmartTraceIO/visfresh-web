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

import com.visfresh.constants.CriticalActionListConstants;
import com.visfresh.dao.CriticalActionListDao;
import com.visfresh.entities.CriticalActionList;
import com.visfresh.io.json.CriticalActionListSerializer;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CriticalActionListDaoImpl extends EntityWithCompanyDaoImplBase<CriticalActionList, Long>
        implements CriticalActionListDao {
    public static final String TABLE = "criticalactions";

    public static final String ID_FIELD = "id";
    public static final String NAME_FIELD = "name";

    private static final String COMPANY_FIELD = "company";
    private static final String ACTIONS = "actions";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public CriticalActionListDaoImpl() {
        super();

        //build property to field map
        propertyToDbFields.put(CriticalActionListConstants.LIST_NAME, NAME_FIELD);
        propertyToDbFields.put(CriticalActionListConstants.LIST_ID, ID_FIELD);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends CriticalActionList> S saveImpl(final S list) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        final List<String> fields = getFields(false);

        String sql;
        if (list.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        paramMap.put(ID_FIELD, list.getId());
        paramMap.put(NAME_FIELD, list.getName());
        paramMap.put(ACTIONS, toJson(list.getActions()));
        paramMap.put(COMPANY_FIELD, list.getCompany().getId());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            list.setId(keyHolder.getKey().longValue());
        }

        return list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createCache()
     */
    @Override
    protected EntityCache<Long> createCache() {
        return new EntityCache<Long>("CriticalActionListDao", 1000, 10 * 60, 20 * 60);
    }

    private List<String> getFields(final boolean includeId) {
        final List<String> fields = new LinkedList<String>();
        fields.add(NAME_FIELD);
        fields.add(ACTIONS);
        fields.add(COMPANY_FIELD);

        if (includeId) {
            fields.add(ID_FIELD);
        }
        return fields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID_FIELD;
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

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#getCompanyFieldName()
     */
    @Override
    protected String getCompanyFieldName() {
        return COMPANY_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected CriticalActionList createEntity(final Map<String, Object> map) {
        final CriticalActionList list = new CriticalActionList();

        list.setId(((Number) map.get(ID_FIELD)).longValue());
        list.setName((String) map.get(NAME_FIELD));
        list.getActions().addAll(parseActions((String) map.get(ACTIONS)));

        return list;
    }

    /**
     * @param json
     * @return
     */
    private List<String> parseActions(final String json) {
        return new CriticalActionListSerializer().parseActions(SerializerUtils.parseJson(json).getAsJsonArray());
    }
    /**
     * @param a
     * @return
     */
    private String toJson(final List<String> a) {
        return new CriticalActionListSerializer().toJson(a).toString();
    }
}
