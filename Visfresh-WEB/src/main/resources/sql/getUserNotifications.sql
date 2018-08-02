(
select 
notifications.id as id,
notifications.type as type,
notifications.issue as issue,
notifications.user as user,
notifications.isread as isread,
notifications.hidden as hidden,

al.date as issueDate,

s.device as device,
d.model as deviceModel,
s.id as shipmentId,
s.tripcount as shipmentTripCount,
s.description as shipmentDescription,

e.id as trackerEventId,
e.time as eventTime,
e.temperature as readingTemperature,

al.type as alertType,
al.minutes as alertMinutes,
al.cumulative as alertCumulative,
al.temperature as temperature,

rule.timeout as alertRuleTimeOutMinutes,
rule.temp as alertRuleTemperature,

NULL as numberOfMettersOfArrival

from notifications
join alerts al on notifications.user = :user and notifications.type = 'Alert'
   and notifications.issue = al.id
   -- %insert-slert-criterias%
left outer join temperaturerules rule on al.rule = rule.id
join shipments s on s.id = al.shipment
join devices d on s.device = d.imei
left outer join trackerevents e on al.event = e.id
-- %insert-criterias%
)

union

(
select
notifications.id as id,
notifications.type as type,
notifications.issue as issue,
notifications.user as user,
notifications.isread as isread,
notifications.hidden as hidden,

arr.date as issueDate,

s.device as device,
d.model as deviceModel,
s.id as shipmentId,
s.tripcount as shipmentTripCount,
s.description as shipmentDescription,

e.id as trackerEventId,
e.time as eventTime,
e.temperature as readingTemperature,

NULL as alertType,
NULL as alertMinutes,
NULL as alertCumulative,
NULL as temperature,

NULL as alertRuleTimeOutMinutes,
NULL as alertRuleTemperature,

arr.nummeters as numberOfMettersOfArrival

from notifications
join arrivals arr on notifications.user = :user and notifications.type = 'Arrival'
   and notifications.issue = arr.id
join shipments s on s.id = arr.shipment
join devices d on s.device = d.imei
left outer join trackerevents e on arr.event = e.id
-- %insert-criterias%
)
