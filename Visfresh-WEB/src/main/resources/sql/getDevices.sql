select
	d.imei as imei,
	d.name as name,
	d.model as model,
	d.description as description,
	d.active as active,
	d.color as color,
	aut.id as autostartTemplateId,
	tpl.name as autostartTemplateName,
	substring(d.imei, -7, 6) as sn,
	COALESCE(substring(sp.device, -7, 6), '999999999999999999') as shipmentNumber,
	sp.id as lastShipmentId,
	lr.latitude as lastReadingLat,
	lr.longitude as lastReadingLong,
	lr.battery as lastReadingBattery,
	lr.temperature as lastReadingTemperature,
	lr.time as lastReadingTimeISO,
	sp.status as shipmentStatus,
	sp.tripcount as deviceTripCount
from devices d

left outer join (
	select
	s.*,
	-- selects last reading for shipment, it will used for join last reading data
	(select max(te1.id) from trackerevents te1 where te1.shipment = s.id) as lasteventid
	from shipments s
	-- joins all last shipments for given company for each company device
	join (select max(id) as id from shipments where company = :company group by device) s1
		on s1.id = s.id
) sp on sp.device = d.imei

left outer join trackerevents lr on lr.id = sp.lasteventid
left outer join autostartshipments aut on aut.id = d.autostart
left outer join shipments tpl on tpl.id = aut.template
where d.company = :company
