select
notifications.*,

IF(al.id is NULL, arr.date, al.date) as issueDate,

s.device as device,
s.id as shipmentId,
s.tripcount as shipmentTripCount,
s.description as shipmentDescription,

e.id as trackerEventId,
e.time as eventTime,

al.type as alertType,
al.minutes as alertMinutes,
al.cumulative as alertCumulative,
al.temperature as temperature,

rule.timeout as alertRuleTimeOutMinutes,
rule.temp as alertRuleTemperature,

arr.nummeters as numberOfMettersOfArrival,

lfrom.name as shippedFrom,
lto.name as shippedTo

from notifications
left outer join alerts al on notifications.issue = al.id
left outer join temperaturerules rule on al.rule = rule.id
left outer join arrivals arr on notifications.issue = arr.id
join shipments s on s.id = al.shipment or s.id = arr.shipment
left outer join locationprofiles lfrom on lfrom.id = s.shippedfrom
left outer join locationprofiles lto on lto.id = s.shippedto
left outer join trackerevents e on (arr.event = e.id or al.event = e.id)
where notifications.user = :user and (:notUseAlertFilter or al.type is NULL
   or not al.type in ('LightOff', 'LightOn'))    
