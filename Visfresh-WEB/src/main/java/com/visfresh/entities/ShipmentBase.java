/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

/**
 * Base class for Shipment templates and Shipments
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public abstract class ShipmentBase implements EntityWithId {
    /**
     * ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @Column(name = "id", columnDefinition="BIGINT AUTO_INCREMENT")
    private Long id;
    /**
     * Company name.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "company",
        foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT),
        columnDefinition = "bigint",
        referencedColumnName = "id")
    private Company company;
    /**
     * Name.
     */
    @Column(name = "name")
    private String name;
    /**
     * Shipment description.
     */
    @Column(name = "description")
    private String shipmentDescription;
    /**
     * Shipped from location.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "shippedfrom")
    private LocationProfile shippedFrom;
    /**
     * Shipped to location.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "shippedto")
    private LocationProfile shippedTo;
    /**
     * Alert profile
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "alertprofile")
    private AlertProfile alertProfile;
    /**
     * Alert notification schedules.
     */
    @ManyToMany
    private final List<NotificationSchedule> alertsNotificationSchedules = new LinkedList<NotificationSchedule>();
    /**
     * Alert suppression time in hours.
     */
    @Column(name = "alertcooldownsuppression")
    private int alertSuppressionDuringCoolDown;
    /**
     * Arrival notification with in.
     */
    @Column(name = "arrivalnotifwithin")
    private int arrivalNotificationWithIn;
    /**
     * Arrival notification shedules.
     */
    @ManyToMany
    private final List<NotificationSchedule> arrivalNotificationSchedules = new LinkedList<NotificationSchedule>();
    /**
     * Exclude notifications if not alerts fired.
     */
    @Column(name = "excludenoalerts")
    private boolean excludeNotificationsIfNoAlertsFired;
    /**
     * Shutdown device time out in minutes.
     */
    @Column(name = "shutdowntimeout")
    private int shutdownDeviceTimeOut;

    /**
     * Default constructor.
     */
    public ShipmentBase() {
        super();
    }

    /**
     * @param shipment the shipment to copy.
     */
    public ShipmentBase(final ShipmentBase shipment) {
        super();

        setName(shipment.getName());
        setShipmentDescription(shipment.getShipmentDescription());
        setShippedFrom(shipment.getShippedFrom());
        setShippedTo(shipment.getShippedTo());
        setAlertProfile(shipment.getAlertProfile());
        alertsNotificationSchedules.addAll((shipment.getAlertsNotificationSchedules()));
        setAlertSuppressionDuringCoolDown(shipment.getAlertSuppressionDuringCoolDown());
        setArrivalNotificationWithIn(shipment.getArrivalNotificationWithIn());
        arrivalNotificationSchedules.addAll((shipment.getArrivalNotificationSchedules()));
        setExcludeNotificationsIfNoAlertsFired(shipment.isExcludeNotificationsIfNoAlertsFired());
        setShutdownDeviceTimeOut(shipment.getShutdownDeviceTimeOut());
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the shipmentDescription
     */
    public String getShipmentDescription() {
        return shipmentDescription;
    }

    /**
     * @param shipmentDescription the shipmentDescription to set
     */
    public void setShipmentDescription(final String shipmentDescription) {
        this.shipmentDescription = shipmentDescription;
    }

    /**
     * @return the shippedFrom
     */
    public LocationProfile getShippedFrom() {
        return shippedFrom;
    }

    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final LocationProfile shippedFrom) {
        this.shippedFrom = shippedFrom;
    }

    /**
     * @return the shippedTo
     */
    public LocationProfile getShippedTo() {
        return shippedTo;
    }

    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final LocationProfile shippedTo) {
        this.shippedTo = shippedTo;
    }

    /**
     * @return the alertProfile
     */
    public AlertProfile getAlertProfile() {
        return alertProfile;
    }

    /**
     * @param alertProfile the alertProfile to set
     */
    public void setAlertProfile(final AlertProfile alertProfile) {
        this.alertProfile = alertProfile;
    }

    /**
     * @return the alertsNotificationSchedule
     */
    public List<NotificationSchedule> getAlertsNotificationSchedules() {
        return alertsNotificationSchedules;
    }
    /**
     * @return the alertSuppressionDuringCoolDown
     */
    public int getAlertSuppressionDuringCoolDown() {
        return alertSuppressionDuringCoolDown;
    }

    /**
     * @param alertSuppressionDuringCoolDown the alertSuppressionDuringCoolDown to set
     */
    public void setAlertSuppressionDuringCoolDown(final int alertSuppressionDuringCoolDown) {
        this.alertSuppressionDuringCoolDown = alertSuppressionDuringCoolDown;
    }

    /**
     * @return the arrivalNotification
     */
    public int getArrivalNotificationWithIn() {
        return arrivalNotificationWithIn;
    }

    /**
     * @param km number of kilometers for arrival notification.
     */
    public void setArrivalNotificationWithIn(final int km) {
        this.arrivalNotificationWithIn = km;
    }

    /**
     * @return the arrivalNotificationSchedule
     */
    public List<NotificationSchedule> getArrivalNotificationSchedules() {
        return arrivalNotificationSchedules;
    }
    /**
     * @return the excludeNotificationsIfNoAlertsFired
     */
    public boolean isExcludeNotificationsIfNoAlertsFired() {
        return excludeNotificationsIfNoAlertsFired;
    }

    /**
     * @param excludeNotificationsIfNoAlertsFired the excludeNotificationsIfNoAlertsFired to set
     */
    public void setExcludeNotificationsIfNoAlertsFired(final boolean excludeNotificationsIfNoAlertsFired) {
        this.excludeNotificationsIfNoAlertsFired = excludeNotificationsIfNoAlertsFired;
    }

    /**
     * @return the shutdownDevice
     */
    public int getShutdownDeviceTimeOut() {
        return shutdownDeviceTimeOut;
    }

    /**
     * @param ninutes the shutdownDevice to set
     */
    public void setShutdownDeviceTimeOut(final int ninutes) {
        this.shutdownDeviceTimeOut = ninutes;
    }
    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Company company) {
        this.company = company;
    }
    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
}
