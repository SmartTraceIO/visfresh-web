-- the 6343 shipment ID is used as placeholder for allow to edit given script in MySql editor
-- location from
select
   loc.id as id,
   s.id as shipment,
   true as isStart, 
   false as isStop, 
   false as isInterim,
   loc.address as address,
   loc.companydetails as companyName,
   loc.start as forStart,
   loc.stop as forStop,
   loc.interim as forInterim,
   loc.name as name,
   loc.notes as notes,
   loc.radius as radius,
   loc.latitude as latitude,
   loc.longitude as longitude,
   NULL as altLocId, 
   NULL as stopId,
   NULL as stopDate,
   NULL as stopTime
from shipments s
join locationprofiles loc on s.shippedfrom = loc.id
where s.id = 6343 or  s.siblings = 6343 or s.siblings like concat(6343, ',%')
 or s.siblings like concat('%,', 6343, ',%') or s.siblings like concat('%,', 6343)
union
-- location to
select
   loc.id as id,
   s.id as shipment,
   false as isStart,
   true as isStop,
   false as isInterim,
   loc.address as address,
   loc.companydetails as companyName,
   loc.start as forStart,
   loc.stop as forStop,
   loc.interim as forInterim,
   loc.name as name,
   loc.notes as notes,
   loc.radius as radius,
   loc.latitude as latitude,
   loc.longitude as longitude,
   NULL as altLocId, 
   NULL as stopId,
   NULL as stopDate,
   NULL as stopTime
from shipments s
join locationprofiles loc on s.shippedto = loc.id 
where s.id = 6343 or  s.siblings = 6343 or s.siblings like concat(6343, ',%')
 or s.siblings like concat('%,', 6343, ',%') or s.siblings like concat('%,', 6343)
union
-- alternative locations
select
   loc.id as id,
   s.id as shipment,
   (alt.loctype = 'from') as isStart, 
   (alt.loctype = 'to') as isStop, 
   (alt.loctype = 'interim') as isInterim, 
   alt.shipment as altLocId, 
   loc.address as address,
   loc.companydetails as companyName,
   loc.start as forStart,
   loc.stop as forStop,
   loc.interim as forInterim,
   loc.name as name,
   loc.notes as notes,
   loc.radius as radius,
   loc.latitude as latitude,
   loc.longitude as longitude,
   NULL as stopId,
   NULL as stopDate,
   NULL as stopTime
from shipments s
join locationprofiles loc
join alternativelocations alt on alt.shipment = s.id and alt.location = loc.id
where s.id = 6343 or  s.siblings = 6343 or s.siblings like concat(6343, ',%')
 or s.siblings like concat('%,', 6343, ',%') or s.siblings like concat('%,', 6343)
union
-- interim stops
select
   loc.id as id,
   s.id as shipment,
   false as isStart, 
   false as isStop, 
   false as isInterim, 
   NULL as altLocId, 
   loc.address as address,
   loc.companydetails as companyName,
   loc.start as forStart,
   loc.stop as forStop,
   loc.interim as forInterim,
   loc.name as name,
   loc.notes as notes,
   loc.radius as radius,
   loc.latitude as latitude,
   loc.longitude as longitude,
   stp.id as stopId,
   stp.date as stopDate,
   stp.pause as stopTime
from shipments s
join locationprofiles loc
join interimstops stp on stp.shipment = s.id and stp.location = loc.id
where s.id = 6343 or  s.siblings = 6343 or s.siblings like concat(6343, ',%')
 or s.siblings like concat('%,', 6343, ',%') or s.siblings like concat('%,', 6343)
