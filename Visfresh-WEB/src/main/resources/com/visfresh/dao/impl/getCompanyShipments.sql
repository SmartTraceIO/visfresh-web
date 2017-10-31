select
  shipments.*,
  -- shipped from
  sfrom.name as shippedFromLocationName,
  sfrom.latitude as shippedFromLat,
  sfrom.longitude as shippedFromLon,
  -- shipped to
  sto.name as shippedToLocationName,
  sto.latitude as shippedToLat,
  sto.longitude as shippedToLon,
  -- first reading
  (select JSON_OBJECT(
    'id', e.id, 
    'time', e.time, 
    'temperature', e.temperature, 
    'lat', e.latitude, 
    'lon', e.longitude,
    'battery', e.battery
    )
  from trackerevents e 
  where e.shipment = shipments.id order by e.time limit 1) as firstReadingJson,
  -- last reading
  (select JSON_OBJECT(
    'id', e.id, 
    'time', e.time, 
    'temperature', e.temperature, 
    'lat', e.latitude, 
    'lon', e.longitude,
    'battery', e.battery
    )
  from trackerevents e 
  where e.shipment = shipments.id order by e.time desc limit 1) as lastReadingJson,
  (select te.temperature from trackerevents te
     where te.shipment = shipments.id order by te.time desc, te.id desc limit 1) as lastReadingTemperature,
  -- alerts
  (select count(*) from alerts al where al.shipment = shipments.id) as alertSummary, -- this field required for sorting
    (select CONCAT(
	    '[', 
	    GROUP_CONCAT(JSON_OBJECT(
	    	'id', r.id,
			'type', r.type,
			't', r.temp,
			'timeout', r.timeout, 
			'cumulative', IF(r.cumulative, 'true', false),
			'profile', r.alertprofile,
			'maxrates', r.maxrateminutes
	    )),
	    ']'
        )
		from temperaturerules r
		join alerts a on a.rule = r.id
		where a.shipment = shipments.id
    ) as alertRulesJson
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
		where a.shipment = shipments.id
    ) as alertsJson
  -- device
  ,d.name as deviceName,
  substring(d.imei, -7, 6) as deviceSn,
  -- arrival
  arr.date as arrivalDate,
  -- alert profile
  ap.id as alertProfileId,
  ap.name as alertProfileName,
  ap.uppertemplimit as upperTemperatureLimit,
  ap.lowertemplimit as lowerTemperatureLimit
from shipments
left outer join alertprofiles as ap on shipments.alert = ap.id
left outer join devices as d on shipments.device = d.imei
left outer join locationprofiles as sfrom on shipments.shippedfrom = sfrom.id
left outer join locationprofiles as sto on shipments.shippedto = sto.id
left outer join arrivals as arr on arr.shipment = shipments.id
