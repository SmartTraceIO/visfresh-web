select
  shipments.*,
  substring(d.imei, -7, 6) as deviceSN,
  sfrom.name as shippedFromLocationName,
  sto.name as shippedToLocationName,
  (select te.temperature from trackerevents te
     where te.shipment = shipments.id order by te.time desc, te.id desc limit 1) as lastReadingTemperature,
  (select count(*) from alerts al where al.shipment = shipments.id) as alertSummary,
  ap.uppertemplimit as upperTemperatureLimit,
  ap.lowertemplimit as lowerTemperatureLimit
from shipments
left outer join alertprofiles as ap
  on shipments.alert = ap.id
left outer join devices as d on shipments.device = d.imei
left outer join locationprofiles as sfrom on shipments.shippedfrom = sfrom.id
left outer join locationprofiles as sto on shipments.shippedto = sto.id
