/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.io.json.AbstractJsonSerializer;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentDaoImpl extends ShipmentBaseDao<Shipment> implements ShipmentDao {
    private static final String PONUM_FIELD = "ponum";
    private static final String TRIPCOUNT_FIELD = "tripcount";
    private static final String PALETTID_FIELD = "palletid";
    private static final String ASSETNUM_FIELD = "assetnum";
    private static final String SHIPMENTDATE_FIELD = "shipmentdate";
    private static final String ARRIVALDATE_FIELD = "arrivaldate";
    private static final String CUSTOMFIELDS_FIELD = "customfiels";
    private static final String STATUS_FIELD = "status";
    private static final String DEVICE_FIELD = "device";
    private static final String SIBLINGGROUP_FIELD = "siblinggroup";
    private static final String SIBLINGCOUNT_FIELD = "siblingcount";
    private static final String ASSETTYPE_FIELD = "assettype";
    private static final String LASTEVENT_FIELD = "lasteventdate";
    private static final String DEVICESHUTDOWNDATE_FIELD = "deviceshutdowndate";

    @Autowired
    private DeviceDao deviceDao;

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();

    /**
     * Default constructor.
     */
    public ShipmentDaoImpl() {
        super();
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ALERT_PROFILE_ID, ALERT_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES, NOALERTIFCOODOWN_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM,
                ARRIVALNOTIFWITHIN_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS,
                NONOTIFSIFNOALERTS_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPPED_FROM, SHIPPEDFROM_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPPED_TO, SHIPPEDTO_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES,
                SHUTDOWNTIMEOUT_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_MAX_TIMES_ALERT_FIRES,
//                );
        propertyToDbFields.put(ShipmentConstants.PROPERTY_COMMENTS_FOR_RECEIVER, COMMENTS_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPMENT_DESCRIPTION, DESCRIPTION_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ALERT_PROFILE, ALERT_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_DEVICE_IMEI, DEVICE_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_STATUS, STATUS_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_CUSTOM_FIELDS, CUSTOMFIELDS_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPMENT_DATE, SHIPMENTDATE_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ARRIVAL_DATE, ARRIVALDATE_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_PO_NUM, PONUM_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_TRIP_COUNT, TRIPCOUNT_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ASSET_NUM, ASSETNUM_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_PALLET_ID, PALETTID_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_SHIPMENT_ID, ID_FIELD);
        propertyToDbFields.put(ShipmentConstants.PROPERTY_ASSET_TYPE, ASSETTYPE_FIELD);
//        propertyToDbFields.put(ShipmentConstants.PROPERTY_ETA, )
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createEntity()
     */
    @Override
    protected Shipment createEntity() {
        return new Shipment();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findActiveShipments(com.visfresh.entities.Company)
     */
    @Override
    public List<Shipment> findActiveShipments(final Company company) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("company", company.getId());
        params.put("st1", ShipmentStatus.Ended.name());
        params.put("st2", ShipmentStatus.Arrived.name());

        final String sql = "select * from " + TABLE + " s"
                + " where"
                + " s." + STATUS_FIELD + "<> :st1"
                + " and s." + STATUS_FIELD + "<> :st2"
                + " and s." + ISTEMPLATE_FIELD + " = false"
                + " and s.company = :company"
                + " order by s." + ID_FIELD + " desc";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);

        final Map<String, Object> cache = new HashMap<String, Object>();
        final List<Shipment> result = new LinkedList<Shipment>();

        for (final Map<String, Object> row : rows) {
            final Shipment s = createEntity(row);
            result.add(s);
            resolveReferences(s, row, cache);
        }

        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findNextShipmentFor(com.visfresh.entities.Shipment)
     */
    @Override
    public Shipment findNextShipmentFor(final Shipment s) {
        //first of all find the last tracker event for given shipment
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("shipment", s.getId());
        params.put("device", s.getDevice().getImei());

        final String sql = "select id from " + TABLE + " s"
                + " where"
                + " s." + ISTEMPLATE_FIELD + " = false"
                + " and s." + ID_FIELD + "> :shipment"
                + " and s." + DEVICE_FIELD + "= :device"
                + " order by s." + ID_FIELD + " limit 1";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        if (!rows.isEmpty()) {
            final Long nextShipmentId = ((Number) rows.get(0).get("id")).longValue();
            return findOne(nextShipmentId);
        }

        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findActiveShipment(java.lang.String)
     */
    @Override
    public Shipment findLastShipment(final String imei) {
        //sorting
        final Sorting sort = new Sorting(false, ShipmentConstants.PROPERTY_SHIPMENT_ID);
        //filter
        final Filter filter = new Filter();
        filter.addFilter(ShipmentConstants.PROPERTY_DEVICE_IMEI, imei);
        //page
        final List<Shipment> shipments = findAll(filter, sort, new Page(1, 1));

        if (shipments.size() > 0) {
            return shipments.get(0);
        } else {
            return null;
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#getSiblingGroup(java.lang.Long)
     */
    @Override
    public List<Shipment> getSiblingGroup(final Long siblingGroup) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("siblings", siblingGroup);

        final String sql = "select * from " + TABLE + " s"
                + " where s." + ISTEMPLATE_FIELD + " = false"
                + " and s." + SIBLINGGROUP_FIELD + " = :siblings"
                + " order by s." + ID_FIELD + " desc";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);

        final Map<String, Object> cache = new HashMap<String, Object>();
        final List<Shipment> result = new LinkedList<Shipment>();

        for (final Map<String, Object> row : rows) {
            final Shipment s = createEntity(row);
            result.add(s);
            resolveReferences(s, row, cache);
        }

        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#getSiblingCount(java.lang.Long)
     */
    @Override
    public int getGroupSize(final Long siblingGroup) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("siblings", siblingGroup);

        final String sql = "select s."
                + SIBLINGCOUNT_FIELD
                + " as count from " + TABLE + " s"
                + " where"
                + " s." + SIBLINGGROUP_FIELD + " = :siblings"
                + " limit 1";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);
        if (rows.isEmpty()) {
            return 0;
        }
        return ((Number) rows.get(0).get("count")).intValue() + 1;
    }
    /**
     * @param params
     * @return
     */
    protected Map<String, List<Alert>> getDeviceAlertMap(
            final Map<String, Object> params) {
        final String resultPrefix = "result_";

        //select alerts.
        final String selectAs = buildSelectAs(AlertDaoImpl.getFields(true), "a", resultPrefix);
        final String sql = "select " + selectAs + " from " + AlertDaoImpl.TABLE + " a, "
                + DeviceDaoImpl.TABLE + " d"
                + " where a." + AlertDaoImpl.DEVICE_FIELD + " = d." + DeviceDaoImpl.IMEI_FIELD
                + " and d." + DeviceDaoImpl.COMPANY_FIELD + " = :companyId"
                + " and a." + AlertDaoImpl.DATE_FIELD + " >= :startDate and a." + AlertDaoImpl.DATE_FIELD + " <= :endDate"
                + " order by a." + AlertDaoImpl.DATE_FIELD;

        //map of device alerts. The key is device ID.
        final Map<String, List<Alert>> deviceAlerts = new HashMap<String, List<Alert>>();

        final List<Map<String, Object>> list = jdbc.queryForList(sql, params);
        for (final Map<String, Object> row : list) {
            final String deviceId = (String) row.get(resultPrefix + AlertDaoImpl.DEVICE_FIELD);
            //add alert to map
            List<Alert> alerts = deviceAlerts.get(deviceId);
            if (alerts == null) {
                alerts = new LinkedList<Alert>();
                deviceAlerts.put(deviceId, alerts);
            }

            alerts.add(AlertDaoImpl.createAlert(row));
        }

        return deviceAlerts;
    }

    /**
     * @param fields
     * @param tableAlias
     * @param resultPrefix
     * @return
     */
    private String buildSelectAs(final List<String> fields, final String tableAlias,
            final String resultPrefix) {
        final StringBuilder sb = new StringBuilder();
        for (final String f : fields) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(tableAlias + "." + f);
            sb.append(" as ");
            sb.append(resultPrefix + f);
        }
        return sb.toString();
    }

    /**
     * @param map
     * @param companyCache
     * @param deviceCache
     * @return
     */
    @Override
    protected Shipment createEntity(final Map<String, Object> map) {
        final Shipment e = super.createEntity(map);
        e.setPalletId((String) map.get(PALETTID_FIELD));
        e.setAssetNum((String) map.get(ASSETNUM_FIELD));
        e.setShipmentDate((Date) map.get(SHIPMENTDATE_FIELD));
        e.setArrivalDate((Date) map.get(ARRIVALDATE_FIELD));
        e.setLastEventDate((Date) map.get(LASTEVENT_FIELD));
        e.setDeviceShutdownTime((Date) map.get(DEVICESHUTDOWNDATE_FIELD));
        e.getCustomFields().putAll(parseJsonMap((String) map.get(CUSTOMFIELDS_FIELD)));
        final Object statusObject = map.get(STATUS_FIELD);
        if (statusObject != null) {
            e.setStatus(ShipmentStatus.valueOf((String) statusObject));
        }
        e.setDevice(deviceDao.findOne((String) map.get(DEVICE_FIELD)));
        final Number num = (Number) map.get(SIBLINGGROUP_FIELD);
        if (num != null) {
            e.setSiblingGroup(num.longValue());
        }
        e.setSiblingCount(((Number) map.get(SIBLINGCOUNT_FIELD)).intValue());
        e.setTripCount(((Number) map.get(TRIPCOUNT_FIELD)).intValue());
        e.setPoNum(((Number) map.get(PONUM_FIELD)).intValue());
        e.setAssetType((String) map.get(ASSETTYPE_FIELD));
        return e;
    }
    /**
     * @param str
     * @return
     */
    private Map<String, String> parseJsonMap(final String str) {
        return AbstractJsonSerializer.parseStringMap(SerializerUtils.parseJson(str));
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createParameterMap(com.visfresh.entities.ShipmentBase)
     */
    @Override
    protected Map<String, Object> createParameterMap(final Shipment s) {
        final Map<String, Object> params = super.createParameterMap(s);
        params.put(ISTEMPLATE_FIELD, false);
        params.put(PALETTID_FIELD, s.getPalletId());
        params.put(ASSETNUM_FIELD, s.getAssetNum());
        params.put(SHIPMENTDATE_FIELD, s.getShipmentDate());
        params.put(ARRIVALDATE_FIELD, s.getArrivalDate());
        params.put(LASTEVENT_FIELD, s.getLastEventDate());
        params.put(DEVICESHUTDOWNDATE_FIELD, s.getDeviceShutdownTime());
        params.put(CUSTOMFIELDS_FIELD, AbstractJsonSerializer.toJson(s.getCustomFields()).toString());
        params.put(STATUS_FIELD, s.getStatus().name());
        params.put(PONUM_FIELD, s.getPoNum());
        params.put(TRIPCOUNT_FIELD, s.getTripCount());
        params.put(DEVICE_FIELD, s.getDevice() == null ? null : s.getDevice().getId());
        params.put(SIBLINGGROUP_FIELD, s.getSiblingGroup());
        params.put(SIBLINGCOUNT_FIELD, s.getSiblingCount());
        params.put(ASSETTYPE_FIELD, s.getAssetType());
        return params;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#save(com.visfresh.entities.ShipmentBase)
     */
    @Override
    public <S extends Shipment> S save(final S s) {
        final boolean insert = s.getId() == null;
        if (insert) {
            s.setTripCount(s.getDevice().getTripCount() + 1);
            s.getDevice().setTripCount(s.getTripCount());
        }
        final S result = super.save(s);
        if (insert) {
            deviceDao.save(s.getDevice());
        }
        return result;
    }
    @Override
    public List<Shipment> findActiveShipments(final String imei) {
        final List<Long> ids = findActiveShipmentIds(imei);

        final List<Shipment> result = new LinkedList<>();
        for (final Long id : ids) {
            result.add(findOne(id));
        }
        return result;
    }
    /**
     * @param imei
     * @return
     */
    private List<Long> findActiveShipmentIds(final String imei) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("imei", imei);
        params.put("st1", ShipmentStatus.Ended.name());
        params.put("st2", ShipmentStatus.Arrived.name());

        final String sql = "select s." + ID_FIELD
                + " from " + TABLE + " s"
                + " where"
                + " s." + DEVICE_FIELD + "= :imei"
                + " and s." + STATUS_FIELD + "<> :st1"
                + " and s." + STATUS_FIELD + "<> :st2"
                + " order by s." + ID_FIELD + " desc";
        final List<Map<String, Object>> rows = jdbc.queryForList(sql, params);

        final List<Long> result = new LinkedList<Long>();
        for (final Map<String, Object> row : rows) {
            final Entry<String, Object> firstEntry = row.entrySet().iterator().next();
            result.add(((Number) firstEntry.getValue()).longValue());
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#isTemplate()
     */
    @Override
    protected boolean isTemplate() {
        return false;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.EntityWithCompanyDaoImplBase#resolveReferences(com.visfresh.entities.EntityWithId, java.util.Map, java.util.Map)
     */
    @Override
    protected void resolveReferences(final Shipment s, final Map<String, Object> row,
            final Map<String, Object> cache) {
        super.resolveReferences(s, row, cache);

        final String imei = (String) row.get(DEVICE_FIELD);
        final String key = "device_" + imei;
        Device device = (Device) cache.get(key);
        if (device == null) {
            device = deviceDao.findByImei(imei);
            cache.put(key, device);
        }

        s.setDevice(device);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbFields;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#addFilterValue(java.lang.String, java.lang.String, java.lang.Object, java.util.Map, java.util.List)
     */
    @Override
    protected void addFilterValue(final String property, final Object value, final Map<String, Object> params,
            final List<String> filters) {
        final String defaultKey = DEFAULT_FILTER_KEY_PREFIX + property;

        if (STATUS_FIELD.equals(property)) {
            super.addFilterValue(property, value == null ? null : value.toString(), params, filters);
        } else if (ShipmentConstants.PROPERTY_SHIPPED_TO.equals(property)){
            //create placeholder for 'in' operator
            final List<String> in = new LinkedList<String>();
            int num = 0;
            for (final Object obj : ((List<?>) value)) {
                final String key = defaultKey + "_" + num;
                params.put(key, obj);
                in.add(":" + key);
                num++;
            }

            filters.add(TABLE + "." + SHIPPEDTO_FIELD + " in (" + StringUtils.combine(in, ",") + ")");
        } else if (ShipmentConstants.PROPERTY_SHIPPED_FROM.equals(property)){
            //create placeholder for 'in' operator
            final List<String> in = new LinkedList<String>();
            int num = 0;
            for (final Object obj : ((List<?>) value)) {
                final String key = defaultKey + "_" + num;
                params.put(key, obj);
                in.add(":" + key);
                num++;
            }

            filters.add(SHIPPEDFROM_FIELD + " in (" + StringUtils.combine(in, ",") + ")");
        } else if (ShipmentConstants.PROPERTY_SHIPPED_TO_DATE.equals(property)){
            //shipped to date
            params.put(defaultKey, value);
            filters.add(TABLE + "." + SHIPMENTDATE_FIELD + " <= :" + defaultKey);
        } else if (ShipmentConstants.PROPERTY_SHIPPED_FROM_DATE.equals(property)){
            //shipped from date
            params.put(defaultKey, value);
            filters.add(TABLE + "." + LASTEVENT_FIELD + " >= :" + defaultKey);
        } else if (ShipmentConstants.PROPERTY_SHIPMENT_DESCRIPTION.equals(property)){
            params.put(defaultKey, "%" + value + "%");
            filters.add(TABLE + "." + DESCRIPTION_FIELD + " like :" + defaultKey);
        } else if (ShipmentConstants.PROPERTY_DEVICE_SN.equals(property)){
            filters.add(ShipmentConstants.PROPERTY_DEVICE_SN + " = :" + defaultKey);
        } else {
            super.addFilterValue(property, value, params, filters);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#addFiltes(com.visfresh.dao.Filter, java.util.Map, java.util.List, java.util.Map)
     */
    @Override
    protected void addFiltesForFindAll(final Filter filter, final Map<String, Object> params,
            final List<String> filters) {
        final Object value = filter.getFilter(ShipmentConstants.PROPERTY_ONLY_WITH_ALERTS);
        if (value != null) {
            filter.removeFilter(ShipmentConstants.PROPERTY_ONLY_WITH_ALERTS);
        }

        super.addFiltesForFindAll(filter, params, filters);

        if (Boolean.TRUE.equals(value)) {
            filters.add("exists (select * from alerts where alerts.shipment = shipments.id)");
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#buildSelectBlockForFindAll()
     */
    @Override
    protected String buildSelectBlockForFindAll() {
        return "select "
                + getTableName()
                + ".*"
                + " , substring(d." + DeviceDaoImpl.IMEI_FIELD + ", -7, 6)"
                + " as " + ShipmentConstants.PROPERTY_DEVICE_SN
                + " , sfrom." + LocationProfileDaoImpl.NAME_FIELD
                + " as " + ShipmentConstants.PROPERTY_SHIPPED_FROM_LOCATION_NAME
                + " , sto." + LocationProfileDaoImpl.NAME_FIELD
                + " as " + ShipmentConstants.PROPERTY_SHIPPED_TO_LOCATION_NAME
                + " from " + getTableName()
                + " left outer join " + DeviceDaoImpl.TABLE + " as d"
                + " on " + getTableName() + "." + DEVICE_FIELD + " = d." + DeviceDaoImpl.IMEI_FIELD
                + " left outer join " + LocationProfileDaoImpl.TABLE + " as sfrom"
                + " on " + getTableName() + "." + SHIPPEDFROM_FIELD + " = sfrom." + LocationProfileDaoImpl.ID_FIELD
                + " left outer join " + LocationProfileDaoImpl.TABLE + " as sto"
                + " on " + getTableName() + "." + SHIPPEDTO_FIELD + " = sto." + LocationProfileDaoImpl.ID_FIELD
                ;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#addSortForDbField(java.lang.String, java.util.List, boolean)
     */
    @Override
    protected void addSortForDbField(final String field, final List<String> sorts,
            final boolean isAscent) {
        if (ShipmentConstants.PROPERTY_DEVICE_SN.equals(field)) {
            //also add the trip count to sort
            super.addSortForDbField(field, sorts, isAscent);
            super.addSortForDbField(TABLE + "." + TRIPCOUNT_FIELD, sorts, isAscent);
        } else if (ShipmentConstants.PROPERTY_SHIPPED_FROM_LOCATION_NAME.equals(field)){
            super.addSortForDbField(field, sorts, isAscent);
        } else if (ShipmentConstants.PROPERTY_SHIPPED_TO_LOCATION_NAME.equals(field)){
            super.addSortForDbField(field, sorts, isAscent);
        } else {
            super.addSortForDbField(TABLE + "." + field, sorts, isAscent);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#createNewFrom(com.visfresh.entities.ShipmentTemplate)
     */
    @Override
    public Shipment createNewFrom(final ShipmentTemplate tpl) {
        if (tpl == null || tpl.getId() == null) {
            return null;
        }

        //in fact the template and the shipment is some table.
        final Filter f = new Filter();
        f.addFilter(ID_FIELD, tpl.getId());

        final List<Shipment> list = daoBaseFindAll(f, null, null);
        if (!list.isEmpty()) {
            final Shipment s = list.get(0);
            s.setId(null);
            return s;
        }
        return null;
    }
}
