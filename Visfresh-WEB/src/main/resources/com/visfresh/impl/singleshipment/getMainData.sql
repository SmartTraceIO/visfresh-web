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
    bloa.actions as bloaActions,
    -- interim stops json
    (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'location', JSON_OBJECT(
	    		'locationId', loc.id,
	    		'companyName', loc.companydetails,
	    		'locationName', loc.name,
	    		'notes', loc.notes,
	    		'address', loc.address,
	    		'startFlag', IF(loc.start,'Y','N'),
	    		'interimFlag', IF(loc.interim,'Y','N'),
	    		'endFlag', IF(loc.stop,'Y','N'),
	    		'location', JSON_OBJECT(
	    			'lat', loc.latitude,
	    			'lon', loc.longitude
	    		),
				'radiusMeters', loc.radius
	    	),
		    'id', stp.id,
		    'stopDate', stp.date,
		    'time', stp.pause
	    )),
	    ']'
    ) from interimstops stp 
    join locationprofiles loc on stp.location = loc.id
    where stp.shipment = s.id) as interimStopsJson,
    -- Shipped from location
	JSON_OBJECT(
		'locationId', locFrom.id,
		'companyName', locFrom.companydetails,
		'locationName', locFrom.name,
		'notes', locFrom.notes,
		'address', locFrom.address,
		'startFlag', IF(locFrom.start,'Y','N'),
		'interimFlag', IF(locFrom.interim,'Y','N'),
		'endFlag', IF(locFrom.stop,'Y','N'),
		'location', JSON_OBJECT(
			'lat', locFrom.latitude,
			'lon', locFrom.longitude
		),
		'radiusMeters', locFrom.radius
	) as shippedFromJson,
    -- Shipped to location
	JSON_OBJECT(
		'locationId', locTo.id,
		'companyName', locTo.companydetails,
		'locationName', locTo.name,
		'notes', locTo.notes,
		'address', locTo.address,
		'startFlag', IF(locTo.start,'Y','N'),
		'interimFlag', IF(locTo.interim,'Y','N'),
		'endFlag', IF(locTo.stop,'Y','N'),
		'location', JSON_OBJECT(
			'lat', locTo.latitude,
			'lon', locTo.longitude
		),
		'radiusMeters', locTo.radius
	) as shippedToJson,
	-- alternative locations
    (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'location', JSON_OBJECT(
	    		'locationId', altLoc.id,
	    		'companyName', altLoc.companydetails,
	    		'locationName', altLoc.name,
	    		'notes', altLoc.notes,
	    		'address', altLoc.address,
	    		'startFlag', IF(altLoc.start,'Y','N'),
	    		'interimFlag', IF(altLoc.interim,'Y','N'),
	    		'endFlag', IF(altLoc.stop,'Y','N'),
	    		'location', JSON_OBJECT(
	    			'lat', altLoc.latitude,
	    			'lon', altLoc.longitude
	    		),
				'radiusMeters', altLoc.radius
	    	),
		    'locType', alt.loctype
	    )),
	    ']'
    )
	from alternativelocations alt
	join locationprofiles altLoc on alt.location = altLoc.id
    where alt.shipment = s.id) as altLocationJson
from
    shipments s
left outer join
	locationprofiles locFrom on s.shippedfrom = locFrom.id
left outer join
	locationprofiles locTo on s.shippedto = locTo.id
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