alter table shipments add column lasteventdate timestamp default '00-01-01 00:00:00';

-- attempt to set last event date from tracker events
update shipments s set s.lasteventdate = (
   select max(t.time) from trackerevents t where t.shipment = s.id
)
where not s.istemplate && s.id <> 0; -- id <> 0 for suppress safe mode

-- set lasteventdate from shipment start date if not previously assigned
update shipments set lasteventdate = shipmentdate where not istemplate && shipmentdate is NULL and id <> 0;
