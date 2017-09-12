/**
 *
 */
package com.visfresh.impl.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Location;
import com.visfresh.entities.Note;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentServiceTest extends SingleShipmentServiceImpl {

    //support fields for overriding the service methods
    private long nextId;

    private final Map<Long, List<Alert>> alerts = new HashMap<>();
    private final Map<Long, Date> alertsSuppressionDates = new HashMap<>();
    private final Map<Long, List<AlertRule>> alertsYetToFire = new HashMap<>();
    private final Map<Long, SingleShipmentBean> beans = new HashMap<>();
    private final Map<Long, List<InterimStop>> interimStops = new HashMap<>();
    private final Map<Long, Arrival> arrivals = new HashMap<>();
    private final Map<Long, List<AlertRule>> sentAlertRules = new HashMap<>();
    private final Map<Long, AlternativeLocations> alternativeLocations = new HashMap<>();
    private final Map<Long, List<Note>> notes = new HashMap<>();
    private final Map<Long, List<TrackerEventDto>> readings = new HashMap<>();
    private final List<Shipment> shipments = new LinkedList<>();
    private final Map<Long, Boolean> alertSuppressions = new HashMap<>();
    private final Map<Long, Boolean> sentReports = new HashMap<>();
    private final Map<Long, List<DeviceGroupDto>> shipmentGroups = new HashMap<>();

    private Company company = createCompany();
    private Device device = createDevice("102983470912874");

    /**
     * Default constructor.
     */
    public SingleShipmentServiceTest() {
        super();
    }


    @Test
    public void testWithoutReadings() {
        final Shipment s = createShipment(ShipmentStatus.InProgress);
        final SingleShipmentData data = this.getShipmentData(s.getId());

        assertNotNull(data);
        //check bean created
        assertNotNull(this.beans.get(s.getId()));
    }
    @Test
    public void testSiblings() {
        final Shipment s1 = createShipment(ShipmentStatus.InProgress);
        final Shipment s2 = createShipment(ShipmentStatus.InProgress);
        final Shipment s3 = createShipment(ShipmentStatus.InProgress);
        setAsSiblings(s1, s2, s3);

        //check bean created
        final SingleShipmentData data = this.getShipmentData(s1.getId());
        final Set<Long> siblings = new HashSet<>(data.getBean().getSiblings());
        assertEquals(2, siblings.size());
        assertTrue(siblings.contains(s2.getId()));
        assertTrue(siblings.contains(s3.getId()));
    }
    /**
     * @param shipments
     */
    private void setAsSiblings(final Shipment... shipments) {
        //create ID set
        final Set<Long> ids = new HashSet<>();
        for (final Shipment s : shipments) {
            ids.add(s.getId());
        }

        //set as siblings
        for (final Shipment s : shipments) {
            //create copy of ID set
            final Set<Long> siblings = new HashSet<>(ids);
            //remove given shipment
            siblings.remove(s.getId());

            //set as siblings
            s.getSiblings().clear();
            s.getSiblings().addAll(siblings);
            s.setSiblingCount(s.getSiblings().size());
        }
    }

    @Test
    public void testShipmentNotFound() {
        final SingleShipmentData data = this.getShipmentData(-111l);

        assertNull(data);
        assertEquals(0, this.beans.size());
    }
    @Test
    public void testReadingsWithoutLocations() {
        final Shipment s = createShipment(ShipmentStatus.InProgress);
        createReading(s, null, null);

        final SingleShipmentData data = this.getShipmentData(s.getId());
        assertNotNull(data);
        //check bean created
        assertNotNull(this.beans.get(s.getId()));
    }

    /**
     * @param s shipment.
     * @return new reading for given shipment.
     */
    private TrackerEventDto createReading(final Shipment s, final Double latitude, final Double longitude) {
        final TrackerEventDto r = new TrackerEventDto();

        r.setId(nextId++);
        r.setShipmentId(s.getId());
        r.setCreatedOn(new Date());
        r.setDeviceImei(s.getDevice().getImei());
        r.setLatitude(latitude);
        r.setLongitude(longitude);
        r.setType(TrackerEventType.AUT);
        r.setTime(r.getCreatedOn());
        r.setTemperature(11.);

        List<TrackerEventDto> list = this.readings.get(s.getId());
        if (list == null) {
            list = new LinkedList<>();
            readings.put(s.getId(), list);
        }

        list.add(r);
        return r;
    }


    public Shipment createShipment(final ShipmentStatus status) {
        final Shipment s = new Shipment();
        s.setId(nextId++);
        s.setCompany(company);
        s.setDevice(device);
        s.setTripCount(device.getTripCount() + 1);
        device.setTripCount(s.getTripCount());
        s.setStatus(status);
        this.shipments.add(s);
        return s;
    }
    /**
     * @param imei device IMEI.
     * @return device.
     */
    private Device createDevice(final String imei) {
        final Device d = new Device();
        d.setCompany(company);
        d.setImei(imei);
        d.setActive(true);
        d.setName("JUnit-" + imei);
        return d;
    }
    /**
     * @return company.
     */
    private Company createCompany() {
        final Company c = new Company();
        c.setId(nextId++);
        c.setName("JUnit");
        return c;
    }
    //support methods:
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getAlerts(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<Alert> getAlerts(final Shipment s) {
        final List<Alert> a = alerts.get(s.getId());
        return a == null ? new LinkedList<>() : a;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getAlertsSuppressionDate(com.visfresh.entities.Shipment)
     */
    @Override
    protected Date getAlertsSuppressionDate(final Shipment s) {
        return alertsSuppressionDates.get(s.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getAlertYetFoFireImpl(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<AlertRule> getAlertYetFoFireImpl(final Shipment s) {
        final List<AlertRule> alerts = alertsYetToFire.get(s.getId());
        return alerts == null ? new LinkedList<>() : alerts;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getBeanIncludeSiblings(long)
     */
    @Override
    protected List<SingleShipmentBean> getBeanIncludeSiblings(final long shipmentId) {
        final List<SingleShipmentBean> result = new LinkedList<>();
        final SingleShipmentBean mainBean = beans.get(shipmentId);
        if (mainBean != null) {
            result.add(mainBean);
            for (final Long id : mainBean.getSiblings()) {
                final SingleShipmentBean sibling = beans.get(id);
                if (sibling != null) {
                    result.add(sibling);
                }
            }
        }

        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getBeanIncludeSiblings(java.lang.String, int)
     */
    @Override
    protected List<SingleShipmentBean> getBeanIncludeSiblings(final String sn, final int tripCount) {
        final Long id = getShipmentId(sn, tripCount);
        return super.getBeanIncludeSiblings(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getInterimStops(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<InterimStop> getInterimStops(final Shipment s) {
        final List<InterimStop> stops = interimStops.get(s.getId());
        return stops == null ? new LinkedList<>() : stops;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getAlertFiredImpl(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<AlertRule> getAlertFiredImpl(final Shipment s) {
        final List<AlertRule> alerts = sentAlertRules.get(s.getId());
        return alerts == null ? new LinkedList<>() : alerts;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getAlternativeLocations(com.visfresh.entities.Shipment)
     */
    @Override
    protected AlternativeLocations getAlternativeLocations(final Shipment s) {
        return alternativeLocations.get(s.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getArrival(com.visfresh.entities.Shipment)
     */
    @Override
    protected Arrival getArrival(final Shipment s) {
        return arrivals.get(s.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getLocationDescription(com.visfresh.entities.Location)
     */
    @Override
    protected String getLocationDescription(final Location loc) {
        return "Not determined";
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getNotes(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<Note> getNotes(final Shipment s) {
        final List<Note> result = notes.get(s.getId());
        return result == null ? new LinkedList<>() : result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getReadings(long)
     */
    @Override
    protected List<TrackerEventDto> getReadings(final long shipmentId) {
        return readings.get(shipmentId);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getShipment(long)
     */
    @Override
    protected Shipment getShipment(final long shipmentId) {
        for (final Shipment s : shipments) {
            if (s.getId().equals(shipmentId)) {
                return s;
            }
        }
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getShipmentId(java.lang.String, int)
     */
    @Override
    protected Long getShipmentId(final String sn, final int tripCount) {
        for (final Shipment s : shipments) {
            if (Device.getSerialNumber(s.getDevice().getImei()).equals(sn)
                    && s.getTripCount() == tripCount) {
                return s.getId();
            }
        }
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#isAlertsSuppressed(com.visfresh.entities.Shipment)
     */
    @Override
    protected boolean isAlertsSuppressed(final Shipment s) {
        return Boolean.TRUE.equals(alertSuppressions.get(s.getId()));
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#isArrivalReportSent(com.visfresh.entities.Shipment)
     */
    @Override
    protected boolean isArrivalReportSent(final Shipment s) {
        return Boolean.TRUE.equals(sentReports.get(s.getId()));
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#saveBean(com.visfresh.io.shipment.SingleShipmentBean)
     */
    @Override
    protected void saveBean(final SingleShipmentBean bean) {
        beans.put(bean.getShipmentId(), bean);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getShipmentGroups(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<DeviceGroupDto> getShipmentGroups(final Shipment s) {
        final List<DeviceGroupDto> groups = shipmentGroups.get(s.getId());
        return groups == null ? new LinkedList<>() : groups;
    }
}
