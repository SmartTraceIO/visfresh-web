/**
 *
 */
package com.visfresh.constants;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentConstants extends BaseShipmentConstants {
    String CUSTOM_FIELDS = "customFields";
    String SHIPMENT_DATE = "shipmentDate";
    String SHUTDOWN_TIME_ISO = "shutdownTimeISO";
    String DEVICE_SHUTDOWN_TIME = "shutdownTime";
    String PO_NUM = "poNum";
    String TRIP_COUNT = "tripCount";
    String ASSET_NUM = "assetNum";
    String PALLET_ID = "palletId";
    String SHIPMENT_ID = "shipmentId";
    String ASSET_TYPE = "assetType";

    //search criterias
    String SHIPPED_FROM_DATE = "shippedFromDate";
    String SHIPPED_TO_DATE = "shippedToDate";
    String ONLY_WITH_ALERTS = "alertsOnly";
    String ARRIVAL_DATE = "actualArrivalDate";
    String ETA = "eta";
    String GOODS = "goods";
    String EXCLUDE_PRIOR_SHIPMENTS = "excludePriorShipments";

    String ALERT_PROFILE_NAME = "alertProfileName";
    String ALERT_PROFILE = "alertProfile";
    String SHIPPED_TO_LOCATION_NAME = "shippedToLocationName";
    String SHIPPED_FROM_LOCATION_NAME = "shippedFromLocationName";
    String DEVICE_IMEI = "deviceImei";
    String DEVICE_SN = "deviceSN";
    String DEVICE_NAME = "deviceName";
    String DEVICE_COLOR = "deviceColor";
    String STATUS = "status";
    String SIBLING_COUNT = "siblingCount";
    String LAST_READING_TIME = "lastReadingTime";
    String LAST_READING_TIME_ISO = "lastReadingTimeISO";
    String LAST_READING_TEMPERATURE = "lastReadingTemperature";
    String ALERT_SUMMARY = "alertSummary";
    String END_LOCATION_ALTERNATIVES = "endLocationAlternatives";
}
