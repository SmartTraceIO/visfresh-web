# Visfresh Rest Service

### Date format:###
The date should have following format `yyyy-MM-dd'T'HH:mm` in current user's time zone if the user is logged in now and UTC time zone otherwise. Example:
`2015-09-30T01:19`
### Requests and responses:###
The GET request parameters of URL link should be URL encoded to, but JSON body of request and response should be
sent as is without URL encoding.  
For all POST JSON requests the “Content-Type: application/json” HTTP header should be used.
### Server [Responses](#markdown-header-response-message):###
Each server response has structure:
```json
{
  "status": {
  "code": 0,
  "message": "Success"
},  
"response": {
"token": "token_100001",  
"expired": "2015-09-30T01:19"
}
}  
```
  The `response` element can be absent if there is not any response, but status code should be always in answer.  
0 - status code is a “Success” code  
other code - is an error code. The list of possible error codes will determined in next releases 
In case of error the “response” element of JSON response is absent.  
### Security methods.
An authentication can be performed as from REST client using login method, as from GTSE page (Will implemented in future). If the GTSE authentication has done, then access token can be get by getToken method in some HTTP session. In this case the REST client will attached to REST service using existing GTSE session.
## Data model##
1. [Authentication token](#markdown-header-authentication-token)  
2. [Alert Profile](#markdown-header-alert-profile)  
3. [Temperature Rule](#markdown-header-temperature-rule)  
4. [Notification Schedule](#markdown-header-notification-schedule)  
5. [Location](#markdown-header-location)  
6. [Shipment Template](#markdown-header-shipment-template)  
7. [Device](#markdown-header-device)  
8. [Device group](#markdown-header-device-group)  
9. [Shipment](#markdown-header-shipment)  
10. [Notification](#markdown-header-notification)  
11. [Alert](#markdown-header-alert)  
12. [Temperature Alert](#markdown-header-temperature-alert)  
13. [Arrival](#markdown-header-arrival)  
14. [User](#markdown-header-user)  

## Lists ##
List items is short representations of base entities, like as [Alert Profile](#markdown-header-alert-profile), [Location](#markdown-header-location), etc. Some of fields can be get from corresponding base entity and some can be synthetic fields.  

1. [Shipment Template list item](#markdown-header-shipment-template-list-item) 
2. [Shipment List item](#markdown-header-shipment-list-item)  
3. [Notification Schedule list item](#markdown-header-notification-schedule-list-item)  
4. [User List item](#markdown-header-user-list-item)  
5. [Alert Profile list Item](#markdown-header-list-alert-profile-item)

## Special Request objects ##
1. [Get Shipments filter](#markdown-header-get-shipments-filter)

## Rest Service methods.
1. [Authentication](#markdown-header-authentication).  
2. [Get access token using existing GTS(e) session.](#markdown-header-get-access-token-using-existing-gts-e-session)  
3. [Get User Info](#markdown-header-get-user-info) 
4. [Get Users](#markdown-header-get-users)  
5. [Update User details](#markdown-header-update-user-details)  
6. [Logout](#markdown-header-logout)  
7. [Refresh access token](#markdown-header-refresh-access-token)  
8. [Save alert profile](#markdown-header-save-alert-profile)  
9. [Get Alert Profile](#markdown-header-get-alert-profile)  
10. [Get Alert Profiles](#markdown-header-get-alert-profiles)  
11. [Delete Alert Profile](#markdown-header-delete-alert-profile)  
12. [Save notification schedule](#markdown-header-save-notification-schedule)  
13. [Get notification schedules](#markdown-header-get-notification-schedules)  
14. [Get Notification Schedule](#markdown-header-get-notification-schedule)  
15. [Delete Notification Schedule](#markdown-header-delete-notification-schedule)  
16. [Delete Person Schedule](#markdown-header-delete-person-schedule)  
17. [Save Location](#markdown-header-save-location)  
18. [Get Locations](#markdown-header-get-locations)  
19. [Get Location](#markdown-header-get-location)  
20. [Delete Location](#markdown-header-delete-location)  
21. [Save Shipment Template](#markdown-header-save-shipment-template)  
22. [Get Shipment templates](#markdown-header-get-shipment-templates)  
23. [Get Shipment Template](#markdown-header-get-shipment-template)  
24. [Delete Shipment Template](#markdown-header-delete-shipment-template)  
25. [Save Shipment](#markdown-header-save-shipment)  
26. [Get Shipments](#markdown-header-get-shipments)  
27. [Get Shipment](#markdown-header-get-shipment)  
28. [Get Single Shipment](#markdown-header-get-single-shipment)
29. [Delete Shipment](#markdown-header-delete-shipment)  
30. [Save Device](#markdown-header-save-device)  
31. [Get Device](#markdown-header-get-device)  
32. [Get Devices](#markdown-header-get-devices)  
33. [Delete Device](#markdown-header-delete-device)  
34. [Save Device Group](#markdown-header-save-device-group)  
35. [Get Device Group](#markdown-header-get-device-group)  
36. [Get Device Groups](#markdown-header-get-device-groups)  
37. [Delete Device Group](#markdown-header-delete-device-group)  
38. [Add Device to Group](#markdown-header-add-device-to-group)  
39. [Remove Device from Group](#markdown-header-remove-device-from-group)  
40. [Get Devices of Group](#markdown-header-get-devices-of-group)    
41. [Get Groups of Device](#markdown-header-get-groups-of-device)  
42. [Get Notifications](#markdown-header-get-notifications)  
43. [Send Command to Device](#markdown-header-send-command-to-device)  
44. [Mark Notification as read](#markdown-header-mark-notification-as-read)  

### Authentication.###
Method *GET*, method name *login*, request parameters login - the user login name and password - the user password  
1. email - email of logging in user  
2. password - password  
Returns [Authentication token](#markdown-header-authentication-token).  
[(example)](#markdown-header-authentication-request-example)  

### Get access token using existing GTS(e) session.###
The user should be logged in to GTS(e). (not implemented now).
Method *POST*, method name *getToken*, no parameters. In case of this request the service access a current user session, determines user info, log in as REST service user and returns authentication session.  
[(example)](#markdown-header-attach-to-existing-session-example)

### Get User Info ###
Method *GET*, method name *getUser*, method parameters  
1. userId - ID of user  

Method required associated privileges. The logged in user should be some as requested info user or should have admin role.  
Method returns [User Object](#markdown-header-user)   
[(example)](#markdown-header-get-user-info-example)

### Get Users ###
Method *GET*, method name *getUsers*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
3. sc - sort column  
4. so - sort order  
Method returns array of [User List items](#markdown-header-user-list-item)  
[(example)](#markdown-header-get-users-example)

### Update User Details ###
Method *POST*, method name *updateUserDetails*. JSON request body contains following properties:  
1. user - user ID.  
2. firstName - first user name.  
3. lastName - last user name.  
4. position - position of user in company.  
5. email - user email address.  
6. phone - user phone number.  
7. temperatureUnits - temperature units.  
8. measurementUnits - measurement units Metric/English  
9. password - user password.  
10. user - user login name. It is not changeable parameter. Is used for identify the user to change details.  
11. temperatureUnits - user temperature units.  
12. timeZone - user time zone.  
13. language - user language.  
14. scale - user schale.  
15. title - user title.  
[(example)](#markdown-header-update-user-details-example)

### Logout ###
Method *GET*, method name *logout*, have not parameters. Closes user REST session and clears all associated resources  
[(example)](#markdown-header-logout-example)

### Refresh access token ###
Method *GET*, method name *refreshToken*, have not parameters. Refresh the access token for current REST session.  
[(example)](#markdown-header-refresh-access-token)

### Get Alert Profiles ###
Method *GET*, method name *getAlertProfiles*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
3. sc - sort column  
4. so - sort order (asc/desc)  
Returns an array of [Alert Profile objects](#markdown-header-alert-profile) and total items count.  
[(example)](#markdown-header-get-alert-profiles-example)

### Get Alert Profile ###
Method *GET*, method name getAlertProfile. Request parameters:  
1. alertProfileId - alert profile ID.  
Returns [Alert Profile Object](#markdown-header-alert-profile).  
[(example)](#markdown-header-get-alert-profile-example)

### Save alert profile ###
Method *POST*, method name *saveAlertProfile*, request body contains JSON serialized [Alert Profile object](#markdown-header-alert-profile). Response contains ID of just saved Alert Profile.  
[(example)](#markdown-header-save-alert-profile-example)

### Delete Alert Profile ###
Method *GET*, method name *deleteAlertProfile*, method parameters:  
1. alertProfileId - alert profile ID  
[(example)](#markdown-header-delete-alert-profile-example)

### Save Notification Schedule ###
Method *POST*, method name *saveNotificationSchedule*, request body contains JSON serialized [Notification Schedule object](#markdown-header-notification-schedule). Response contains ID of just saved Notification Schedule.  
[(example)](#markdown-header-save-notification-schedule-example)]

### Get Notification Schedules ###
Method *GET*, method name *getNotificationSchedules*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
3. sc - sort column  
4. so - sort order (asc/desc)  
Return array of [Notification Schedule list item](#markdown-header-notification-schedule-list-item)
and total items count  
[(example)](#markdown-header-get-notification-schedules-example)

### Get Notification Schedule ###
Method *GET*, method name *getNotificationSchedule*. Request parameters:  
1. notificationScheduleId - notification schedule ID.  
Returns [Notification Schedule Object](#markdown-header-notification-schedule)  
[(example)](#markdown-header-get-notification-schedule-example)

### Delete Notification schedule ###
Method *GET*, method name *deleteNotificationSchedule*. Request parameters:  
1. notificationScheduleId - notification schedule ID.  
[(example)](#markdown-header-delete-notification-schedule-example)

### Delete Person Schedule ###
Method *GET*, method name *deletePersonSchedule*. Request parameters:  
1. notificationScheduleId  
2. personScheduleId  
[(example)](#markdown-header-delete-person-schedule-example)

### Save Location ###
Method *POST*, method name *saveLocation*, request body contains JSON serialized [Location Object](#markdown-header-location). Response contains ID of just saved Location  
[(example)](#markdown-header-save-location-example)

### Get Locations ###
Method *GET*, method name *getLocations*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
3. sc - sort column  
4. so - sort order  
Returns array of [Location Objects](#markdown-header-location) and total items count  
[(example)](#markdown-header-get-locations-example)

### Get Location ###
Method *GET*, method name *getLocation*. Request parameters:  
1. locationId - Location ID.  
Returns [Location Object](#markdown-header-location).  
[(example)](#markdown-header-get-location-example)

### Delete Location ###
Method *GET*, method name *deleteLocation*. Request parameters:  
1. locationId - Location ID.  
[(example)](#markdown-header-delete-location-example)

### Save Shipment template ###
Method *POST*, method name *saveShipmentTemplate*, request body contains JSON serialized [Shipment Template Object](#markdown-header-shipment-template). Response contains ID of just saved Shipment Template  
[(example)](#markdown-header-save-shipment-template-example)

### Get Shipment templates ###
Method *GET*, method name *getShipmentTemplates*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Shipment Template List item](#markdown-header-shipment-template-list-item)
and total items count  
[(example)](#markdown-header-get-shipment-templates-example)

### Get Shipment Template ###
Method *GET*, method name *getShipmentTemplate*. Request parameters:  
1. id - shipment template ID.  
Returns [Shipment Template Object](#markdown-header-shipment-template)  
[(example)](#markdown-header-get-shipment-template-example)

### Delete Shipment Template ###
Method *GET*, method name *deleteShipmentTemplate*, Request parameters:  
1. shipmentTemplateId - shipment template ID.  
[(example)](#markdown-header-delete-shipment-template-example)

### Save Device ###
Method *POST*, method name *saveDevice*, request body contains JSON serialized [Device Object](#markdown-header-device).  
[(example)](#markdown-header-save-device-example)

### Get Devices ###
Method *GET*, method name *getDevices*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Device Objects](#markdown-header-device) and total items count.  
[(example)](#markdown-header-get-devices-example)

### Get Device ###
Method *GET*, method name *getDevice*. Request parameters:
1. imei - device IMEI.  
Returns [Device Object](#markdown-header-device)  
[(example)](#markdown-header-get-device-example)

### Delete Device ###
Method *GET*, method name *deleteDevice*. Request parameters:
1. imei - device IMEI.  
[(example)](#markdown-header-delete-device-example)

### Save Device Group ###
Method *POST*, method name *saveDeviceGroup*. Request body contains JSON serialized [Device Group Object](#markdown-header-device-group)  
[(example)](#markdown-header-save-device-group-example)

### Get Device Group ###
Method *GET*, method name *getDeviceGroup*. Method parameters:  
1. name - group name.  
Returns [Device Group object](#markdown-header-device-group)  
[(example)](#markdown-header-get-device-group-example)

### Get Device Groups ###
Method *GET*, method name *getDeviceGroups*. Method parameters:  
1. pageIndex - page index  
2. pageSize - page size  
Returns list of [Device Group object](#markdown-header-device-group)  
[(example)](#markdown-header-get-device-groups-example)  

### Delete Device Group ###
Method *GET*, method name *deleteDeviceGroup*. Method parameters:  
1. name - group name  
[(example)](#markdown-header-delete-device-group-example)

### Add Device to Group ###
Method *GET*, method name *addDeviceToGroup*. Method parameters:  
1. groupName - group name.  
2. device - device IMEI.  
[(example)](add-device-to-group-example)

### Remove Device from Group ###
Method *GET*, method name *removeDeviceFromGroup*. Method parameters:  
1. groupName - group name.  
2. device - device IMEI.  
[(example)](remove-device-from-group-example)

### Get Devices of Group ###
Method *GET*, method name *getDevicesOfGroup*. Method parameters:  
1. groupName - name of group  
Returns array of [Device Objects](#markdown-header-device)  
[(example)](get-devices-of-group-example)

### Get Groups of Device ###  
Method *GET*, method name *getGroupsOfDevice*. Method parameters:  
1. device - device IMEI code  
Returns array of [Device Group Objects](#markdown-header-device-group)  
[(example)](#markdown-header-get-groups-of-device-example)

### Save Shipment ###
Method *POST*, method name saveShipment, request body contains JSON serialized [Save Shipment request](#markdown-header-save-shipment-request). Response contains ID of just saved Shipment and ID of shipment template if the shipment was saved with corresponding option.  
[(example)](#markdown-header-save-shipment-example)

### Get Shipment ###
Method *GET*, method name *getShipment*. Request parameters:  
1. id - shipment ID.  
Returns [Shipment Object](#markdown-header-shipment)  
[(example)](#markdown-header-get-shipment-example)

### Get Shipments ###
Method *POST*, method name getShipments, request body [Get Shipments filter](#markdown-header-get-shipments-filter)  
Returns array of [Shipment List items](#markdown-header-shipment-list-item) and total items count,
it is not same as [Shipment Object](#markdown-header-shipment).  
[(example)](#markdown-header-get-shipments-example)

### Delete Shipment ###
Method *GET*, method name deleteShipment, method parameters:  
1. shipmentId - shipment ID  
[(example)](#markdown-header-delete-shipment-example)

### Get Single Shipment ###
Method *GET*, method *getSingleShipment*. Request parameters:  
1 fromDate start selection data  
2. toDate end selection data  
3. shipment shipment ID  
[(example)](#markdown-header-get-single-shipment-example)

### Get Notifications ###
Method *GET*, method name getNotifications, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Notification Objects](#markdown-header-notification) and total items count  
[(example)](#markdown-header-get-notifications-example)

### Mark Notification as read ###
Method *POST*, method name *markNotificationsAsRead*. Request body contains JSON array of notification ID.  
[(example)](#markdown-header-mark-notification-as-read-example)

### Send Command to Device ###
Method *POST*, method name *sendCommandToDevice*. Request body contains [Device](#markdown-header-device) ID and device specific command.  
[(example)](#markdown-header-send-command-to-device-example)

## Objects
### Response message ###
```json
{
  "status": { //response status
    "code": 0,
    "message": "Success"
  },
  "response": {
    "token": "1401890001-e4adc2304877e138edf5e33a3e584c35",
    "expired": "2016-01-17T00:35"
  }
  //additional response information. I.e. totalCount for lists
}
```
see [ResponseStatus](#markdown-header-response-status)
### Response status ###
```json
{
  "code": 0, // 0 - success, error for other codes
  "message": "Success"
}
```
### Authentication token ###
```json
{
    "token": "701167453-8c73a57deb6903f712dd2359264973ec", // authentication token
    "expired": "2016-01-17T00:35" // expiration time
}
```
### Alert Profile ###
```json
{
	"alertProfileId": 182,
	"alertProfileName": "AnyAlert",
	"alertProfileDescription": "Any description",
	"watchBatteryLow": true,
	"watchEnterBrightEnvironment": true,
	"watchEnterDarkEnvironment": true,
	"watchMovementStart": true,
	"watchMovementStop": true,
	"temperatureIssues": [/* Array of temperature rules */]
}
```
[(See Temperature Rule)](#markdown-header-temperature-rule)
### Temperature Rule ###
```json
{
  "id": 1007,
  "type": "CriticalHot",
  "temperature": 17.0,
  "timeOutMinutes": 1,
  "cumulativeFlag": true
}
```
### List Alert Profile Item ###
```json
{
  "alertProfileId": 848,
  "alertProfileName": "Name",
  "alertProfileDescription": "Description",
  "alertRuleList": [
    ">18,0°C for 0 min in total",
    ">17,0°C for 1 min in total",
    "<-12,0°C for 0 min in total",
    "<-11,0°C for 1 min in total",
    ">6,0°C for 0 min in total",
    ">7,0°C for 2 min in total",
    "<-7,0°C for 40 min in total",
    "<-5,0°C for 55 min in total"
  ]
}
```
### Notification Schedule ###
```json
{
    "notificationScheduleId": 11,
    "notificationScheduleName": "Sched",
    "notificationScheduleDescription": "JUnit schedule",
    "schedules": [
       //array of embedded person schedules
    ]
}
```
[(See Person Schedule)](#markdown-header-person-schedule)
### Person Schedule ###
```json
{
    "personScheduleId": 2165,
    "user": 77, // user ID
    "sendApp": true,
    "sendEmail": false,
    "sendSms": false,
    "fromTime": "13:20",
    "toTime": "20:00",
    "weekDays": [
      true,
      false,
      false,
      true,
      false,
      false,
      false
    ]
}
```  
[(See User)](#markdown-header-user)
### Location ###
```json
{
  "locationId": 11,
  "locationName": "Loc-1",
  "companyName": "Sun Microsystems",
  "notes": "Any notes",
  "address": "Bankstown Warehouse",
  "location": {
    "lat": 100.5,
    "lon": 100.501
  },
  "radiusMeters": 1000,
  "startFlag": "Y",
  "interimFlag": "Y",
  "endFlag": "Y"
}
```
### Shipment Template ###
```json
{
    "shipmentTemplateId": 88,
    "shipmentTemplateName": "JUnit-tpl",
    "shipmentDescription": "Any Description",
    "addDateShipped": true,
    "shippedFrom": 86,
    "shippedTo": 87,
    "detectLocationForShippedFrom": true,
    "useCurrentTimeForDateShipped": true,
    "alertProfileId": 79,
    "alertSuppressionMinutes": 55,
    "maxTimesAlertFires": 4,
    "alertsNotificationSchedules": [
      80
    ],
    "commentsForReceiver": "Comments for receiver"
    "arrivalNotificationWithinKm": 11,
    "excludeNotificationsIfNoAlerts": true,
    "arrivalNotificationSchedules": [
      83
    ],
    "shutdownDeviceAfterMinutes": 155,
}
```  
### Device ###
```json
{
  "description": "Device description",
  "imei": "0239487043987",
  "name": "Device Name",
  "sn": "043987"
}
```
### Device Group ###
```json
{
  "name": "G2",
  "description": "JUnit device group"
}
```
### Save Shipment request ###
```json
{
  "saveAsNewTemplate": true,
  "templateName": "NewTemplate.tpl", // template name in case of save also as new template
  "shipment": ${EmbeddedShipmentObject} //this shipment object to save
}
```
see [Shipment Object](#markdown-header-shipment)
### Shipment ###
```json
{
    "status": "InProgress",
    "deviceImei": "234908720394857",
    "deviceSN": "394857",
    "deviceName": "Device Name",
    "tripCount": 88,
    "shipmentId": 120,
    "shipmentDescription": "Any Description",
    "palletId": "palettid",
    "poNum": 893793487,
    "assetNum": "10515",
    "assetType": "SeaContainer",
    "shippedFrom": 118,
    "shippedTo": 119,
    "shipmentDate": "2015-10-25T21:58",
    "alertProfileId": 111,
    "alertSuppressionMinutes": 55,
    "maxTimesAlertFires": 4,
    "alertsNotificationSchedules": [
      112
    ],
    "commentsForReceiver": "Comments for receiver",
    "arrivalNotificationWithinKm": 111,
    "excludeNotificationsIfNoAlerts": true,
    "arrivalNotificationSchedules": [
      115
    ],
    "shutdownDeviceAfterMinutes": 155,
    "customFields": {
      "field1": "value1"
    }
}
```
### Shipment List Item ###
```json
{
  "status": "InProgress",
  "deviceSN": "234908720394857",
  "deviceName": "Device Name",
  "tripCount": 88,
  "shipmentId": 11,
  "shipmentDescription": "Any Description",
  "shipmentDate": "2015-10-25T22:05",
  "palletId": "palettid",
  "assetNum": "10515",
  "assetType": "SeaContainer",
  "shippedFrom": "Bankstown Warehouse",
  "shippedTo": "Coles Perth DC",
  "estArrivalDate": "2015-10-25T22:05",
  "percentageComplete": 0,
  "alertProfileId": 2,
  "alertProfileName": "AnyAlert",
  "alertSummary": {
    "Hot": "2",
    "Battery": "2",
    "CriticalCold": "3",
    "LightOn": "1",
    "LightOff": "1",
    "MovementStart": "4",
    "Cold": "1",
    "CriticalHot": "1"
  }
}
```
See also [Shipment Object](#markdown-header-shipment)
### Notification ###
```json
{
  "id": 29,
  "type": "Alert",
  "issue": { // Notification issue Temperature Alert|Other Alert|Arrival
    "id": 28,
    "date": "2015-11-05T13:57",
    "device": "234908720394857",
    "shipment": 27,
    "type": "Hot",
    "temperature": 5.0,
    "minutes": 55
  }
}
```  
see [Ordinary Alert Object](#markdown-header-alert), [Temperature Alert Object](#markdown-header-temperature-alert), [Arrival Object](#markdown-header-arrival)
### Alert ###
```json
{
	"id": 14,
	"date": "2015-11-05T13:57",
	"device": "234908720394857",
	"shipment": 11,
	"type": "Battery"  // alert type: (EnterBrightEnvironment|EnterDarkEnvironment|Shock|BatteryLow)
}
```
### Temperature Alert###
```json
{
    "id": 28,
    "date": "2015-11-05T13:57",
    "device": "234908720394857",
    "shipment": 27,
    "type": "Hot", // alert type (LowTemperature|HighTemperature|CriticalLowTemperature|CriticalHighTemperature)
    "temperature": 5.0,
    "minutes": 55
}
```
### Arrival ###
```json
{
	"id": 16,
	"numberOfMetersOfArrival": 1500,
	"date": "2015-11-05T13:57",
	"device": "234908720394857",
	"shipment": 11
}
```
### User ###
```json
{
    "login": "test-1",
    "firstName": "firstname",
    "lastName": "LastName",
    "position": "Manager",
    "email": "abra@cada.bra",
    "phone": "1111111117",
    "roles": [
      "CompanyAdmin",
      "Dispatcher"
    ],
    "timeZone": "UTC",
    "temperatureUnits": "Celsius"
}
```
## List Items ##
### Shipment Template List item ###
```json
{
  "shipmentTemplateId": 58,
  "shipmentTemplateName": "JUnit-tpl",
  "shipmentDescription": "Any Description",
  "shippedFrom": 56,
  "shippedFromLocationName": "Loc-1",
  "shippedTo": 57,
  "shippedToLocationName": "Loc-2",
  "alertProfile": 49,
  "alertProfileName": "AnyAlert"
}
```
### Notification Schedule List item ###
```json
{
  "notificationScheduleId": 5,
  "notificationScheduleName": "Sched",
  "notificationScheduleDescription": "JUnit schedule",
  "peopleToNotify": "Alexander Suvorov, Mikhael Kutuzov"
}
```
### User List item ###
```json
{
  "id": 289,
  "fullName": "A1 LastA1"
}
```
## JSON Requests ##
### Get Shipments filter ###
```json
{
  "alertsOnly": false,
  "deviceImei": "234908720394857",
  "lastMonth": true,
  "shipmentDescription": "Any Description",
  "shippedFrom": [], //array of location ID
  "shippedTo": [], //array of location ID
  "status": "InProgress",
  "pageIndex": 1,
  "pageSize": 10000
}
```
## Examples ##
### Authentication request example ###
**GET /vf/rest/login?email=u1%40google.com&password=password**   
**Response**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "token": "7171890-f481f2909adab6cf92a40661b3bab429",
    "expired": "2016-01-17T11:32"
  }
}
```
### Attach to existing session example ###
**GET /vf/rest/getToken**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "token": "7171890-f481f2909adab6cf92a40661b3bab429",
    "expired": "2016-01-17T11:32"
  }
}
```
### Get user info example ###
**GET /vf/rest/getUser/${authToken}?userId=290**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "id": 290,
    "email": "a@b.c",
    "roles": [
      "GlobalAdmin"
    ],
    "timeZone": "UTC",
    "temperatureUnits": "Celsius",
    "measurementUnits": "Metric",
    "language": "English"
    "deviceGroup": "DeviceGroupName",
    "scale": "scale",
    "title": "Mrs"
  }
}
```
### Get Users example ###
**GET /vf/rest/getUsers/${authToken}?so=asc&pageSize=1&sc=login&pageIndex=1**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "id": 288,
      "fullName": "A2 LastA2"
    },
    {
      "id": 289,
      "fullName": "A1 LastA1"
    }
  ],
  "totalCount": 2
}
```
### Update User Details example ###
**POST /vf/rest/updateUserDetails/${authToken}**  
**Request body:**  
```json
{
  "user": 293,
  "password": "abrakadabra",
  "firstName": "firstname",
  "lastName": "LastName",
  "position": "Manager",
  "email": "abra@cada.bra",
  "phone": "1111111117",
  "temperatureUnits": "Fahrenheit",
  "timeZone": "GMT+02:00",
  "measurementUnits": "English",
  "language": "English",
  "scale": "scale",
  "title": "Developer"
}
```
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Logout example ###
**GET /vf/rest/logout/${authToken}**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```  
### Refresh access token ###
**GET /vf/rest/refreshToken/${authToken}**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "token": "7171890-f481f2909adab6cf92a40661b3bab429",
    "expired": "2016-01-17T11:32"
  }
}
```
### Save alert profile example ###
**POST /vf/rest/saveAlertProfile/${authToken}**  
**Request body:**  
```json
{
  "alertProfileName": "AnyAlert",
  "alertProfileDescription": "Any description",
  "watchBatteryLow": true,
  "watchEnterBrightEnvironment": true,
  "watchEnterDarkEnvironment": true,
  "watchMovementStart": true,
  "watchMovementStop": true,
  "temperatureIssues": [
    {
      "type": "CriticalHot",
      "temperature": 18.0,
      "timeOutMinutes": 0,
      "cumulativeFlag": true
    },
    {
      "type": "CriticalHot",
      "temperature": 17.0,
      "timeOutMinutes": 1,
      "cumulativeFlag": true
    },
    {
      "type": "CriticalCold",
      "temperature": -12.0,
      "timeOutMinutes": 0,
      "cumulativeFlag": true
    },
    {
      "type": "CriticalCold",
      "temperature": -11.0,
      "timeOutMinutes": 1,
      "cumulativeFlag": false
    },
    {
      "type": "Hot",
      "temperature": 6.0,
      "timeOutMinutes": 0,
      "cumulativeFlag": false
    },
    {
      "type": "Hot",
      "temperature": 7.0,
      "timeOutMinutes": 2,
      "cumulativeFlag": true
    },
    {
      "type": "Cold",
      "temperature": -7.0,
      "timeOutMinutes": 40,
      "cumulativeFlag": true
    },
    {
      "type": "Cold",
      "temperature": -5.0,
      "timeOutMinutes": 55,
      "cumulativeFlag": true
    }
  ]
}
```
**Response:** 
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "alertProfileId": 6
  }
}
```
### Get Alert Profile example ###
**GET /vf/rest/getAlertProfile/${accessToken}?alertProfileId=5**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "alertProfileId": 182,
    "alertProfileName": "AnyAlert",
    "alertProfileDescription": "Any description",
    "watchBatteryLow": true,
    "watchEnterBrightEnvironment": true,
    "watchEnterDarkEnvironment": true,
    "watchMovementStart": true,
    "watchMovementStop": true,
    "temperatureIssues": [
      {
        "id": 982,
        "type": "CriticalHot",
        "temperature": 18.0,
        "timeOutMinutes": 0,
        "cumulativeFlag": true
      },
      {
        "id": 983,
        "type": "CriticalHot",
        "temperature": 17.0,
        "timeOutMinutes": 1,
        "cumulativeFlag": true
      },
      {
        "id": 984,
        "type": "CriticalCold",
        "temperature": -12.0,
        "timeOutMinutes": 0,
        "cumulativeFlag": true
      },
      {
        "id": 985,
        "type": "CriticalCold",
        "temperature": -11.0,
        "timeOutMinutes": 1,
        "cumulativeFlag": true
      },
      {
        "id": 986,
        "type": "Hot",
        "temperature": 6.0,
        "timeOutMinutes": 0,
        "cumulativeFlag": true
      },
      {
        "id": 987,
        "type": "Hot",
        "temperature": 7.0,
        "timeOutMinutes": 2,
        "cumulativeFlag": true
      },
      {
        "id": 988,
        "type": "Cold",
        "temperature": -7.0,
        "timeOutMinutes": 40,
        "cumulativeFlag": true
      },
      {
        "id": 989,
        "type": "Cold",
        "temperature": -5.0,
        "timeOutMinutes": 55,
        "cumulativeFlag": true
      }
    ]
  }
}
```
### Get Alert Profiles example ###
**GET /vf/rest/getAlertProfiles/${accessToken}?so=desc&pageSize=10000&sc=alertProfileDescription&pageIndex=1**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "alertProfileId": 846,
      "alertProfileName": "b",
      "alertProfileDescription": "c",
      "alertRuleList": [
        ">18,0°C for 0 min in total",
        ">17,0°C for 1 min in total",
        "<-12,0°C for 0 min in total",
        "<-11,0°C for 1 min in total",
        ">6,0°C for 0 min in total",
        ">7,0°C for 2 min in total",
        "<-7,0°C for 40 min in total",
        "<-5,0°C for 55 min in total"
      ]
    },
    {
      "alertProfileId": 847,
      "alertProfileName": "a",
      "alertProfileDescription": "b",
      "alertRuleList": [
        ">18,0°C for 0 min in total",
        ">17,0°C for 1 min in total",
        "<-12,0°C for 0 min in total",
        "<-11,0°C for 1 min in total",
        ">6,0°C for 0 min in total",
        ">7,0°C for 2 min in total",
        "<-7,0°C for 40 min in total",
        "<-5,0°C for 55 min in total"
      ]
    },
    {
      "alertProfileId": 848,
      "alertProfileName": "c",
      "alertProfileDescription": "a",
      "alertRuleList": [
        ">18,0°C for 0 min in total",
        ">17,0°C for 1 min in total",
        "<-12,0°C for 0 min in total",
        "<-11,0°C for 1 min in total",
        ">6,0°C for 0 min in total",
        ">7,0°C for 2 min in total",
        "<-7,0°C for 40 min in total",
        "<-5,0°C for 55 min in total"
      ]
    }
  ],
  "totalCount": 3
}
```
### Delete Alert Profile example ###
**GET vf/rest/deleteAlertProfile/${accessToken}?alertProfileId=4**  
**Response:**
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```

### Save Notification Schedule example ###
**POST /vf/rest/saveNotificationSchedule/${accessToken}**  
**Request body:**  
```json
{
  "notificationScheduleDescription": "JUnit schedule",
  "notificationScheduleName": "Sched",
  "schedules": [
    {
      "user": 1,
      "sendApp": true,
      "sendEmail": false,
      "sendSms": false,
      "fromTime": "13:20",
      "toTime": "20:00",
      "weekDays": [
        true,
        false,
        false,
        true,
        false,
        false,
        false
      ]
    },
    {
      "user": 2,
      "sendApp": true,
      "sendEmail": false,
      "sendSms": false,
      "fromTime": "13:20",
      "toTime": "20:00",
      "weekDays": [
        true,
        false,
        false,
        true,
        false,
        false,
        false
      ]
    }
  ]
}
```
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "notificationScheduleId": 2
  }
}
```  
### Get Notification Schedules example ###
**GET /vf/rest/getNotificationSchedules/${accessToken}?so=desc&pageSize=10000&sc=notificationScheduleDescription&pageIndex=1**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "notificationScheduleId": 2,
      "notificationScheduleName": "Sched",
      "notificationScheduleDescription": "JUnit schedule",
      "peopleToNotify": "Alexander Suvorov, Alexander Suvorov"
    },
    {
      "notificationScheduleId": 5,
      "notificationScheduleName": "Sched",
      "notificationScheduleDescription": "JUnit schedule",
      "peopleToNotify": "Alexander Suvorov, Alexander Suvorov"
    }
  ],
  "totalCount": 2
}
```  
### Get Notification Schedule example ###
**GET /vf/rest/getNotificationSchedule/${accessToken}?notificationScheduleId=77**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "notificationScheduleDescription": "JUnit schedule",
    "notificationScheduleId": 1289,
    "notificationScheduleName": "Sched",
    "schedules": [
      {
        "personScheduleId": 2157,
        "user": 3,
        "sendApp": true,
        "sendEmail": false,
        "sendSms": false,
        "fromTime": "13:20",
        "toTime": "20:00",
        "weekDays": [
          true,
          false,
          false,
          true,
          false,
          false,
          false
        ]
      },
      {
        "personScheduleId": 2158,
        "user": 4,
        "sendApp": true,
        "sendEmail": false,
        "sendSms": false,
        "fromTime": "13:20",
        "toTime": "20:00",
        "weekDays": [
          true,
          false,
          false,
          true,
          false,
          false,
          false
        ]
      }
    ]
  }
}
```
### Save Location example ###
**POST vf/rest/saveLocation/${accessToken}**  
**Request body:**  
```json
{
  "locationName": "Loc-1",
  "companyName": "Sun Microsystems",
  "notes": "Any notes",
  "address": "Bankstown Warehouse",
  "location": {
    "lat": 100.5,
    "lon": 100.501
  },
  "radiusMeters": 1000,
  "startFlag": "Y",
  "interimFlag": "Y",
  "endFlag": "Y"
}
```
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "locationId": 2
  }
}
```
### Get Locations example ###
**GET /vf/rest/getLocations/${accessToken}?so=desc&pageSize=10000&sc=notes&pageIndex=1**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "locationId": 2,
      "locationName": "Loc-1",
      "companyName": "Sun Microsystems",
      "notes": "Any notes",
      "address": "Bankstown Warehouse",
      "location": {
        "lat": 100.5,
        "lon": 100.501
      },
      "radiusMeters": 1000,
      "startFlag": "Y",
      "interimFlag": "Y",
      "endFlag": "Y"
    }
  ],
  "totalCount": 1
}
```  
### Save Shipment Template example ###
**POST /vf/rest/saveShipmentTemplate/${accessToken}**  
**Request body:**  
```json
{
  "shipmentTemplateName": "JUnit-tpl",
  "shipmentDescription": "Any Description",
  "addDateShipped": true,
  "shippedFrom": 45,
  "shippedTo": 46,
  "detectLocationForShippedFrom": true,
  "useCurrentTimeForDateShipped": true,
  "alertProfileId": 38,
  "alertSuppressionMinutes": 55,
  "maxTimesAlertFires": 4,
  "alertsNotificationSchedules": [
    39
  ],
  "commentsForReceiver": "Comments for receiver"
  "arrivalNotificationWithinKm": 11,
  "excludeNotificationsIfNoAlerts": true,
  "arrivalNotificationSchedules": [
    42
  ],
  "shutdownDeviceAfterMinutes": 155,
}
```  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "shipmentTemplateId": 48
  }
}
``` 
### Get Shipment Templates example ###
**GET /vf/rest/getShipmentTemplates/${accessToken}?pageSize=1&pageIndex=3**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "shipmentTemplateId": 58,
      "shipmentTemplateName": "JUnit-tpl",
      "shipmentDescription": "Any Description",
      "shippedFrom": 56,
      "shippedFromLocationName": "Loc-1",
      "shippedTo": 57,
      "shippedToLocationName": "Loc-2",
      "alertProfile": 49,
      "alertProfileName": "AnyAlert"
    },
    {
      "shipmentTemplateId": 68,
      "shipmentTemplateName": "JUnit-tpl",
      "shipmentDescription": "Any Description",
      "shippedFrom": 66,
      "shippedFromLocationName": "Loc-3",
      "shippedTo": 67,
      "shippedToLocationName": "Loc-4",
      "alertProfile": 59,
      "alertProfileName": "AnyAlert"
    }
  ],
  "totalCount": 2
}
```
### Delete Shipment Template example ###
**GET /vf/rest/deleteShipmentTemplate/${accessToken}?shipmentTemplateId=78**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Save Shipment example ###
**POST  /vf/rest/saveShipment/${accessToken}**  
**Request body:**  
```json
{
  "saveAsNewTemplate": true,
  "templateName": "NewTemplate.tpl",
  "shipment": {
    "status": "InProgress",
    "deviceImei": "234908720394857",
    "tripCount": 88,
    "shipmentDescription": "Any Description",
    "palletId": "palettid",
    "poNum": 893793487,
    "assetNum": "10515",
    "assetType": "SeaContainer",
    "shippedFrom": 96,
    "shippedTo": 97,
    "shipmentDate": "2015-10-25T23:58",
    "alertProfileId": 89,
    "alertSuppressionMinutes": 55,
    "maxTimesAlertFires": 4,
    "alertsNotificationSchedules": [
      90
    ],
    "commentsForReceiver": "Comments for receiver",
    "arrivalNotificationWithinKm": 111,
    "excludeNotificationsIfNoAlerts": true,
    "arrivalNotificationSchedules": [
      93
    ],
    "shutdownDeviceAfterMinutes": 155,
    "customFields": {
      "field1": "value1"
    }
  }
}
```
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "shipmentId": 11,
    "templateId": 12
  }
}
```
### Get Shipments example ###
**POST /vf/rest/getShipments/${accessToken}**  
```json
{
  "alertsOnly": false,
  "deviceImei": "234908720394857",
  "shipmentDescription": "Any Description",
  "shippedFrom": [
    323,
    1,
    2,
    3,
    4
  ],
  "shippedTo": [
    324,
    1,
    2,
    3,
    4
  ],
  "status": "InProgress",
  "pageIndex": 1,
  "pageSize": 10000
}
```
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "status": "InProgress",
      "deviceSN": "234908720394857",
      "deviceName": "Device Name",
      "tripCount": 88,
      "shipmentId": 11,
      "shipmentDescription": "Any Description",
      "shipmentDate": "2015-10-25T22:05",
      "palletId": "palettid",
      "assetNum": "10515",
      "assetType": "SeaContainer",
      "shippedFrom": "Bankstown Warehouse",
      "shippedTo": "Coles Perth DC",
      "estArrivalDate": "2015-10-25T22:05",
      "percentageComplete": 0,
      "alertProfileId": 2,
      "alertProfileName": "AnyAlert",
      "alertSummary": {
        "Hot": "2",
        "Battery": "2",
        "CriticalCold": "3",
        "LightOn": "1",
        "LightOff": "1",
        "MovementStart": "4",
        "Cold": "1",
        "CriticalHot": "1"
      }
    },
    {
      "status": "InProgress",
      "deviceSN": "234908720394857",
      "deviceName": "Device Name",
      "tripCount": 88,
      "shipmentId": 21,
      "shipmentDescription": "Any Description",
      "shipmentDate": "2015-10-25T22:05",
      "palletId": "palettid",
      "assetNum": "10515",
      "assetType": "SeaContainer",
      "shippedFrom": "Bankstown Warehouse",
      "shippedTo": "Coles Perth DC",
      "estArrivalDate": "2015-10-25T22:05",
      "percentageComplete": 0,
      "alertProfileId": 12,
      "alertProfileName": "AnyAlert",
      "alertSummary": {}
    }
  ],
  "totalCount": 2
}
```
### Delete Shipment example ###
**GET /vf/rest/deleteShipment/${accessToken}?shipmentId=110**  
** Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Save Device example ###
**POST /vf/rest/saveDevice/${accessToken}**  
**Request body:**  
```json
{
  "description": "Device description",
  "imei": "0239487043987",
  "name": "Device Name",
  "sn": "043987"
}
```  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```  
### Get Devices example ###
**GET /vf/rest/getDevices/${accessToken}?pageSize=10000&pageIndex=1**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "description": "Device description",
      "imei": "0239487043222",
      "name": "Device Name",
      "sn": "043222"
    },
    {
      "description": "Device description",
      "imei": "0239487043987",
      "name": "Device Name",
      "sn": "043987"
    }
  ],
  "totalCount": 2
}
```
### Delete Device example ##
**GET /vf/rest/deleteDevice/${accessToken}?imei=0239487043987**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Save Device Group example ###
**POST /vf/rest/saveDeviceGroup/${accessToken}**  
**Request:**  
```json
{
  "name": "JUnit",
  "description": "JUnit device group"
}
```  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Get Device Group example ###
**GET /vf/rest/getDeviceGroup/${accessToken}?name=G1**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "name": "G1",
    "description": "JUnit device group"
  }
}
```
### Get Device Groups example ###
**GET /vf/rest/getDeviceGroups/${accessTonek}**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "name": "G1",
      "description": "JUnit device group"
    },
    {
      "name": "G2",
      "description": "JUnit device group"
    }
  ],
  "totalCount": 2
}
```
### Delete Device Group example ###
**GET /vf/rest/deleteDeviceGroup/${accessToken}?name=G1**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Add Device to Group example ###
**GET /vf/rest/addDeviceToGroup/${accessToken}?groupName=JUnit&device=0238947023987**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Remove Device from Group example ###
**GET /vf/rest/removeDeviceFromGroup/${accessToken}?groupName=JUnit&device=0238947023987**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Get Devices of Group example ###
**GET /vf/rest/getDevicesOfGroup/${accessToken}?groupName=JUnit**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "description": "Device description",
      "imei": "0238947023987",
      "name": "Device Name",
      "sn": "023987"
    },
    {
      "description": "Device description",
      "imei": "2398472903879",
      "name": "Device Name",
      "sn": "903879"
    }
  ],
  "totalCount": 2
}
```
### Get Groups of Device example ###
**GET /vf/rest/getGroupsOfDevice/${accessToken}?device=0238947023987**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "name": "JUnit-1",
      "description": "JUnit device group"
    },
    {
      "name": "JUnit-2",
      "description": "JUnit device group"
    }
  ],
  "totalCount": 2
}
```
### Get Notifications example ###
**GET  /vf/rest/getNotifications/${accessToken}?shipment=11&pageSize=1&pageIndex=3**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [ //array of notifications id, notification type, issue (Alert/Arrival)
    {
      "id": 13,
      "type": "Alert",
      "issue": { //Temperature alert
        "id": 12,
        "date": "2015-11-05T13:57",
        "device": "234908720394857",
        "shipment": 11,
        "type": "Hot",
        "temperature": 5.0,
        "minutes": 55
      }
    },
    {
      "id": 15,
      "type": "Alert",
      "issue": { // Low Battery alert
        "id": 14,
        "date": "2015-11-05T13:57",
        "device": "234908720394857",
        "shipment": 11,
        "type": "Battery"
      }
    },
    {
      "id": 17,
      "type": "Arrival",
      "issue": { //Arrival
        "id": 16,
        "numberOfMetersOfArrival": 1500,
        "date": "2015-11-05T13:57",
        "device": "234908720394857",
        "shipment": 11
      }
    }
  ],
  "totalCount": 3
}
```
### Mark Notification as read example ###
**POST /vf/rest/markNotificationsAsRead/${accessToken}**    
**Request body:**  
```json
[ //array of notification ID
  31,
  33
]
```  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Send Command to Device example ###
**POST /vf/rest/sendCommandToDevice/${accessToken}**  
**Request body:**  
```json
{
  "device": "089723409857032498",
  "command": "shutdown"
}
```
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Get Location example ###
**GET /vf/rest/getLocation/${accessToken}?locationId=2**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "locationId": 2,
    "locationName": "Loc-1",
    "companyName": "Sun Microsystems",
    "notes": "Any notes",
    "address": "Bankstown Warehouse",
    "location": {
      "lat": 100.5,
      "lon": 100.501
    },
    "radiusMeters": 1000,
    "startFlag": "Y",
    "interimFlag": "Y",
    "endFlag": "Y"
  }
}
```
### Delete Location example ###
**GET /vf/rest/deleteLocation/${accessToken}?locationId=2**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Get Shipment Template example ###
**GET /rest/getShipmentTemplate/${accessToken}?id=88**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "shipmentTemplateId": 88,
    "shipmentTemplateName": "JUnit-tpl",
    "shipmentDescription": "Any Description",
    "addDateShipped": true,
    "shippedFrom": 86,
    "shippedTo": 87,
    "detectLocationForShippedFrom": true,
    "useCurrentTimeForDateShipped": true,
    "alertProfileId": 79,
    "alertSuppressionMinutes": 55,
    "maxTimesAlertFires": 4,
    "alertsNotificationSchedules": [
      80
    ],
    "commentsForReceiver": "Comments for receiver"
    "arrivalNotificationWithinKm": 11,
    "excludeNotificationsIfNoAlerts": true,
    "arrivalNotificationSchedules": [
      83
    ],
    "shutdownDeviceAfterMinutes": 155,
  }
}
```
### Get Device example ###
**GET /vf/rest/getDevice/${accessToken}?id=923487509328**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "description": "Device description",
    "id": "923487509328",
    "imei": "923487509328",
    "name": "Device Name",
    "sn": "1"
  }
}
```
### Get Shipment example ###
**GET /vf/rest/getShipment/${accessToken}?id=77**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "status": "InProgress",
    "deviceImei": "234908720394857",
    "deviceSN": "394857",
    "deviceName": "Device Name",
    "tripCount": 88,
    "shipmentId": 120,
    "shipmentDescription": "Any Description",
    "palletId": "palettid",
    "poNum": 893793487,
    "assetNum": "10515",
    "assetType": "SeaContainer",
    "shippedFrom": 118,
    "shippedTo": 119,
    "shipmentDate": "2015-10-25T21:58",
    "alertProfileId": 111,
    "alertSuppressionMinutes": 55,
    "maxTimesAlertFires": 4,
    "alertsNotificationSchedules": [
      112
    ],
    "commentsForReceiver": "Comments for receiver",
    "arrivalNotificationWithinKm": 111,
    "excludeNotificationsIfNoAlerts": true,
    "arrivalNotificationSchedules": [
      115
    ],
    "shutdownDeviceAfterMinutes": 155,
    "customFields": {
      "field1": "value1"
    }
  }
}
```
### Delete Notification Schedule example ###
**GET vf/rest/deleteNotificationSchedule/${accessToken}?notificationScheduleId=23**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Delete Person Schedule example ###
**GET /vf/rest/deletePersonSchedule/${accessToken}?notificationScheduleId=1&personScheduleId=3**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```
### Get Single Shipment example ###
**GET /vf/rest/getSingleShipment/${accessToken}?fromDate=${startDate}&shipment=${shipmentId}&toDate=${endData}**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "status": "In Progress",
    "deviceSN": "394857",
    "deviceName": "Device Name",
    "tripCount": 88,
    "shipmentId": 140,
    "shipmentDescription": "Any Description",
    "palletId": "palettid",
    "poNum": 893793487,
    "assetNum": "10515",
    "assetType": "SeaContainer",
    "shippedFrom": 138,
    "shippedTo": 139,
    "currentLocation": "Not determined",
    "estArrivalDate": "2015-11-08T11:44",
    "actualArrivalDate": "2015-11-08T11:44",
    "percentageComplete": 0,
    "alertProfileId": 131,
    "alertProfileName": "AnyAlert",
    "alertSuppressionMinutes": 55,
    "maxTimesAlertFires": 4,
    "alertsNotificationSchedules": [
      {
        "notificationScheduleId": 132,
        "notificationScheduleName": "Sched",
        "notificationScheduleDescription": "JUnit schedule",
        "peopleToNotify": "Alexander Suvorov, Alexander Suvorov"
      }
    ],
    "alertSummary": {
      "Hot": "1"
    },
    "arrivalNotificationWithinKm": 111,
    "excludeNotificationIfNoAlerts": false,
    "arrivalNotificationSchedules": [
      {
        "notificationScheduleId": 135,
        "notificationScheduleName": "Sched",
        "notificationScheduleDescription": "JUnit schedule",
        "peopleToNotify": "Alexander Suvorov, Alexander Suvorov"
      }
    ],
    "commentsForReceiver": "Comments for receiver",
    "items": [
      {
        "timestamp": "2015-11-06T11:44",
        "location": {
          "latitude": 50.5,
          "longitude": 51.51
        },
        "temperature": 56.0,
        "type": "AUT",
        "alerts": [
          {
            "description": "Too hot alert - tracker 394857(88) went above 5,0C degrees for 55 min",
            "type": "Hot"
          }
        ],
        "arrivals": [
          {
            "numberOfMetersOfArrival": 400,
            "arrivalReportSentTo": ""
          }
        ]
      },
      {
        "timestamp": "2015-11-06T11:44",
        "location": {
          "latitude": 50.5,
          "longitude": 51.51
        },
        "temperature": 56.0,
        "type": "AUT",
        "alerts": [],
        "arrivals": []
      }
    ]
  }
}
```
### New Utility methods ###
This is a dumps of new utility methods, which will accurrate formed in future
```
GET http://localhost:52409/web/vf/rest/getLanguages/14353566-fe7dfb85ca71cefeec5eee6b5a80bad3
Response:
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    "English"
  ]
}
GET http://localhost:52409/web/vf/rest/getRoles/1411739-cc45397413d4f65e4abd51be1a75d4a2
Response:
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    "GlobalAdmin",
    "CompanyAdmin",
    "Dispatcher",
    "ReportViewer"
  ]
}
GET http://localhost:52409/web/vf/rest/getTimeZones/27325411-43cc6e26dab7b8c13b2e915790dce848
Response:
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "id": "Etc/GMT+12",
      "displayName": "GMT-12:00"
    },
    {
      "id": "Etc/GMT+11",
      "displayName": "GMT-11:00"
    },
    {
      "id": "Pacific/Midway",
      "displayName": "Samoa Standard Time"
    },
    {
      "id": "Pacific/Niue",
      "displayName": "Niue Time"
    },
    {
      "id": "Pacific/Pago_Pago",
      "displayName": "Samoa Standard Time"
    },
    {
      "id": "Pacific/Samoa",
      "displayName": "Samoa Standard Time"
    },
    {
      "id": "US/Samoa",
      "displayName": "Samoa Standard Time"
    },
    {
      "id": "America/Adak",
      "displayName": "Hawaii-Aleutian Standard Time"
    },
    {
      "id": "America/Atka",
      "displayName": "Hawaii-Aleutian Standard Time"
    },
    {
      "id": "Etc/GMT+10",
      "displayName": "GMT-10:00"
    },
    {
      "id": "HST",
      "displayName": "Hawaii Standard Time"
    },
    {
      "id": "Pacific/Honolulu",
      "displayName": "Hawaii Standard Time"
    },
    {
      "id": "Pacific/Johnston",
      "displayName": "Hawaii Standard Time"
    },
    {
      "id": "Pacific/Rarotonga",
      "displayName": "Cook Is. Time"
    },
    {
      "id": "Pacific/Tahiti",
      "displayName": "Tahiti Time"
    },
    {
      "id": "SystemV/HST10",
      "displayName": "Hawaii Standard Time"
    },
    {
      "id": "US/Aleutian",
      "displayName": "Hawaii-Aleutian Standard Time"
    },
    {
      "id": "US/Hawaii",
      "displayName": "Hawaii Standard Time"
    },
    {
      "id": "Pacific/Marquesas",
      "displayName": "Marquesas Time"
    },
    {
      "id": "AST",
      "displayName": "Alaska Standard Time"
    },
    {
      "id": "America/Anchorage",
      "displayName": "Alaska Standard Time"
    },
    {
      "id": "America/Juneau",
      "displayName": "Alaska Standard Time"
    },
    {
      "id": "America/Nome",
      "displayName": "Alaska Standard Time"
    },
    {
      "id": "America/Sitka",
      "displayName": "Alaska Standard Time"
    },
    {
      "id": "America/Yakutat",
      "displayName": "Alaska Standard Time"
    },
    {
      "id": "Etc/GMT+9",
      "displayName": "GMT-09:00"
    },
    {
      "id": "Pacific/Gambier",
      "displayName": "Gambier Time"
    },
    {
      "id": "SystemV/YST9",
      "displayName": "Alaska Standard Time"
    },
    {
      "id": "SystemV/YST9YDT",
      "displayName": "Alaska Standard Time"
    },
    {
      "id": "US/Alaska",
      "displayName": "Alaska Standard Time"
    },
    {
      "id": "America/Dawson",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "America/Ensenada",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "America/Los_Angeles",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "America/Metlakatla",
      "displayName": "Metlakatla Standard Time"
    },
    {
      "id": "America/Santa_Isabel",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "America/Tijuana",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "America/Vancouver",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "America/Whitehorse",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "Canada/Pacific",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "Canada/Yukon",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "Etc/GMT+8",
      "displayName": "GMT-08:00"
    },
    {
      "id": "Mexico/BajaNorte",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "PST",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "PST8PDT",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "Pacific/Pitcairn",
      "displayName": "Pitcairn Standard Time"
    },
    {
      "id": "SystemV/PST8",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "SystemV/PST8PDT",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "US/Pacific",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "US/Pacific-New",
      "displayName": "Pacific Standard Time"
    },
    {
      "id": "America/Boise",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Cambridge_Bay",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Chihuahua",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Creston",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Dawson_Creek",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Denver",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Edmonton",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Hermosillo",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Inuvik",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Mazatlan",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Ojinaga",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Phoenix",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Shiprock",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Yellowknife",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "Canada/Mountain",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "Etc/GMT+7",
      "displayName": "GMT-07:00"
    },
    {
      "id": "MST",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "MST7MDT",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "Mexico/BajaSur",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "Navajo",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "PNT",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "SystemV/MST7",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "SystemV/MST7MDT",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "US/Arizona",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "US/Mountain",
      "displayName": "Mountain Standard Time"
    },
    {
      "id": "America/Bahia_Banderas",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Belize",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Cancun",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Chicago",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Costa_Rica",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/El_Salvador",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Guatemala",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Indiana/Knox",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Indiana/Tell_City",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Knox_IN",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Managua",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Matamoros",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Menominee",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Merida",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Mexico_City",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Monterrey",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/North_Dakota/Beulah",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/North_Dakota/Center",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/North_Dakota/New_Salem",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Rainy_River",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Rankin_Inlet",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Regina",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Resolute",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Swift_Current",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Tegucigalpa",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Winnipeg",
      "displayName": "Central Standard Time"
    },
    {
      "id": "CST",
      "displayName": "Central Standard Time"
    },
    {
      "id": "CST6CDT",
      "displayName": "Central Standard Time"
    },
    {
      "id": "Canada/Central",
      "displayName": "Central Standard Time"
    },
    {
      "id": "Canada/East-Saskatchewan",
      "displayName": "Central Standard Time"
    },
    {
      "id": "Canada/Saskatchewan",
      "displayName": "Central Standard Time"
    },
    {
      "id": "Chile/EasterIsland",
      "displayName": "Easter Is. Time"
    },
    {
      "id": "Etc/GMT+6",
      "displayName": "GMT-06:00"
    },
    {
      "id": "Mexico/General",
      "displayName": "Central Standard Time"
    },
    {
      "id": "Pacific/Easter",
      "displayName": "Easter Is. Time"
    },
    {
      "id": "Pacific/Galapagos",
      "displayName": "Galapagos Time"
    },
    {
      "id": "SystemV/CST6",
      "displayName": "Central Standard Time"
    },
    {
      "id": "SystemV/CST6CDT",
      "displayName": "Central Standard Time"
    },
    {
      "id": "US/Central",
      "displayName": "Central Standard Time"
    },
    {
      "id": "US/Indiana-Starke",
      "displayName": "Central Standard Time"
    },
    {
      "id": "America/Atikokan",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Bogota",
      "displayName": "Colombia Time"
    },
    {
      "id": "America/Cayman",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Coral_Harbour",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Detroit",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Eirunepe",
      "displayName": "Amazon Time"
    },
    {
      "id": "America/Fort_Wayne",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Grand_Turk",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Guayaquil",
      "displayName": "Ecuador Time"
    },
    {
      "id": "America/Havana",
      "displayName": "Cuba Standard Time"
    },
    {
      "id": "America/Indiana/Indianapolis",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Indiana/Marengo",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Indiana/Petersburg",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Indiana/Vevay",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Indiana/Vincennes",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Indiana/Winamac",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Indianapolis",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Iqaluit",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Jamaica",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Kentucky/Louisville",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Kentucky/Monticello",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Lima",
      "displayName": "Peru Time"
    },
    {
      "id": "America/Louisville",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Montreal",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Nassau",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/New_York",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Nipigon",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Panama",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Pangnirtung",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Port-au-Prince",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Porto_Acre",
      "displayName": "Amazon Time"
    },
    {
      "id": "America/Rio_Branco",
      "displayName": "Amazon Time"
    },
    {
      "id": "America/Thunder_Bay",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Toronto",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "Brazil/Acre",
      "displayName": "Amazon Time"
    },
    {
      "id": "Canada/Eastern",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "Cuba",
      "displayName": "Cuba Standard Time"
    },
    {
      "id": "EST",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "EST5EDT",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "Etc/GMT+5",
      "displayName": "GMT-05:00"
    },
    {
      "id": "IET",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "Jamaica",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "SystemV/EST5",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "SystemV/EST5EDT",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "US/East-Indiana",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "US/Eastern",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "US/Michigan",
      "displayName": "Eastern Standard Time"
    },
    {
      "id": "America/Caracas",
      "displayName": "Venezuela Time"
    },
    {
      "id": "America/Anguilla",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Antigua",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Aruba",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Asuncion",
      "displayName": "Paraguay Time"
    },
    {
      "id": "America/Barbados",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Blanc-Sablon",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Boa_Vista",
      "displayName": "Amazon Time"
    },
    {
      "id": "America/Campo_Grande",
      "displayName": "Amazon Time"
    },
    {
      "id": "America/Cuiaba",
      "displayName": "Amazon Time"
    },
    {
      "id": "America/Curacao",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Dominica",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Glace_Bay",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Goose_Bay",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Grenada",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Guadeloupe",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Guyana",
      "displayName": "Guyana Time"
    },
    {
      "id": "America/Halifax",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Kralendijk",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/La_Paz",
      "displayName": "Bolivia Time"
    },
    {
      "id": "America/Lower_Princes",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Manaus",
      "displayName": "Amazon Time"
    },
    {
      "id": "America/Marigot",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Martinique",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Moncton",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Montserrat",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Port_of_Spain",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Porto_Velho",
      "displayName": "Amazon Time"
    },
    {
      "id": "America/Puerto_Rico",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Santiago",
      "displayName": "Chile Time"
    },
    {
      "id": "America/Santo_Domingo",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/St_Barthelemy",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/St_Kitts",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/St_Lucia",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/St_Thomas",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/St_Vincent",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Thule",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Tortola",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/Virgin",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "Antarctica/Palmer",
      "displayName": "Chile Time"
    },
    {
      "id": "Atlantic/Bermuda",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "Brazil/West",
      "displayName": "Amazon Time"
    },
    {
      "id": "Canada/Atlantic",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "Chile/Continental",
      "displayName": "Chile Time"
    },
    {
      "id": "Etc/GMT+4",
      "displayName": "GMT-04:00"
    },
    {
      "id": "PRT",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "SystemV/AST4",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "SystemV/AST4ADT",
      "displayName": "Atlantic Standard Time"
    },
    {
      "id": "America/St_Johns",
      "displayName": "Newfoundland Standard Time"
    },
    {
      "id": "CNT",
      "displayName": "Newfoundland Standard Time"
    },
    {
      "id": "Canada/Newfoundland",
      "displayName": "Newfoundland Standard Time"
    },
    {
      "id": "AGT",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Araguaina",
      "displayName": "Brasilia Time"
    },
    {
      "id": "America/Argentina/Buenos_Aires",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/Catamarca",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/ComodRivadavia",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/Cordoba",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/Jujuy",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/La_Rioja",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/Mendoza",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/Rio_Gallegos",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/Salta",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/San_Juan",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/San_Luis",
      "displayName": "Western Argentine Time"
    },
    {
      "id": "America/Argentina/Tucuman",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Argentina/Ushuaia",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Bahia",
      "displayName": "Brasilia Time"
    },
    {
      "id": "America/Belem",
      "displayName": "Brasilia Time"
    },
    {
      "id": "America/Buenos_Aires",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Catamarca",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Cayenne",
      "displayName": "French Guiana Time"
    },
    {
      "id": "America/Cordoba",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Fortaleza",
      "displayName": "Brasilia Time"
    },
    {
      "id": "America/Godthab",
      "displayName": "Western Greenland Time"
    },
    {
      "id": "America/Jujuy",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Maceio",
      "displayName": "Brasilia Time"
    },
    {
      "id": "America/Mendoza",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Miquelon",
      "displayName": "Pierre & Miquelon Standard Time"
    },
    {
      "id": "America/Montevideo",
      "displayName": "Uruguay Time"
    },
    {
      "id": "America/Paramaribo",
      "displayName": "Suriname Time"
    },
    {
      "id": "America/Recife",
      "displayName": "Brasilia Time"
    },
    {
      "id": "America/Rosario",
      "displayName": "Argentine Time"
    },
    {
      "id": "America/Santarem",
      "displayName": "Brasilia Time"
    },
    {
      "id": "America/Sao_Paulo",
      "displayName": "Brasilia Time"
    },
    {
      "id": "Antarctica/Rothera",
      "displayName": "Rothera Time"
    },
    {
      "id": "Atlantic/Stanley",
      "displayName": "Falkland Is. Time"
    },
    {
      "id": "BET",
      "displayName": "Brasilia Time"
    },
    {
      "id": "Brazil/East",
      "displayName": "Brasilia Time"
    },
    {
      "id": "Etc/GMT+3",
      "displayName": "GMT-03:00"
    },
    {
      "id": "America/Noronha",
      "displayName": "Fernando de Noronha Time"
    },
    {
      "id": "Atlantic/South_Georgia",
      "displayName": "South Georgia Standard Time"
    },
    {
      "id": "Brazil/DeNoronha",
      "displayName": "Fernando de Noronha Time"
    },
    {
      "id": "Etc/GMT+2",
      "displayName": "GMT-02:00"
    },
    {
      "id": "America/Scoresbysund",
      "displayName": "Eastern Greenland Time"
    },
    {
      "id": "Atlantic/Azores",
      "displayName": "Azores Time"
    },
    {
      "id": "Atlantic/Cape_Verde",
      "displayName": "Cape Verde Time"
    },
    {
      "id": "Etc/GMT+1",
      "displayName": "GMT-01:00"
    },
    {
      "id": "Africa/Abidjan",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Accra",
      "displayName": "Ghana Mean Time"
    },
    {
      "id": "Africa/Bamako",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Banjul",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Bissau",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Casablanca",
      "displayName": "Western European Time"
    },
    {
      "id": "Africa/Conakry",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Dakar",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/El_Aaiun",
      "displayName": "Western European Time"
    },
    {
      "id": "Africa/Freetown",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Lome",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Monrovia",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Nouakchott",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Ouagadougou",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Sao_Tome",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Africa/Timbuktu",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "America/Danmarkshavn",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Atlantic/Canary",
      "displayName": "Western European Time"
    },
    {
      "id": "Atlantic/Faeroe",
      "displayName": "Western European Time"
    },
    {
      "id": "Atlantic/Faroe",
      "displayName": "Western European Time"
    },
    {
      "id": "Atlantic/Madeira",
      "displayName": "Western European Time"
    },
    {
      "id": "Atlantic/Reykjavik",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Atlantic/St_Helena",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Eire",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Etc/GMT",
      "displayName": "GMT+00:00"
    },
    {
      "id": "Etc/GMT+0",
      "displayName": "GMT+00:00"
    },
    {
      "id": "Etc/GMT-0",
      "displayName": "GMT+00:00"
    },
    {
      "id": "Etc/GMT0",
      "displayName": "GMT+00:00"
    },
    {
      "id": "Etc/Greenwich",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Etc/UCT",
      "displayName": "Coordinated Universal Time"
    },
    {
      "id": "Etc/UTC",
      "displayName": "Coordinated Universal Time"
    },
    {
      "id": "Etc/Universal",
      "displayName": "Coordinated Universal Time"
    },
    {
      "id": "Etc/Zulu",
      "displayName": "Coordinated Universal Time"
    },
    {
      "id": "Europe/Belfast",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Europe/Dublin",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Europe/Guernsey",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Europe/Isle_of_Man",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Europe/Jersey",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Europe/Lisbon",
      "displayName": "Western European Time"
    },
    {
      "id": "Europe/London",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "GB",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "GB-Eire",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "GMT",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "GMT0",
      "displayName": "GMT+00:00"
    },
    {
      "id": "Greenwich",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Iceland",
      "displayName": "Greenwich Mean Time"
    },
    {
      "id": "Portugal",
      "displayName": "Western European Time"
    },
    {
      "id": "UCT",
      "displayName": "Coordinated Universal Time"
    },
    {
      "id": "UTC",
      "displayName": "Coordinated Universal Time"
    },
    {
      "id": "Universal",
      "displayName": "Coordinated Universal Time"
    },
    {
      "id": "WET",
      "displayName": "Western European Time"
    },
    {
      "id": "Zulu",
      "displayName": "Coordinated Universal Time"
    },
    {
      "id": "Africa/Algiers",
      "displayName": "Central European Time"
    },
    {
      "id": "Africa/Bangui",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Brazzaville",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Ceuta",
      "displayName": "Central European Time"
    },
    {
      "id": "Africa/Douala",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Kinshasa",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Lagos",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Libreville",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Luanda",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Malabo",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Ndjamena",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Niamey",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Porto-Novo",
      "displayName": "Western African Time"
    },
    {
      "id": "Africa/Tunis",
      "displayName": "Central European Time"
    },
    {
      "id": "Africa/Windhoek",
      "displayName": "Western African Time"
    },
    {
      "id": "Arctic/Longyearbyen",
      "displayName": "Central European Time"
    },
    {
      "id": "Atlantic/Jan_Mayen",
      "displayName": "Central European Time"
    },
    {
      "id": "CET",
      "displayName": "Central European Time"
    },
    {
      "id": "ECT",
      "displayName": "Central European Time"
    },
    {
      "id": "Etc/GMT-1",
      "displayName": "GMT+01:00"
    },
    {
      "id": "Europe/Amsterdam",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Andorra",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Belgrade",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Berlin",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Bratislava",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Brussels",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Budapest",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Busingen",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Copenhagen",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Gibraltar",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Ljubljana",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Luxembourg",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Madrid",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Malta",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Monaco",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Oslo",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Paris",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Podgorica",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Prague",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Rome",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/San_Marino",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Sarajevo",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Skopje",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Stockholm",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Tirane",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Vaduz",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Vatican",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Vienna",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Warsaw",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Zagreb",
      "displayName": "Central European Time"
    },
    {
      "id": "Europe/Zurich",
      "displayName": "Central European Time"
    },
    {
      "id": "MET",
      "displayName": "Middle Europe Time"
    },
    {
      "id": "Poland",
      "displayName": "Central European Time"
    },
    {
      "id": "ART",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Africa/Blantyre",
      "displayName": "Central African Time"
    },
    {
      "id": "Africa/Bujumbura",
      "displayName": "Central African Time"
    },
    {
      "id": "Africa/Cairo",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Africa/Gaborone",
      "displayName": "Central African Time"
    },
    {
      "id": "Africa/Harare",
      "displayName": "Central African Time"
    },
    {
      "id": "Africa/Johannesburg",
      "displayName": "South Africa Standard Time"
    },
    {
      "id": "Africa/Kigali",
      "displayName": "Central African Time"
    },
    {
      "id": "Africa/Lubumbashi",
      "displayName": "Central African Time"
    },
    {
      "id": "Africa/Lusaka",
      "displayName": "Central African Time"
    },
    {
      "id": "Africa/Maputo",
      "displayName": "Central African Time"
    },
    {
      "id": "Africa/Maseru",
      "displayName": "South Africa Standard Time"
    },
    {
      "id": "Africa/Mbabane",
      "displayName": "South Africa Standard Time"
    },
    {
      "id": "Africa/Tripoli",
      "displayName": "Central European Time"
    },
    {
      "id": "Asia/Amman",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Asia/Beirut",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Asia/Damascus",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Asia/Gaza",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Asia/Hebron",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Asia/Istanbul",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Asia/Jerusalem",
      "displayName": "Israel Standard Time"
    },
    {
      "id": "Asia/Nicosia",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Asia/Tel_Aviv",
      "displayName": "Israel Standard Time"
    },
    {
      "id": "CAT",
      "displayName": "Central African Time"
    },
    {
      "id": "EET",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Egypt",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Etc/GMT-2",
      "displayName": "GMT+02:00"
    },
    {
      "id": "Europe/Athens",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Bucharest",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Chisinau",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Helsinki",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Istanbul",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Kiev",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Mariehamn",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Nicosia",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Riga",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Simferopol",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Sofia",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Tallinn",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Tiraspol",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Uzhgorod",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Vilnius",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Europe/Zaporozhye",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Israel",
      "displayName": "Israel Standard Time"
    },
    {
      "id": "Libya",
      "displayName": "Central European Time"
    },
    {
      "id": "Turkey",
      "displayName": "Eastern European Time"
    },
    {
      "id": "Africa/Addis_Ababa",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Africa/Asmara",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Africa/Asmera",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Africa/Dar_es_Salaam",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Africa/Djibouti",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Africa/Juba",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Africa/Kampala",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Africa/Khartoum",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Africa/Mogadishu",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Africa/Nairobi",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Antarctica/Syowa",
      "displayName": "Syowa Time"
    },
    {
      "id": "Asia/Aden",
      "displayName": "Arabia Standard Time"
    },
    {
      "id": "Asia/Baghdad",
      "displayName": "Arabia Standard Time"
    },
    {
      "id": "Asia/Bahrain",
      "displayName": "Arabia Standard Time"
    },
    {
      "id": "Asia/Kuwait",
      "displayName": "Arabia Standard Time"
    },
    {
      "id": "Asia/Qatar",
      "displayName": "Arabia Standard Time"
    },
    {
      "id": "Asia/Riyadh",
      "displayName": "Arabia Standard Time"
    },
    {
      "id": "EAT",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Etc/GMT-3",
      "displayName": "GMT+03:00"
    },
    {
      "id": "Europe/Kaliningrad",
      "displayName": "Further-eastern European Time"
    },
    {
      "id": "Europe/Minsk",
      "displayName": "Further-eastern European Time"
    },
    {
      "id": "Indian/Antananarivo",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Indian/Comoro",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Indian/Mayotte",
      "displayName": "Eastern African Time"
    },
    {
      "id": "Asia/Tehran",
      "displayName": "Iran Standard Time"
    },
    {
      "id": "Iran",
      "displayName": "Iran Standard Time"
    },
    {
      "id": "Asia/Baku",
      "displayName": "Azerbaijan Time"
    },
    {
      "id": "Asia/Dubai",
      "displayName": "Gulf Standard Time"
    },
    {
      "id": "Asia/Muscat",
      "displayName": "Gulf Standard Time"
    },
    {
      "id": "Asia/Tbilisi",
      "displayName": "Georgia Time"
    },
    {
      "id": "Asia/Yerevan",
      "displayName": "Armenia Time"
    },
    {
      "id": "Etc/GMT-4",
      "displayName": "GMT+04:00"
    },
    {
      "id": "Europe/Moscow",
      "displayName": "Moscow Standard Time"
    },
    {
      "id": "Europe/Samara",
      "displayName": "Samara Time"
    },
    {
      "id": "Europe/Volgograd",
      "displayName": "Volgograd Time"
    },
    {
      "id": "Indian/Mahe",
      "displayName": "Seychelles Time"
    },
    {
      "id": "Indian/Mauritius",
      "displayName": "Mauritius Time"
    },
    {
      "id": "Indian/Reunion",
      "displayName": "Reunion Time"
    },
    {
      "id": "NET",
      "displayName": "Armenia Time"
    },
    {
      "id": "W-SU",
      "displayName": "Moscow Standard Time"
    },
    {
      "id": "Asia/Kabul",
      "displayName": "Afghanistan Time"
    },
    {
      "id": "Antarctica/Mawson",
      "displayName": "Mawson Time"
    },
    {
      "id": "Asia/Aqtau",
      "displayName": "Aqtau Time"
    },
    {
      "id": "Asia/Aqtobe",
      "displayName": "Aqtobe Time"
    },
    {
      "id": "Asia/Ashgabat",
      "displayName": "Turkmenistan Time"
    },
    {
      "id": "Asia/Ashkhabad",
      "displayName": "Turkmenistan Time"
    },
    {
      "id": "Asia/Dushanbe",
      "displayName": "Tajikistan Time"
    },
    {
      "id": "Asia/Karachi",
      "displayName": "Pakistan Time"
    },
    {
      "id": "Asia/Oral",
      "displayName": "Oral Time"
    },
    {
      "id": "Asia/Samarkand",
      "displayName": "Uzbekistan Time"
    },
    {
      "id": "Asia/Tashkent",
      "displayName": "Uzbekistan Time"
    },
    {
      "id": "Etc/GMT-5",
      "displayName": "GMT+05:00"
    },
    {
      "id": "Indian/Kerguelen",
      "displayName": "French Southern & Antarctic Lands Time"
    },
    {
      "id": "Indian/Maldives",
      "displayName": "Maldives Time"
    },
    {
      "id": "PLT",
      "displayName": "Pakistan Time"
    },
    {
      "id": "Asia/Calcutta",
      "displayName": "India Standard Time"
    },
    {
      "id": "Asia/Colombo",
      "displayName": "India Standard Time"
    },
    {
      "id": "Asia/Kolkata",
      "displayName": "India Standard Time"
    },
    {
      "id": "IST",
      "displayName": "India Standard Time"
    },
    {
      "id": "Asia/Kathmandu",
      "displayName": "Nepal Time"
    },
    {
      "id": "Asia/Katmandu",
      "displayName": "Nepal Time"
    },
    {
      "id": "Antarctica/Vostok",
      "displayName": "Vostok Time"
    },
    {
      "id": "Asia/Almaty",
      "displayName": "Alma-Ata Time"
    },
    {
      "id": "Asia/Bishkek",
      "displayName": "Kirgizstan Time"
    },
    {
      "id": "Asia/Dacca",
      "displayName": "Bangladesh Time"
    },
    {
      "id": "Asia/Dhaka",
      "displayName": "Bangladesh Time"
    },
    {
      "id": "Asia/Qyzylorda",
      "displayName": "Qyzylorda Time"
    },
    {
      "id": "Asia/Thimbu",
      "displayName": "Bhutan Time"
    },
    {
      "id": "Asia/Thimphu",
      "displayName": "Bhutan Time"
    },
    {
      "id": "Asia/Yekaterinburg",
      "displayName": "Yekaterinburg Time"
    },
    {
      "id": "BST",
      "displayName": "Bangladesh Time"
    },
    {
      "id": "Etc/GMT-6",
      "displayName": "GMT+06:00"
    },
    {
      "id": "Indian/Chagos",
      "displayName": "Indian Ocean Territory Time"
    },
    {
      "id": "Asia/Rangoon",
      "displayName": "Myanmar Time"
    },
    {
      "id": "Indian/Cocos",
      "displayName": "Cocos Islands Time"
    },
    {
      "id": "Antarctica/Davis",
      "displayName": "Davis Time"
    },
    {
      "id": "Asia/Bangkok",
      "displayName": "Indochina Time"
    },
    {
      "id": "Asia/Ho_Chi_Minh",
      "displayName": "Indochina Time"
    },
    {
      "id": "Asia/Hovd",
      "displayName": "Hovd Time"
    },
    {
      "id": "Asia/Jakarta",
      "displayName": "West Indonesia Time"
    },
    {
      "id": "Asia/Novokuznetsk",
      "displayName": "Novosibirsk Time"
    },
    {
      "id": "Asia/Novosibirsk",
      "displayName": "Novosibirsk Time"
    },
    {
      "id": "Asia/Omsk",
      "displayName": "Omsk Time"
    },
    {
      "id": "Asia/Phnom_Penh",
      "displayName": "Indochina Time"
    },
    {
      "id": "Asia/Pontianak",
      "displayName": "West Indonesia Time"
    },
    {
      "id": "Asia/Saigon",
      "displayName": "Indochina Time"
    },
    {
      "id": "Asia/Vientiane",
      "displayName": "Indochina Time"
    },
    {
      "id": "Etc/GMT-7",
      "displayName": "GMT+07:00"
    },
    {
      "id": "Indian/Christmas",
      "displayName": "Christmas Island Time"
    },
    {
      "id": "VST",
      "displayName": "Indochina Time"
    },
    {
      "id": "Antarctica/Casey",
      "displayName": "Western Standard Time (Australia)"
    },
    {
      "id": "Asia/Brunei",
      "displayName": "Brunei Time"
    },
    {
      "id": "Asia/Choibalsan",
      "displayName": "Choibalsan Time"
    },
    {
      "id": "Asia/Chongqing",
      "displayName": "China Standard Time"
    },
    {
      "id": "Asia/Chungking",
      "displayName": "China Standard Time"
    },
    {
      "id": "Asia/Harbin",
      "displayName": "China Standard Time"
    },
    {
      "id": "Asia/Hong_Kong",
      "displayName": "Hong Kong Time"
    },
    {
      "id": "Asia/Kashgar",
      "displayName": "China Standard Time"
    },
    {
      "id": "Asia/Krasnoyarsk",
      "displayName": "Krasnoyarsk Time"
    },
    {
      "id": "Asia/Kuala_Lumpur",
      "displayName": "Malaysia Time"
    },
    {
      "id": "Asia/Kuching",
      "displayName": "Malaysia Time"
    },
    {
      "id": "Asia/Macao",
      "displayName": "China Standard Time"
    },
    {
      "id": "Asia/Macau",
      "displayName": "China Standard Time"
    },
    {
      "id": "Asia/Makassar",
      "displayName": "Central Indonesia Time"
    },
    {
      "id": "Asia/Manila",
      "displayName": "Philippines Time"
    },
    {
      "id": "Asia/Shanghai",
      "displayName": "China Standard Time"
    },
    {
      "id": "Asia/Singapore",
      "displayName": "Singapore Time"
    },
    {
      "id": "Asia/Taipei",
      "displayName": "China Standard Time"
    },
    {
      "id": "Asia/Ujung_Pandang",
      "displayName": "Central Indonesia Time"
    },
    {
      "id": "Asia/Ulaanbaatar",
      "displayName": "Ulaanbaatar Time"
    },
    {
      "id": "Asia/Ulan_Bator",
      "displayName": "Ulaanbaatar Time"
    },
    {
      "id": "Asia/Urumqi",
      "displayName": "China Standard Time"
    },
    {
      "id": "Australia/Perth",
      "displayName": "Western Standard Time (Australia)"
    },
    {
      "id": "Australia/West",
      "displayName": "Western Standard Time (Australia)"
    },
    {
      "id": "CTT",
      "displayName": "China Standard Time"
    },
    {
      "id": "Etc/GMT-8",
      "displayName": "GMT+08:00"
    },
    {
      "id": "Hongkong",
      "displayName": "Hong Kong Time"
    },
    {
      "id": "PRC",
      "displayName": "China Standard Time"
    },
    {
      "id": "Singapore",
      "displayName": "Singapore Time"
    },
    {
      "id": "Australia/Eucla",
      "displayName": "Central Western Standard Time (Australia)"
    },
    {
      "id": "Asia/Dili",
      "displayName": "Timor-Leste Time"
    },
    {
      "id": "Asia/Irkutsk",
      "displayName": "Irkutsk Time"
    },
    {
      "id": "Asia/Jayapura",
      "displayName": "East Indonesia Time"
    },
    {
      "id": "Asia/Pyongyang",
      "displayName": "Korea Standard Time"
    },
    {
      "id": "Asia/Seoul",
      "displayName": "Korea Standard Time"
    },
    {
      "id": "Asia/Tokyo",
      "displayName": "Japan Standard Time"
    },
    {
      "id": "Etc/GMT-9",
      "displayName": "GMT+09:00"
    },
    {
      "id": "JST",
      "displayName": "Japan Standard Time"
    },
    {
      "id": "Japan",
      "displayName": "Japan Standard Time"
    },
    {
      "id": "Pacific/Palau",
      "displayName": "Palau Time"
    },
    {
      "id": "ROK",
      "displayName": "Korea Standard Time"
    },
    {
      "id": "ACT",
      "displayName": "Central Standard Time (Northern Territory)"
    },
    {
      "id": "Australia/Adelaide",
      "displayName": "Central Standard Time (South Australia)"
    },
    {
      "id": "Australia/Broken_Hill",
      "displayName": "Central Standard Time (South Australia/New South Wales)"
    },
    {
      "id": "Australia/Darwin",
      "displayName": "Central Standard Time (Northern Territory)"
    },
    {
      "id": "Australia/North",
      "displayName": "Central Standard Time (Northern Territory)"
    },
    {
      "id": "Australia/South",
      "displayName": "Central Standard Time (South Australia)"
    },
    {
      "id": "Australia/Yancowinna",
      "displayName": "Central Standard Time (South Australia/New South Wales)"
    },
    {
      "id": "AET",
      "displayName": "Eastern Standard Time (New South Wales)"
    },
    {
      "id": "Antarctica/DumontDUrville",
      "displayName": "Dumont-d'Urville Time"
    },
    {
      "id": "Asia/Khandyga",
      "displayName": "Yakutsk Time"
    },
    {
      "id": "Asia/Yakutsk",
      "displayName": "Yakutsk Time"
    },
    {
      "id": "Australia/ACT",
      "displayName": "Eastern Standard Time (New South Wales)"
    },
    {
      "id": "Australia/Brisbane",
      "displayName": "Eastern Standard Time (Queensland)"
    },
    {
      "id": "Australia/Canberra",
      "displayName": "Eastern Standard Time (New South Wales)"
    },
    {
      "id": "Australia/Currie",
      "displayName": "Eastern Standard Time (New South Wales)"
    },
    {
      "id": "Australia/Hobart",
      "displayName": "Eastern Standard Time (Tasmania)"
    },
    {
      "id": "Australia/Lindeman",
      "displayName": "Eastern Standard Time (Queensland)"
    },
    {
      "id": "Australia/Melbourne",
      "displayName": "Eastern Standard Time (Victoria)"
    },
    {
      "id": "Australia/NSW",
      "displayName": "Eastern Standard Time (New South Wales)"
    },
    {
      "id": "Australia/Queensland",
      "displayName": "Eastern Standard Time (Queensland)"
    },
    {
      "id": "Australia/Sydney",
      "displayName": "Eastern Standard Time (New South Wales)"
    },
    {
      "id": "Australia/Tasmania",
      "displayName": "Eastern Standard Time (Tasmania)"
    },
    {
      "id": "Australia/Victoria",
      "displayName": "Eastern Standard Time (Victoria)"
    },
    {
      "id": "Etc/GMT-10",
      "displayName": "GMT+10:00"
    },
    {
      "id": "Pacific/Chuuk",
      "displayName": "Chuuk Time"
    },
    {
      "id": "Pacific/Guam",
      "displayName": "Chamorro Standard Time"
    },
    {
      "id": "Pacific/Port_Moresby",
      "displayName": "Papua New Guinea Time"
    },
    {
      "id": "Pacific/Saipan",
      "displayName": "Chamorro Standard Time"
    },
    {
      "id": "Pacific/Truk",
      "displayName": "Chuuk Time"
    },
    {
      "id": "Pacific/Yap",
      "displayName": "Chuuk Time"
    },
    {
      "id": "Australia/LHI",
      "displayName": "Lord Howe Standard Time"
    },
    {
      "id": "Australia/Lord_Howe",
      "displayName": "Lord Howe Standard Time"
    },
    {
      "id": "Antarctica/Macquarie",
      "displayName": "Macquarie Island Time"
    },
    {
      "id": "Asia/Sakhalin",
      "displayName": "Sakhalin Time"
    },
    {
      "id": "Asia/Ust-Nera",
      "displayName": "Vladivostok Time"
    },
    {
      "id": "Asia/Vladivostok",
      "displayName": "Vladivostok Time"
    },
    {
      "id": "Etc/GMT-11",
      "displayName": "GMT+11:00"
    },
    {
      "id": "Pacific/Efate",
      "displayName": "Vanuatu Time"
    },
    {
      "id": "Pacific/Guadalcanal",
      "displayName": "Solomon Is. Time"
    },
    {
      "id": "Pacific/Kosrae",
      "displayName": "Kosrae Time"
    },
    {
      "id": "Pacific/Noumea",
      "displayName": "New Caledonia Time"
    },
    {
      "id": "Pacific/Pohnpei",
      "displayName": "Pohnpei Time"
    },
    {
      "id": "Pacific/Ponape",
      "displayName": "Pohnpei Time"
    },
    {
      "id": "SST",
      "displayName": "Solomon Is. Time"
    },
    {
      "id": "Pacific/Norfolk",
      "displayName": "Norfolk Time"
    },
    {
      "id": "Antarctica/McMurdo",
      "displayName": "New Zealand Standard Time"
    },
    {
      "id": "Antarctica/South_Pole",
      "displayName": "New Zealand Standard Time"
    },
    {
      "id": "Asia/Anadyr",
      "displayName": "Anadyr Time"
    },
    {
      "id": "Asia/Kamchatka",
      "displayName": "Petropavlovsk-Kamchatski Time"
    },
    {
      "id": "Asia/Magadan",
      "displayName": "Magadan Time"
    },
    {
      "id": "Etc/GMT-12",
      "displayName": "GMT+12:00"
    },
    {
      "id": "Kwajalein",
      "displayName": "Marshall Islands Time"
    },
    {
      "id": "NST",
      "displayName": "New Zealand Standard Time"
    },
    {
      "id": "NZ",
      "displayName": "New Zealand Standard Time"
    },
    {
      "id": "Pacific/Auckland",
      "displayName": "New Zealand Standard Time"
    },
    {
      "id": "Pacific/Fiji",
      "displayName": "Fiji Time"
    },
    {
      "id": "Pacific/Funafuti",
      "displayName": "Tuvalu Time"
    },
    {
      "id": "Pacific/Kwajalein",
      "displayName": "Marshall Islands Time"
    },
    {
      "id": "Pacific/Majuro",
      "displayName": "Marshall Islands Time"
    },
    {
      "id": "Pacific/Nauru",
      "displayName": "Nauru Time"
    },
    {
      "id": "Pacific/Tarawa",
      "displayName": "Gilbert Is. Time"
    },
    {
      "id": "Pacific/Wake",
      "displayName": "Wake Time"
    },
    {
      "id": "Pacific/Wallis",
      "displayName": "Wallis & Futuna Time"
    },
    {
      "id": "NZ-CHAT",
      "displayName": "Chatham Standard Time"
    },
    {
      "id": "Pacific/Chatham",
      "displayName": "Chatham Standard Time"
    },
    {
      "id": "Etc/GMT-13",
      "displayName": "GMT+13:00"
    },
    {
      "id": "MIT",
      "displayName": "West Samoa Time"
    },
    {
      "id": "Pacific/Apia",
      "displayName": "West Samoa Time"
    },
    {
      "id": "Pacific/Enderbury",
      "displayName": "Phoenix Is. Time"
    },
    {
      "id": "Pacific/Fakaofo",
      "displayName": "Tokelau Time"
    },
    {
      "id": "Pacific/Tongatapu",
      "displayName": "Tonga Time"
    },
    {
      "id": "Etc/GMT-14",
      "displayName": "GMT+14:00"
    },
    {
      "id": "Pacific/Kiritimati",
      "displayName": "Line Is. Time"
    }
  ]
}
GET http://localhost:52409/web/vf/rest/getUserTime/6748790-17ed06302a55e85eddeba21b7d1a0006
Response:
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "dateTimeIso": "2015-11-23T03:12",
    "formattedDateTimeIso": "23 ноя 2015 03:12AM",
    "dateTimeString": "23 ноя 2015",
    "dateString": "03:12AM",
    "dateString24": "03:12"
  }
}
GET http://localhost:52409/web/vf/rest/getMeasurementUnits/26110244-0d0f2d80e681547c6cd7250aa9f492bc
Response:
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    "Metric",
    "English"
  ]
}

```
