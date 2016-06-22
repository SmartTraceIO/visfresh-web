/**
 *
 */
package com.visfresh.mpl.services.siblings;

import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.mpl.services.TrackerEventParser;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultSiblingDetectorSiblingIssueBugFix extends AbstractSiblingDetectorTool {
    private TrackerEventParser eventParser;

    /**
     * @throws Exception
     */
    public DefaultSiblingDetectorSiblingIssueBugFix() throws Exception {
        super();
        eventParser = new TrackerEventParser() {
            /* (non-Javadoc)
             * @see com.visfresh.mpl.services.TrackerEventParser#getCompany()
             */
            @Override
            public Company getCompany() {
                return company;
            }
        };
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.DefaultSiblingDetectorBugFix#parseData()
     */
    private void parseData() throws Exception {
        eventParser.parseData(
            DefaultSiblingDetectorSiblingIssueBugFix.class.getResource("shipments.csv"),
            DefaultSiblingDetectorSiblingIssueBugFix.class.getResource("events.csv"));
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.AbstractSiblingDetectorTool#getAllShipments()
     */
    @Override
    protected List<Shipment> getAllShipments() {
        return eventParser.getAllShipments();
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.siblings.AbstractSiblingDetectorTool#getEventsFromDb(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<TrackerEvent> getEventsFromDb(final Shipment shipment) {
        return eventParser.getEvents(shipment);
    }

    public void runTest() {
        //run sibling detection
        updateShipmentSiblingsForCompany(eventParser.getCompany());
    }

    public static void main(final String[] args)throws Exception {
        final DefaultSiblingDetectorSiblingIssueBugFix bugFix = new DefaultSiblingDetectorSiblingIssueBugFix();
        bugFix.parseData();
        bugFix.runTest();
    }
}
