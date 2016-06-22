/**
 *
 */
package com.visfresh.mpl.services.injectte;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.dao.impl.DaoImplBase;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.mpl.services.TrackerEventParser;
import com.visfresh.tools.SpringConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEventsInjector extends TrackerEventParser {
    private CompanyDao companyDao;
    private DeviceDao deviceDao;
    private ShipmentDao shipmentDao;
    private TrackerEventDao trackerEventDao;

    private Company company;
    private Map<Long, Long> shipmentIdReplaces = new HashMap<>();

    /**
     * @throws Exception
     *
     */
    public TrackerEventsInjector(final ApplicationContext context) throws Exception {
        super();

        //get DAO
        this.companyDao = context.getBean(CompanyDao.class);
        this.deviceDao = context.getBean(DeviceDao.class);
        this.shipmentDao = context.getBean(ShipmentDao.class);
        this.trackerEventDao = context.getBean(TrackerEventDao.class);

        //initialize
        //find company
        company = findCompany();
    }

    /**
     * @return the demo company.
     */
    private Company findCompany() {
        for(final Company c: companyDao.findAll(null, null, null)) {
            if ("Demo".equals(c.getName())) {
                return c;
            }
        }

        throw new IllegalArgumentException("Unable to find the 'Demo' company");
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.TrackerEventParser#createDevice(java.lang.String)
     */
    @Override
    protected Device createDevice(final String imei) {
        final Device d = super.createDevice(imei);
        deviceDao.save(d);
        return d;
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.TrackerEventParser#parseShipments(java.net.URL)
     */
    @Override
    protected Map<Long, Shipment> parseShipments(final URL shipmentsDataUrl)
            throws Exception {
        final Map<Long, Shipment> result = new HashMap<>();
        final Map<Long, Shipment> parsedShipments = super.parseShipments(shipmentsDataUrl);
        for (final Shipment s : parsedShipments.values()) {
            final Long old = s.getId();
            s.setId(null);
            shipmentDao.save(s);

            shipmentIdReplaces.put(old, s.getId());
            result.put(s.getId(), s);
        }

        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.TrackerEventParser#getShipmentForParsedShipmentId(long)
     */
    @Override
    protected Shipment getShipmentForParsedShipmentId(final long shipmentId) {
        return super.getShipmentForParsedShipmentId(
                shipmentIdReplaces.get(shipmentId));
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.TrackerEventParser#getCompany()
     */
    @Override
    public Company getCompany() {
        return company;
    }
    /**
     * @throws Exception
     */
    public void injectEvents() throws Exception {
        parseData(
            TrackerEventsInjector.class.getResource("shipments.csv"),
            TrackerEventsInjector.class.getResource("events.csv"));

        //add events to DB.
        for (final Shipment s : getAllShipments()) {
            final List<TrackerEvent> events = getEvents(s);
            for (final TrackerEvent e : events) {
                e.setId(null);
                if (e.getType() == null) {
                    e.setType(TrackerEventType.AUT);
                }
                trackerEventDao.save(e);
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan(
            SpringConfig.class.getPackage().getName(),
            DaoImplBase.class.getPackage().getName());
        context.refresh();

        final TrackerEventsInjector injector = new TrackerEventsInjector(context);
        injector.injectEvents();
    }
}
