/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentTemplateDaoImpl extends ShipmentBaseDao<ShipmentTemplate>
    implements ShipmentTemplateDao {

    public static final String ADDDATASHIPPED_FIELD = "adddatashipped";
    public static final String DETECTLOCATION_FIELD = "detectlocation";
    public static final String USECURRENTTIME_FIELD = "usecurrenttime";

    /**
     * Default constructor.
     */
    public ShipmentTemplateDaoImpl() {
        super();
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
    protected ShipmentTemplate createEntity(final Map<String, Object> map,
            final Map<Long, Company> cache) {
        final ShipmentTemplate tpl = super.createEntity(map, cache);
        tpl.setAddDateShipped((Boolean) map.get(ADDDATASHIPPED_FIELD));
        tpl.setDetectLocationForShippedFrom((Boolean) map.get(DETECTLOCATION_FIELD));
        tpl.setUseCurrentTimeForDateShipped((Boolean) map.get(USECURRENTTIME_FIELD));
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
        params.put(USECURRENTTIME_FIELD, s.isUseCurrentTimeForDateShipped());
        return params;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.impl.ShipmentBaseDao#isTemplate()
     */
    @Override
    protected boolean isTemplate() {
        return true;
    }
}
