/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Shipment;
import com.visfresh.services.SiblingDetectorService;
import com.visfresh.utils.CollectionUtils;
import com.visfresh.utils.Grouper;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SiblingDetectorServiceImpl implements SiblingDetectorService {
    /**
     * Group name prefix.
     */
    private static final String GROUP_PREFIX = "siblingGroup_";
    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public SiblingDetectorServiceImpl() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SiblingDetectorService#getSiblings(com.visfresh.entities.Shipment)
     */
    @Override
    public List<Shipment> getSiblings(final Shipment shipment) {
        final String groupKey = getGroup(shipment);
        if (groupKey == null) {
            return new LinkedList<>();
        }

        final Sorting sorting = new Sorting(ShipmentConstants.PROPERTY_SHIPMENT_ID);
        //device all shipments to groups
        final Map<String, List<Shipment>> groups = CollectionUtils.group(
                shipmentDao.findByCompany(shipment.getCompany(), sorting, null, null),
                new Grouper<Shipment>() {
                    /* (non-Javadoc)
                     * @see com.visfresh.utils.Grouper#getGroup(java.lang.Object)
                     */
                    @Override
                    public String getGroup(final Shipment obj) {
                        return getGroup(obj);
                    }
                });

        List<Shipment> group = groups.get(groupKey);
        if (group == null) {
            group = new LinkedList<>();
        } else {
            //remove given shipment from group
            removeGivenShipment(group, shipment);
        }

        return group;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SiblingDetectorService#getSiblingCount(com.visfresh.entities.Shipment)
     */
    @Override
    public int getSiblingCount(final Shipment s) {
        return getSiblings(s).size();
    }
    /**
     * @param group group.
     * @param shipment shipment to remove.
     */
    private void removeGivenShipment(final List<Shipment> group, final Shipment shipment) {
        final Iterator<Shipment> iter = group.iterator();
        while (iter.hasNext()) {
            if (iter.next().getId().equals(shipment.getId())) {
                iter.remove();
                return;
            }
        }
    }
    /**
     * @param shipment
     * @return
     */
    private String getGroup(final Shipment shipment) {
        final String desc = shipment.getShipmentDescription();
        return getGroup(desc);
    }
    /**
     * @param desc
     * @return
     */
    private static String getGroup(final String desc) {
        if (desc != null) {
            for (final String seg: desc.split("[^\\w]+")) {
                if (seg.startsWith(GROUP_PREFIX)) {
                    return seg.substring(GROUP_PREFIX.length());
                }
            }
        }
        return null;
    }
}
