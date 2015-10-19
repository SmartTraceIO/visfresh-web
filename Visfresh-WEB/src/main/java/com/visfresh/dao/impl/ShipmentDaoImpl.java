/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentDaoImpl extends ShipmentBaseDao<Shipment> implements ShipmentDao {
    private static final String PALETTID_FIELD = "palletid";
    private static final String PONUM_FIELD = "ponum";
    private static final String DESCRIPTIONDATE_FIELD = "descriptiondate";
    private static final String CUSTOMFIELDS_FIELD = "customfiels";
    private static final String STATUS_FIELD = "status";

    @Autowired
    private DeviceDao deviceDao;

    /**
     * Default constructor.
     */
    public ShipmentDaoImpl() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createEntity()
     */
    @Override
    protected Shipment createEntity() {
        return new Shipment();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#getShipmentData(java.util.Date, java.util.Date, java.lang.String)
     */
    @Override
    public List<ShipmentData> getShipmentData(final Date startDate, final Date endDate,
            final String onlyWithAlerts) {
        return new LinkedList<ShipmentData>();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createEntity(java.util.Map, java.util.Map)
     */
    @Override
    protected Shipment createEntity(final Map<String, Object> map,
            final Map<Long, Company> cache) {
        final Shipment e = super.createEntity(map, cache);
        e.setPalletId((String) map.get(PALETTID_FIELD));
        e.setPoNum((String) map.get(PONUM_FIELD));
        e.setShipmentDescriptionDate((Date) map.get(DESCRIPTIONDATE_FIELD));
        e.setCustomFields((String) map.get(CUSTOMFIELDS_FIELD));
        e.setStatus(ShipmentStatus.valueOf((String) map.get(STATUS_FIELD)));
        e.getDevices().addAll(findNotificationSchedules(e));
        return e;
    }
    /**
     * @param no
     * @param table
     * @return
     */
    private List<Device> findNotificationSchedules(final Shipment no) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(ID_FIELD, no.getId());
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select device from shipmentdevices where shipment =:" + ID_FIELD,
                params);

        final List<Device> result = new LinkedList<Device>();
        for (final Map<String,Object> row : list) {
            row.remove("schedule");
            final String id = ((String) row.get("id"));
            result.add(deviceDao.findOne(id));
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createParameterMap(com.visfresh.entities.ShipmentBase)
     */
    @Override
    protected Map<String, Object> createParameterMap(final Shipment s) {
        final Map<String, Object> params = super.createParameterMap(s);
        params.put(ISTEMPLATE_FIELD, false);
        params.put(PALETTID_FIELD, s.getPalletId());
        params.put(PONUM_FIELD, s.getPoNum());
        params.put(DESCRIPTIONDATE_FIELD, s.getShipmentDescriptionDate());
        params.put(CUSTOMFIELDS_FIELD, s.getCustomFields());
        params.put(STATUS_FIELD, s.getStatus().name());
        return params;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createManyToManyRefs(com.visfresh.entities.ShipmentBase)
     */
    @Override
    protected void createManyToManyRefs(final Shipment s) {
        super.createManyToManyRefs(s);
        for (final Device m : s.getDevices()) {
            final HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("shipment", s.getId());
            params.put("device", m.getId());
            jdbc.update("insert into " + "shipmentdevices" + " (shipment, device)"
                    + " values(:shipment,:device)", params);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#clearManyToManyRefs(java.lang.Long)
     */
    @Override
    protected void clearManyToManyRefs(final Long id) {
        super.clearManyToManyRefs(id);
        cleanRefs("shipmentdevices", id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#isTemplate()
     */
    @Override
    protected boolean isTemplate() {
        return false;
    }
}
