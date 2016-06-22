/**
 *
 */
package com.visfresh.mpl.services.siblings;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.CollectionUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractSiblingDetectorTool extends DefaultSiblingDetector {
    protected Company company;

    /**
     * Default constructor.
     */
    public AbstractSiblingDetectorTool() throws Exception {
        super(0);
        company = new Company();
        company.setId(1l);
        company.setName("JUnit Company");
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#findActiveShipments(com.visfresh.entities.Company)
     */
    @Override
    protected List<Shipment> findActiveShipments(final Company company) {
        final List<Shipment> list = new LinkedList<>();
        for (final Shipment shipment : getAllShipments()) {
            if (!shipment.hasFinalStatus()) {
                list.add(shipment);
            }
        }

        CollectionUtils.sortById(list);
        return list;
    }

    /**
     * @return
     */
    protected abstract List<Shipment> getAllShipments();
    @Override
    protected abstract List<TrackerEvent> getEventsFromDb(final Shipment shipment);

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#getShipments(java.util.Set)
     */
    @Override
    protected List<Shipment> getShipments(final Set<Long> ids) {
        final List<Shipment> list = new LinkedList<>();
        for (final Shipment shipment : getAllShipments()) {
            if (ids.contains(shipment.getId())) {
                list.add(shipment);
            }
        }
        return list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetector#updateSiblingInfo(com.visfresh.entities.Shipment, java.util.Set)
     */
    @Override
    protected void updateSiblingInfo(final Shipment master, final Set<Long> set) {
        master.getSiblings().clear();
        master.getSiblings().addAll(set);
        master.setSiblingCount(set.size());
    }
}
