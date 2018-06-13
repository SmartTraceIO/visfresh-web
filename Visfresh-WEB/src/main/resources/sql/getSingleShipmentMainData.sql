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
    nd.imei as nearestDevice,
    nd.color as nearestDeviceColor,
    d.name as deviceName,
    d.model as deviceModel,
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
    ap.id as apId,
    ap.name as apName,
    ap.description as apDescription,
	ap.onenterbright as onenterbright,
	ap.onenterdark as onenterdark,
	ap.onmovementstart as onmovementstart,
	ap.onmovementstop as onmovementstop,
	ap.onbatterylow as onbatterylow,
	ap.lowertemplimit as lowertemplimit,
	ap.uppertemplimit as uppertemplimit,
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
    join locationprofiles loc on loc.company = 123321 and stp.location = loc.id
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
	join locationprofiles altLoc on altLoc.company = 123321 and alt.location = altLoc.id
    where alt.shipment = s.id) as altLocationJson,
    -- Not user
    (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
			'noteText', n.notetext,
			'timeOnChart', n.timeonchart,
			'noteType', n.notetype,
			'noteNum', n.notenum,
			'creationDate', n.createdon,
			'createdBy', n.createdby,
			'active', IF(n.active, 'true', 'false'),
			'firstName', nu.firstname,
			'lastName', nu.lastname
	    )),
	    ']'
    )
	from notes n
 	left outer join users nu on nu.company = 123321 and n.createdby = nu.email
    where n.shipment = s.id) as notesJson,
    -- arrival
    arr.id as arrId,
    arr.nummeters as arrMeters,
    arr.date as arrDate,
    arr.event as arrEvent
    -- alert notification schedules
    ,(select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'id', sched.id,
	    	'name', sched.name,
	    	'description', sched.description
	    )),
	    ']'
        )
	from alertnotifschedules sc
	join notificationschedules sched on sched.company = 123321 and sc.notification = sched.id
	where sc.shipment = s.id
    ) as alertNotifSchedJson
    -- arrival notification schedules
    ,(select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'id', sched.id,
	    	'name', sched.name,
	    	'description', sched.description
	    )),
	    ']'
        )
	from arrivalnotifschedules sc
	join notificationschedules sched on sched.company = 123321 and sc.notification = sched.id
	where sc.shipment = s.id
    ) as arrivalNotifSchedJson
    -- notification schedule users
    , (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'schedule', sched.id,
	    	'firstName', u.firstname,
	    	'lastName', u.lastname
	    )),
	    ']'
        )
		from arrivalnotifschedules aa
		join notificationschedules sched on sched.company = 123321 and aa.notification = sched.id
        join personalschedules ps on ps.schedule = sched.id
        join users u on u.company = 123321 and ps.user = u.id
        where aa.shipment = s.id
    ) as arrivalScheduleUsersJson
    , (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'schedule', sched.id,
	    	'firstName', u.firstname,
	    	'lastName', u.lastname
	    )),
	    ']'
        )
		from alertnotifschedules aa
		join notificationschedules sched on sched.company = 123321 and aa.notification = sched.id
        join personalschedules ps on ps.schedule = sched.id
        join users u on u.company = 123321 and ps.user = u.id
        where aa.shipment = s.id
    ) as alertScheduleUsersJson
    , (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'groupId', grp.id,
	    	'name', grp.name,
	    	'description', grp.description
	    )),
	    ']'
        )
		from devicegroups grp
		join devicegrouprelations rel on rel.group = grp.id and grp.company = 123321
		where rel.device = s.device
    ) as deviceGroupsJson
    , (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'userId', u.id,
	    	'email', u.email
	    )),
	    ']'
        )
		from externalusers ex
		join users u on u.id = ex.user
		where ex.shipment = s.id
    ) as userAccessJson
    , (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'companyId', c.id,
	    	'companyName', c.name
	    )),
	    ']'
        )
		from externalcompanies ex
		join companies c on c.id = ex.company
		where ex.shipment = s.id
    ) as companyAccessJson
    , (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	        'id', a.id,
	        'type', a.type,
	        'date', a.date,
	        'trackerEventId', a.event,
            'temperature', a.temperature,
            'minutes', a.minutes,
            'cumulative', IF(a.cumulative, 'true', 'false'),
            'ruleId', a.rule 
	    )),
	    ']'
        )
		from alerts a
		where a.shipment = s.id
    ) as alertsJson
    , (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'id', r.id,
			'type', r.type,
			't', r.temp,
			'timeout', r.timeout, 
			'cumulative', IF(r.cumulative, 'true', false),
			'profile', r.alertprofile,
			'maxrates', r.maxrateminutes, 
			'actionsId', r.corractions,
			'actionsName', ca.name,
			'actionsDesc', ca.description,
			'actionsActions', ca.actions
	    )),
	    ']'
        )
		from temperaturerules r
		left outer join correctiveactions ca on ca.company = 123321 and r.corractions = ca.id
		where r.alertprofile = ap.id
    ) as alertRulesJson
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
left outer join -- arrival
	arrivals arr on arr.shipment = s.id
join
    devices d on d.imei = s.device
left outer join -- nearest tracker
	devices nd on nd.imei = s.nearestdevice
where
    s.id = 1387