alter table autostartshipments add column priority int not null default 0;
alter table shipments add column arrivaldate timestamp default '00-01-01 00:00:00';

-- populate arrivaldate
update shipments set arrivaldate = 
(select date from arrivals where arrivals.shipment = shipments.id order by date desc limit 1)
where shipments.id <> -1;
