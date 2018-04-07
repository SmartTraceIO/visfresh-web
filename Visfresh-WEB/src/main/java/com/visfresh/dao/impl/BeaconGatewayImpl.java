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

import com.visfresh.dao.BeaconGatewayDao;
import com.visfresh.entities.BeaconGateway;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class BeaconGatewayImpl extends DaoImplBase<BeaconGateway, BeaconGateway, Long> implements BeaconGatewayDao {
    /**
     * Default constructor.
     */
    public BeaconGatewayImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends BeaconGateway> S save(final S g) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;
        final List<String> fields = getFields(false);

        if (g.getId() == null) {
            //insert
            sql = createInsertScript("beacongateways", fields);
        } else {
            //update
            sql = createUpdateScript("beacongateways", fields, "id");
        }

        paramMap.put("id", g.getId());
        paramMap.put("company", g.getCompany());
        paramMap.put("gateway", g.getGateway());
        paramMap.put("beacon", g.getBeacon());
        paramMap.put("active", g.isActive());
        paramMap.put("description", g.getDescription());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            g.setId(keyHolder.getKey().longValue());
        }

        return g;
    }

    public List<String> getFields(final boolean addId) {
        final LinkedList<String> fields = new LinkedList<String>();
        if (addId) {
            fields.add("id");
        }
        fields.add("company");
        fields.add("gateway");
        fields.add("beacon");
        fields.add("active");
        fields.add("description");
        return fields;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return "id";
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return new HashMap<String, String>();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return "beacongateways";
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final BeaconGateway a, final Map<String, Object> row,
            final Map<String, Object> cache) {
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected BeaconGateway createEntity(final Map<String, Object> map) {
        final BeaconGateway a = new BeaconGateway();
        a.setId(((Number) map.get("id")).longValue());
        a.setCompany(((Number) map.get("company")).longValue());
        a.setGateway((String) map.get("gateway"));
        a.setBeacon((String) map.get("beacon"));
        a.setActive(Boolean.TRUE.equals(map.get("active")));
        a.setDescription((String) map.get("description"));
        return a;
    }
}
