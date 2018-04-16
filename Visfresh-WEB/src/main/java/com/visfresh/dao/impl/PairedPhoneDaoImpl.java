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

import com.visfresh.constants.PairedPhoneConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.PairedPhoneDao;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.PairedPhone;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class PairedPhoneDaoImpl extends DaoImplBase<PairedPhone, PairedPhone, Long> implements PairedPhoneDao {
    private static final String DESCRIPTION = "description";
    private static final String ACTIVE = "active";
    private static final String BEACONID = "beaconid";
    private static final String IMEI = "imei";
    private static final String COMPANY = "company";
    private static final String ID = "id";

    /**
     * Default constructor.
     */
    public PairedPhoneDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends PairedPhone> S save(final S g) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        String sql;
        final List<String> fields = getFields(false);

        if (g.getId() == null) {
            //insert
            sql = createInsertScript("pairedphones", fields);
        } else {
            //update
            sql = createUpdateScript("pairedphones", fields, ID);
        }

        paramMap.put(ID, g.getId());
        paramMap.put(COMPANY, g.getCompany());
        paramMap.put(IMEI, g.getImei());
        paramMap.put(BEACONID, g.getBeaconId());
        paramMap.put(ACTIVE, g.isActive());
        paramMap.put(DESCRIPTION, g.getDescription());

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
            fields.add(ID);
        }
        fields.add(COMPANY);
        fields.add(IMEI);
        fields.add(BEACONID);
        fields.add(ACTIVE);
        fields.add(DESCRIPTION);
        return fields;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getIdFieldName()
     */
    @Override
    protected String getIdFieldName() {
        return ID;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        final HashMap<String, String> map = new HashMap<String, String>();

        map.put(PairedPhoneConstants.ID, ID);
        map.put(PairedPhoneConstants.COMAPNY, COMPANY);
        map.put(PairedPhoneConstants.IMEI, IMEI);
        map.put(PairedPhoneConstants.BEACON_ID, BEACONID);
        map.put(PairedPhoneConstants.ACTIVE, ACTIVE);
        map.put(PairedPhoneConstants.DESCRIPTION, DESCRIPTION);

        return map;
    }
 /* (non-Javadoc)
     * @see com.visfresh.dao.PairedPhoneDao#findByCompany(java.lang.Long, com.visfresh.dao.Sorting, com.visfresh.dao.Page, com.visfresh.dao.Filter)
     */
    @Override
    public List<PairedPhone> findByCompany(final Long company, final Sorting sorting, final Page page, final Filter filter) {
        final Filter f = new Filter(filter);
        f.addFilter(COMPANY, company);
        return findAll(f, sorting, page);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.PairedPhoneDao#getEntityCount(java.lang.Long, com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final Long company, final Filter filter) {
        final Filter f = new Filter(filter);
        f.addFilter(COMPANY, company);
        return getEntityCount(f);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.PairedPhoneDao#getPairedBeacons(java.lang.String)
     */
    @Override
    public List<PairedPhone> getPairedBeacons(final String phone) {
        final Filter f = new Filter();
        f.addFilter(IMEI, phone);
        f.addFilter(ACTIVE, true);
        return findAll(f, new Sorting(BEACONID), null);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.PairedPhoneDao#findOne(java.lang.String, java.lang.String)
     */
    @Override
    public PairedPhone findOne(final String phone, final String beacon) {
        final Filter f = new Filter();
        f.addFilter(IMEI, phone);
        f.addFilter(BEACONID, beacon);

        final List<PairedPhone> pairs = findAll(f, null, null);
        if (pairs.size() > 0) {
            return pairs.get(0);
        }
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getTableName()
     */
    @Override
    protected String getTableName() {
        return "pairedphones";
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final PairedPhone a, final Map<String, Object> row,
            final Map<String, Object> cache) {
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#createEntity(java.util.Map)
     */
    @Override
    protected PairedPhone createEntity(final Map<String, Object> map) {
        final PairedPhone a = new PairedPhone();
        a.setId(((Number) map.get(ID)).longValue());
        a.setCompany(((Number) map.get(COMPANY)).longValue());
        a.setBeaconId((String) map.get(BEACONID));
        a.setImei((String) map.get(IMEI));
        a.setActive(Boolean.TRUE.equals(map.get(ACTIVE)));
        a.setDescription((String) map.get(DESCRIPTION));
        return a;
    }
}
