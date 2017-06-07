/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import com.visfresh.dao.Filter;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.ShipmentNoteDao;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentNote;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentNoteDaoImpl extends DaoImplBase<ShipmentNote,ShipmentNote, Long> implements ShipmentNoteDao {
    /**
     * Table name.
     */
    public static final String TABLE = "shipmentnotes";

    private static final String ID_FIELD = "id";
    private static final String SHIPMENT_FIELD = "shipment";
    private static final String USER_FIELD = "user";
    private static final String TEXT_FIELD = "text";

    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private UserDao userDao;

    /**
     * Default constructor.
     */
    public ShipmentNoteDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createCache()
     */
    @Override
    protected EntityCache<Long> createCache() {
        return new EntityCache<>("ShipmentNoteDao", 1000, 60, 3 * 60);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends ShipmentNote> S saveImpl(final S note) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;
        final List<String> fields = getFields(false);

        if (note.getId() == null) {
            //insert
            sql = createInsertScript(TABLE, fields);
        } else {
            //update
            sql = createUpdateScript(TABLE, fields, ID_FIELD);
        }

        paramMap.put(ID_FIELD, note.getId());
        paramMap.put(SHIPMENT_FIELD, note.getShipment().getId());
        paramMap.put(USER_FIELD, note.getUser().getId());
        paramMap.put(TEXT_FIELD, note.getText());

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, new MapSqlParameterSource(paramMap), keyHolder);
        if (keyHolder.getKey() != null) {
            note.setId(keyHolder.getKey().longValue());
        }

        return note;
    }

    public List<String> getFields(final boolean addId) {
        final LinkedList<String> fields = new LinkedList<String>();
        fields.add(USER_FIELD);
        fields.add(TEXT_FIELD);
        fields.add(SHIPMENT_FIELD);
        if (addId) {
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
        return new HashMap<String, String>();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return TABLE;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final ShipmentNote t, final Map<String, Object> map,
            final Map<String, Object> cache) {
        final Long userId = ((Number) map.get(USER_FIELD)).longValue();
        final Long shipmentId = ((Number) map.get(SHIPMENT_FIELD)).longValue();

        //resolve user
        final String userKey = "user_" + userId;
        User user = (User) cache.get(userKey);
        if (user == null) {
            user = userDao.findOne(userId);
            cache.put(userKey, user);
        }
        t.setUser(user);

        //resolve shipment
        final String shipmentKey = "shipment_" + shipmentId;
        Shipment s = (Shipment) cache.get(shipmentKey);
        if (s == null) {
            s = shipmentDao.findOne(shipmentId);
            cache.put(shipmentKey, s);
        }
        t.setShipment(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected ShipmentNote createEntity(final Map<String, Object> row) {
        final ShipmentNote a = new ShipmentNote();
        a.setId(((Number) row.get(ID_FIELD)).longValue());
        a.setText((String) row.get(TEXT_FIELD));
        return a;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentNoteDao#findByUserAndShipment(com.visfresh.entities.Shipment, com.visfresh.entities.User)
     */
    @Override
    public List<ShipmentNote> findByUserAndShipment(final Shipment shipment, final User user) {
        //create filter.
        final Filter f = new Filter();
        f.addFilter(SHIPMENT_FIELD, shipment.getId());
        f.addFilter(USER_FIELD, user.getId());

        //create sorting
        final Sorting sorting = new Sorting(ID_FIELD);
        return findAll(f, sorting, null);
    }
}
