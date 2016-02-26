/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.ShipmentTemplateConstants;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentTemplateDaoImpl extends ShipmentBaseDao<ShipmentTemplate>
    implements ShipmentTemplateDao {

    private static final String ADDDATASHIPPED_FIELD = "adddatashipped";
    private static final String DETECTLOCATION_FIELD = "detectlocation";
    public static final String AUTOSTART_FIELD = "isautostart";

    private final Map<String, String> propertyToDbFields = new HashMap<String, String>();
    /**
     * Default constructor.
     */
    public ShipmentTemplateDaoImpl() {
        super();
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES,
                SHUTDOWNTIMEOUT_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS,
                NONOTIFSIFNOALERTS_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM,
                ARRIVALNOTIFWITHIN_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_COMMENTS_FOR_RECEIVER,
                COMMENTS_FIELD);
//        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_MAX_TIMES_ALERT_FIRES, );
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES,
                NOALERTIFCOODOWN_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_DETECT_LOCATION_FOR_SHIPPED_FROM,
                DETECTLOCATION_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_SHIPPED_TO,
                SHIPPEDTO_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_SHIPPED_FROM,
                SHIPPEDFROM_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_ADD_DATE_SHIPPED,
                ADDDATASHIPPED_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_SHIPMENT_DESCRIPTION,
                DESCRIPTION_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_NAME,
                NAME_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_ID,
                ID_FIELD);
        propertyToDbFields.put(ShipmentTemplateConstants.PROPERTY_ALERT_PROFILE_ID,
                ALERT_FIELD);

    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createEntity()
     */
    @Override
    protected ShipmentTemplate createEntity() {
        return new ShipmentTemplate();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createEntity(java.util.Map, java.util.Map)
     */
    @Override
    protected ShipmentTemplate createEntity(final Map<String, Object> map) {
        final ShipmentTemplate tpl = super.createEntity(map);
        tpl.setAddDateShipped((Boolean) map.get(ADDDATASHIPPED_FIELD));
        tpl.setDetectLocationForShippedFrom((Boolean) map.get(DETECTLOCATION_FIELD));
        tpl.setName((String) map.get(NAME_FIELD));
        tpl.setAutostart(Boolean.TRUE.equals(map.get(AUTOSTART_FIELD)));
        return tpl;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#createParameterMap(com.visfresh.entities.ShipmentBase)
     */
    @Override
    protected Map<String, Object> createParameterMap(final ShipmentTemplate s) {
        final Map<String, Object> params = super.createParameterMap(s);
        params.put(ADDDATASHIPPED_FIELD, s.isAddDateShipped());
        params.put(DETECTLOCATION_FIELD, s.isDetectLocationForShippedFrom());
        params.put(NAME_FIELD, s.getName());
        params.put(AUTOSTART_FIELD, s.isAutostart());
        return params;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#buildSelectBlockForFindAll()
     */
    @Override
    protected String buildSelectBlockForFindAll() {
        return "select "
                + getTableName()
                + ".*"
                + " , shipfrom." + LocationProfileDaoImpl.NAME_FIELD
                + " as " + ShipmentTemplateConstants.PROPERTY_SHIPPEDFROM_NAME
                + " , shipto." + LocationProfileDaoImpl.NAME_FIELD
                + " as " + ShipmentTemplateConstants.PROPERTY_SHIPPEDTO_NAME
                + " from " + getTableName()
            //join alert profiles table
            + " left outer join " + AlertProfileDaoImpl.TABLE + " ap on ap."
            + AlertProfileDaoImpl.ID_FIELD + " = " + ALERT_FIELD + "\n"
            //join location from
            + " left outer join " + LocationProfileDaoImpl.TABLE + " shipfrom on shipfrom."
            + LocationProfileDaoImpl.ID_FIELD + " = " + SHIPPEDFROM_FIELD
            //join location to
            + " left outer join " + LocationProfileDaoImpl.TABLE + " shipto on shipto."
            + LocationProfileDaoImpl.ID_FIELD + " = " + SHIPPEDTO_FIELD;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#isTemplate()
     */
    @Override
    protected boolean isTemplate() {
        return true;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.DaoImplBase#getPropertyToDbMap()
     */
    @Override
    protected Map<String, String> getPropertyToDbMap() {
        return propertyToDbFields;
    }
}
