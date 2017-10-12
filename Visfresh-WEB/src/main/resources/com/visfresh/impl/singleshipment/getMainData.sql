-- the 1387 shipment ID is used as placeholder for allow to edit given script in MySql editor
select 
    s.id as id,
    s.noalertsifcooldown as alertSuppressionMinutes,
    s.arrivalnotifwithIn as arrivalNotificationWithinKm,
    s.arrivaldate as arrivalDate,
    s.assetnum as assetNum,
    s.assettype as assetType,
    s.comments as commentsForReceiver,
    s.company as company,
    s.device as device,
    d.color as deviceColor,
    d.name as deviceName,
    s.nonotifsifnoalerts as excludeNotificationsIfNoAlerts,
    s.status as status,
    s.tripcount as tripCount,
    d.tripcount as deviceTripCount,
    s.noalertsafterarrivalminutes as noAlertsAfterArrivalMinutes,
    s.noalertsafterstartminutes as noAlertsAfterStartMinutes,
    s.palletid as palletId,
    s.arrivalreport as isSendArrivalReport,
    s.arrivalreportonlyifalerts as sendArrivalReportOnlyIfAlerts,
    s.description as description,
    s.id as id,
    s.isautostart as autostart,
    s.shutdownafterstartminutes as shutDownAfterStartMinutes,
    s.shutdownafterarrivalminutes as shutdownDeviceAfterMinutes,
    s.shipmentdate as startTime,
    s.eta as eta,
    ss.state as session,
    -- main alert profile properties
    -- battery low corrective actions properties
    lona.id as lonaId,
    lona.name as lonaName,
    lona.description as lonaDesc,
    lona.actions as lonaActions,
    -- light on corrective actions properties 
    bloa.id as bloaId,
    bloa.name as bloaName,
    bloa.description as bloaDesc,
    bloa.actions as bloaActions
from
    shipments s
left outer join
    shipmentsessions ss on ss.shipment = s.id
left outer join
    alertprofiles ap on ap.id = s.alert
left outer join -- light on corrective actions
    correctiveactions lona on ap.lightonactions = lona.id
left outer join -- battery low corrective actions
    correctiveactions bloa on ap.batterylowactions = bloa.id
join
    devices d on d.imei = s.device
where
    s.id = 1387 or s.siblings = 1387
        or s.siblings like concat(1387, ',%')
        or s.siblings like concat('%,', 1387, ',%')
        or s.siblings like concat('%,', 1387)