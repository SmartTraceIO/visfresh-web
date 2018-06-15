select
  shipments.*,
  substring(d.imei, -7, 6) as deviceSN, -- for sorting
  sfrom.name as shippedFromLocationName, -- for sorting
  sto.name as shippedToLocationName, -- for sorting
  -- additional-fields
  ap.uppertemplimit as upperTemperatureLimit,
  ap.lowertemplimit as lowerTemperatureLimit
from shipments
left outer join devices as d on shipments.device = d.imei
left outer join alertprofiles as ap on shipments.alert = ap.id
left outer join devices as nd on shipments.nearestdevice = nd.imei
left outer join locationprofiles as sfrom on shipments.shippedfrom = sfrom.id
left outer join locationprofiles as sto on shipments.shippedto = sto.id
-- additional-joins
