/**
 *
 */
package com.visfresh.dao.impl.shipment;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.dao.impl.SelectAllSupport;
import com.visfresh.entities.Device;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class ShipmetBaseSelectAllSupport extends SelectAllSupport {
    private static final String selectAll = StringUtils.loadSql("getShipments");
    private static final String allForCompany = StringUtils.loadSql("getCompanyShipments");

    /**
     * @param tableName
     */
    public ShipmetBaseSelectAllSupport() {
        super(ShipmentBaseDao.TABLE);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#addFiltes(com.visfresh.dao.Filter, java.util.Map, java.util.List, java.util.Map)
     */
    @Override
    protected void addFiltesForFindAll(final Filter filter, final Map<String, Object> params,
            final List<String> filters) {
        Object value = filter.getFilter(ShipmentConstants.EXCLUDE_PRIOR_SHIPMENTS);
        if (value != null) {
            filter.removeFilter(ShipmentConstants.EXCLUDE_PRIOR_SHIPMENTS);
            if (Boolean.TRUE.equals(value)) {
                filters.add("d.tripcount = shipments.tripcount");
            }
        }

        value = filter.getFilter(ShipmentConstants.ONLY_WITH_ALERTS);
        if (value != null) {
            filter.removeFilter(ShipmentConstants.ONLY_WITH_ALERTS);
            filters.add("exists (select * from alerts where"
                    + " alerts.shipment = shipments.id and alerts.type <> 'LightOn' and alerts.type <> 'LightOff')");
        }

        super.addFiltesForFindAll(filter, params, filters);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#buildSelectBlockForFindAll()
     */
    @Override
    protected String buildSelectBlockForFindAll(final Filter filter) {
        return selectAll;
    }

    /**
     * @param originFilter
     * @param sorting
     * @param page
     */
    public void buildCompanyShipmentsSelectAll(final Filter originFilter, final Sorting sorting, final Page page) {
        final Filter filter = buildFilter(originFilter);
        buildSelectAll(allForCompany, sorting, page, filter);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#buildSelectBlockForEntityCount(com.visfresh.dao.Filter)
     */
    @Override
    protected String buildSelectBlockForEntityCount(final Filter filter) {
        return "select count(*) as count from " + getTableName()
            + "\njoin devices d on d.imei = shipments.device\n";
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#addSortForDbField(java.lang.String, java.util.List, boolean)
     */
    @Override
    protected void addSortForDbField(final String field, final List<String> sorts,
            final boolean isAscent) {
        if (ShipmentConstants.DEVICE_SN.equals(field)) {
            //also add the trip count to sort
            super.addSortForDbField(field, sorts, isAscent);
            super.addSortForDbField(ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.TRIPCOUNT_FIELD, sorts, isAscent);
        } else if (ShipmentConstants.SHIPPED_FROM_LOCATION_NAME.equals(field)){
            super.addSortForDbField(field, sorts, isAscent);
        } else if (ShipmentConstants.SHIPPED_TO_LOCATION_NAME.equals(field)){
            super.addSortForDbField(field, sorts, isAscent);
        } else if (ShipmentConstants.LAST_READING_TEMPERATURE.equals(field)){
            super.addSortForDbField(field, sorts, isAscent);
        } else if (ShipmentConstants.ALERT_SUMMARY.equals(field)){
            super.addSortForDbField(field, sorts, isAscent);
        } else if (ShipmentConstants.SHIPPED_FROM_DATE.equals(field)){
            //shipped from date
            super.addSortForDbField("COALESCE(" + field
                    + "," + ShipmentDaoImpl.SHIPMENTDATE_FIELD + ")", sorts, isAscent);
        } else if (ShipmentConstants.NEAREST_TRACKER.equals(field)) {
            super.addSortForDbField("nearestTrackerSn", sorts, isAscent);
        } else {
            super.addSortForDbField(ShipmentDaoImpl.TABLE + "." + field, sorts, isAscent);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#addFilterValue(java.lang.String, java.lang.String, java.lang.Object, java.util.Map, java.util.List)
     */
    @Override
    protected void addFilterValue(final String property, final Object value, final Map<String, Object> params,
            final List<String> filters) {
        final String defaultKey = DEFAULT_FILTER_KEY_PREFIX + property;

        if (ShipmentDaoImpl.STATUS_FIELD.equals(property)) {
            super.addFilterValue(property, value == null ? null : value.toString(), params, filters);
        } else if (ShipmentConstants.SHIPPED_TO.equals(property)){
            //create placeholder for 'in' operator
            final List<String> in = new LinkedList<String>();
            int num = 0;
            for (final Object obj : ((List<?>) value)) {
                final String key = defaultKey + "_" + num;
                params.put(key, obj);
                in.add(":" + key);
                num++;
            }

            filters.add(ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.SHIPPEDTO_FIELD + " in (" + StringUtils.combine(in, ",") + ")");
        } else if (ShipmentConstants.SHIPPED_FROM.equals(property)){
            //create placeholder for 'in' operator
            final List<String> in = new LinkedList<String>();
            int num = 0;
            for (final Object obj : ((List<?>) value)) {
                final String key = defaultKey + "_" + num;
                params.put(key, obj);
                in.add(":" + key);
                num++;
            }

            filters.add(ShipmentDaoImpl.SHIPPEDFROM_FIELD + " in (" + StringUtils.combine(in, ",") + ")");
        } else if (ShipmentConstants.SHIPPED_TO_DATE.equals(property)){
            //shipped to date
            params.put(defaultKey, value);
            filters.add(ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.SHIPMENTDATE_FIELD + " <= :" + defaultKey);
        } else if (ShipmentConstants.SHIPPED_FROM_DATE.equals(property)){
            //shipped from date
            params.put(defaultKey, value);
            filters.add("COALESCE(" + ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.LASTEVENT_FIELD + "," + ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.SHIPMENTDATE_FIELD + ") >= :" + defaultKey);
        } else if (ShipmentConstants.SHIPMENT_DESCRIPTION.equals(property)){
            params.put(defaultKey, "%" + value + "%");
            filters.add(ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.DESCRIPTION_FIELD + " like :" + defaultKey);
        } else if (ShipmentConstants.DEVICE_SN.equals(property)){
            params.put(defaultKey, "%" + Device.addZeroSymbolsToSn(String.valueOf(value)) + "_");
            filters.add(ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.DEVICE_FIELD + " like :" + defaultKey);
        } else if (ShipmentConstants.GOODS.equals(property)){
            params.put(defaultKey, "%" + value + "%");
            final StringBuilder sb = new StringBuilder();
            sb.append('(');
            sb.append(ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.DESCRIPTION_FIELD + " like :" + defaultKey);
            sb.append(" or ");
            sb.append(ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.PALETTID_FIELD + " like :" + defaultKey);
            sb.append(" or ");
            sb.append(ShipmentDaoImpl.TABLE + "." + ShipmentDaoImpl.ASSETNUM_FIELD + " like :" + defaultKey);
            sb.append(')');
            filters.add(sb.toString());
        } else if (ShipmentConstants.EXCLUDE_PRIOR_SHIPMENTS.equals(property)){
            //nothing in where clause
        } else {
            super.addFilterValue(property, value, params, filters);
        }
    }
}
