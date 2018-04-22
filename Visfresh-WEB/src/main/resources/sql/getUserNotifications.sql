select
notifications.*,

al.date as issueDate,

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

NULL as numberOfMettersOfArrival,

lfrom.name as shippedFrom,
lto.name as shippedTo

from notifications
join alerts al on notifications.user = :user and notifications.issue = al.id
left outer join temperaturerules rule on al.rule = rule.id
join shipments s on s.id = al.shipment
left outer join locationprofiles lfrom on lfrom.id = s.shippedfrom
left outer join locationprofiles lto on lto.id = s.shippedto
left outer join trackerevents e on al.event = e.id
-- arrival criterias

union

select
notifications.*,

arr.date as issueDate,

s.device as device,
s.id as shipmentId,
s.tripcount as shipmentTripCount,
s.description as shipmentDescription,

e.id as trackerEventId,
e.time as eventTime,

NULL as alertType,
NULL as alertMinutes,
NULL as alertCumulative,
NULL as temperature,

NULL as alertRuleTimeOutMinutes,
NULL as alertRuleTemperature,

arr.nummeters as numberOfMettersOfArrival,

lfrom.name as shippedFrom,
lto.name as shippedTo

from notifications
join arrivals arr on notifications.user = :user and notifications.issue = arr.id
join shipments s on s.id = arr.shipment
left outer join locationprofiles lfrom on lfrom.id = s.shippedfrom
left outer join locationprofiles lto on lto.id = s.shippedto
left outer join trackerevents e on arr.event = e.id
-- arrival criterias