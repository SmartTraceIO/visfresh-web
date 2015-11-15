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
3. [Temperature Issue](#markdown-header-temperature-issue)  
4. [Notification Schedule](#markdown-header-notification-schedule)  
5. [Location](#markdown-header-location)  
6. [Shipment Template](#markdown-header-shipment-template)  
7. [Device](#markdown-header-device)  
8. [Shipment](#markdown-header-shipment)  
9. [Notification](#markdown-header-notification)  
10. [Alert](#markdown-header-alert)  
11. [Temperature Alert](#markdown-header-temperature-alert)  
12. [Arrival](#markdown-header-arrival)  
13. [User](#markdown-header-user)  

## Lists ##
List items is short representations of base entities, like as [Alert Profile](#markdown-header-alert-profile), [Location](#markdown-header-location), etc. Some of fields can be get from corresponding base entity and some can be synthetic fields.  

1. [Shipment Template list item](#markdown-header-shipment-template-list-item) 
2. [Shipment List item](#markdown-header-shipment-list-item)  
3. [Notification Schedule list item](#markdown-header-notification-schedule-list-item)  
4. [User List item](#markdown-header-user-list-item)  

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
28. [Delete Shipment](#markdown-header-delete-shipment)  
29. [Save Device](#markdown-header-save-device)  
30. [Get Device](#markdown-header-get-device)  
31. [Get Devices](#markdown-header-get-devices)  
32. [Delete Device](#markdown-header-delete-device)  
33. [Get Notifications](#markdown-header-get-notifications)  
34. [Send Command to Device](#markdown-header-send-command-to-device)  
35. [Mark Notification as read](#markdown-header-mark-notification-as-read)  

## Reports ##
1. [Get Single Shipment](#markdown-header-get-single-shipment)

### Authentication.###
Method *GET*, method name *login*, request parameters login - the user login name and password - the user password  
1. login - user name of logged in user  
2. password - password  
Returns [Authentication token](#markdown-header-authentication-token).  
[(example)](#markdown-header-authentication-request-example)  

### Get access token using existing GTS(e) session.###
The user should be logged in to GTS(e). (not implemented now).
Method *POST*, method name *getToken*, no parameters. In case of this request the service access a current user session, determines user info, log in as REST service user and returns authentication session.  
[(example)](#markdown-header-attach-to-existing-session-example)

### Get User Info ###
Method *GET*, method name *getUser*, method parameters  
1. username - name of user  

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
1. firstName - first user name.  
2. lastName - last user name.  
3. position - position of user in company.  
4. email - user email address.  
5. phone - user phone number
6. password - user password.  
7. user - user login name. It is not changeable parameter. Is used for identify the user to change details.  
8. temperatureUnits - user temperature units.  
9. timeZone - user time zone.  
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

### Save Shipment ###
Method *POST*, method name saveShipment, request body contains JSON serialized [Save Shipment request](#markdown-header-save-shipment-request). Response contains ID of just saved Shipment and ID of shipment template if the shipment was saved with corresponding option.  
[(example)](#markdown-header-save-shipment-example)

### Get Shipment ###
Method *GET*, method name *getShipment*. Request parameters:  
1. id - shipment ID.  
Returns [Shipment Object](#markdown-header-shipment)  
[(example)](#markdown-header-get-shipment-example)

### Get Shipments ###
Method *GET*, method name getShipments, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
3. sc - sort column  
4. so - sort order  
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
	"temperatureIssues": [/* Array of temperature issues */]
}
```
[(See Temperature Issue)](#markdown-header-temperature-issue)
### Temperature Issue ###
```json
{
  "id": 1007,
  "type": "CriticalHot",
  "temperature": 17.0,
  "timeOutMinutes": 1
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
    "personScheduleId": 293,
    "user": "asuvorov", // user name of linked system user
    "pushToMobileApp": true,
    "fromTime": 1,
    "toTime": 17,
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
  "login": "userLogin",
  "fullName": "Full User Name"
}
```
## Examples ##
### Authentication request example ###
**GET /vf/rest/login?login=user&password=password**   
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
**GET /vf/rest/getUser/${authToken}?username=asuvorov**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "login": "anylogin",
    "roles": [
      "GlobalAdmin"
    ],
    "timeZone": "GMT+2",
    "temperatureUnits": "Celsius"
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
      "login": "u2",
      "fullName": "A1"
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
  "user": "anylogin",
  "password": "abrakadabra",
  "firstName": "firstname",
  "lastName": "LastName",
  "position": "Manager",
  "email": "abra@cada.bra",
  "phone": "1111111117",
  "temperatureUnits": "Fahrenheit",
  "timeZone": "GMT+02:00"
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
      "timeOutMinutes": 0
    },
    {
      "type": "CriticalHot",
      "temperature": 17.0,
      "timeOutMinutes": 1
    },
    {
      "type": "CriticalCold",
      "temperature": -12.0,
      "timeOutMinutes": 0
    },
    {
      "type": "CriticalCold",
      "temperature": -11.0,
      "timeOutMinutes": 1
    },
    {
      "type": "Hot",
      "temperature": 6.0,
      "timeOutMinutes": 0
    },
    {
      "type": "Hot",
      "temperature": 7.0,
      "timeOutMinutes": 2
    },
    {
      "type": "Cold",
      "temperature": -7.0,
      "timeOutMinutes": 40
    },
    {
      "type": "Cold",
      "temperature": -5.0,
      "timeOutMinutes": 55
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
        "timeOutMinutes": 0
      },
      {
        "id": 983,
        "type": "CriticalHot",
        "temperature": 17.0,
        "timeOutMinutes": 1
      },
      {
        "id": 984,
        "type": "CriticalCold",
        "temperature": -12.0,
        "timeOutMinutes": 0
      },
      {
        "id": 985,
        "type": "CriticalCold",
        "temperature": -11.0,
        "timeOutMinutes": 1
      },
      {
        "id": 986,
        "type": "Hot",
        "temperature": 6.0,
        "timeOutMinutes": 0
      },
      {
        "id": 987,
        "type": "Hot",
        "temperature": 7.0,
        "timeOutMinutes": 2
      },
      {
        "id": 988,
        "type": "Cold",
        "temperature": -7.0,
        "timeOutMinutes": 40
      },
      {
        "id": 989,
        "type": "Cold",
        "temperature": -5.0,
        "timeOutMinutes": 55
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
      "alertProfileId": 179,
      "alertProfileName": "AnyAlert",
      "alertProfileDescription": "Any description",
      "watchBatteryLow": true,
      "watchEnterBrightEnvironment": true,
      "watchEnterDarkEnvironment": true,
      "watchMovementStart": true,
      "watchMovementStop": true,
      "temperatureIssues": [
        {
          "id": 958,
          "type": "CriticalHot",
          "temperature": 18.0,
          "timeOutMinutes": 0
        },
        {
          "id": 959,
          "type": "CriticalHot",
          "temperature": 17.0,
          "timeOutMinutes": 1
        },
        {
          "id": 960,
          "type": "CriticalCold",
          "temperature": -12.0,
          "timeOutMinutes": 0
        },
        {
          "id": 961,
          "type": "CriticalCold",
          "temperature": -11.0,
          "timeOutMinutes": 1
        },
        {
          "id": 962,
          "type": "Hot",
          "temperature": 6.0,
          "timeOutMinutes": 0
        },
        {
          "id": 963,
          "type": "Hot",
          "temperature": 7.0,
          "timeOutMinutes": 2
        },
        {
          "id": 964,
          "type": "Cold",
          "temperature": -7.0,
          "timeOutMinutes": 40
        },
        {
          "id": 965,
          "type": "Cold",
          "temperature": -5.0,
          "timeOutMinutes": 55
        }
      ]
    },
    {
      "alertProfileId": 180,
      "alertProfileName": "AnyAlert",
      "alertProfileDescription": "Any description",
      "watchBatteryLow": true,
      "watchEnterBrightEnvironment": true,
      "watchEnterDarkEnvironment": true,
      "watchMovementStart": true,
      "watchMovementStop": true,
      "temperatureIssues": [
        {
          "id": 966,
          "type": "CriticalHot",
          "temperature": 18.0,
          "timeOutMinutes": 0
        },
        {
          "id": 967,
          "type": "CriticalHot",
          "temperature": 17.0,
          "timeOutMinutes": 1
        },
        {
          "id": 968,
          "type": "CriticalCold",
          "temperature": -12.0,
          "timeOutMinutes": 0
        },
        {
          "id": 969,
          "type": "CriticalCold",
          "temperature": -11.0,
          "timeOutMinutes": 1
        },
        {
          "id": 970,
          "type": "Hot",
          "temperature": 6.0,
          "timeOutMinutes": 0
        },
        {
          "id": 971,
          "type": "Hot",
          "temperature": 7.0,
          "timeOutMinutes": 2
        },
        {
          "id": 972,
          "type": "Cold",
          "temperature": -7.0,
          "timeOutMinutes": 40
        },
        {
          "id": 973,
          "type": "Cold",
          "temperature": -5.0,
          "timeOutMinutes": 55
        }
      ]
    }
  ],
  "totalCount": 2
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
      "user": "asuvorov",
      "pushToMobileApp": true,
      "fromTime": 1,
      "toTime": 17,
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
      "user": "asuvorov",
      "pushToMobileApp": true,
      "fromTime": 1,
      "toTime": 17,
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
    "notificationScheduleId": 231,
    "notificationScheduleName": "Sched",
    "schedules": [
      {
        "personScheduleId": 285,
        "user": "asuvorov",
        "pushToMobileApp": true,
        "fromTime": 1,
        "toTime": 17,
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
        "personScheduleId": 286,
        "user": "asuvorov",
        "pushToMobileApp": true,
        "fromTime": 1,
        "toTime": 17,
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
**GET /vf/rest/getShipments/${accessToken}?pageSize=1&pageIndex=3**  
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
