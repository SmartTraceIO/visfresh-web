# Visfresh Rest Service

### Date format:###
The date should have following format `yyyy-MM-dd'T'HH:mm` in current user's time zone if the user is logged in now and UTC time zone otherwise. Example:
`2015-09-30T01:19`
### Requests and responses:###
The GET request parameters of URL link should be URL encoded to, but JSON body of request and response should be
sent as is without URL encoding.  
For all POST JSON requests the “Content-Type: application/json” HTTP header should be used.
### Server [Responses](#response-message):###
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
1. [Authentication token](#authentication-token)  
2. [Alert Profile](#alert-profile)  
3. [Notification Schedule](#notification-schedule)   
4. [Location](#location)  
5. [Shipment Template](#shipment-template)  
6. [Device](#device)  
7. [Shipment](#shipment)  
8. [Notification](#notification)  
9. [Alert](#alert)  
10. [Temperature Alert](#temperature-alert)  
11. [Arrival](#arrival)  
12. [Device Event](#device-event)  

## Rest Service methods.
1. [Authentication](#authentication).  
2. [Get access token using existing GTS(e) session.](#get-access-token-using-existing-gts-e-session)  
3. [Get User Info](#get-user-info)  
4. [Update User details](#update-user-details)  
5. [Logout](#logout)  
6. [Refresh access token](#refresh-access-token)  
7. [Save alert profile](#save-alert-profile)  
8. [Get Alert Profile](#get-alert-profile)  
9. [Get Alert Profiles](#get-alert-profiles)  
10. [Delete Alert Profile](#delete-alert-profile)  
11. [Save notification schedule](#save-notification-schedule)  
12. [Get notification schedules](#get-notification-schedules)  
13. [Get Notification Schedule](#get-notification-schedule)  
14. [Delete Notification Schedule](#delete-notification-schedule)  
15. [Save Location](#save-location)  
16. [Get Locations](#get-locations)  
17. [Get Location](#get-location)  
18. [Delete Location](#delete-location)  
19. [Save Shipment Template](#save-shipment-template)  
20. [Get Shipment templates](#get-shipment-templates)  
21. [Get Shipment Template](#get-shipment-template)  
22. [Delete Shipment Template](#delete-shipment-template)  
23. [Save Shipment](#save-shipment)  
24. [Get Shipments](#get-shipments)  
25. [Get Shipment](#get-shipment)  
26. [Delete Shipment](#delete-shipment)  
27. [Save Device](#save-device)  
28. [Get Device](#get-device)  
29. [Get Devices](#get-devices)  
30. [Delete Device](#delete-device)  
31. [Get Notifications](#get-notifications)  
32. [Send Command to Device](#send-command-to-device)  
33. [Mark Notification as read](#mark-notification-as-read)  
34. [Get Profile](#get-profile)  
35. [Save Profile](#save-profile)  

## Reports ##
1. [Get Single Shipment](#get-single-shipment)

### Authentication.###
Method *GET*, method name *login*, request parameters login - the user login name and password - the user password  
1. login - user name of logged in user  
2. password - password  
Returns [Authentication token](#authentication-token).  
[(example)](#authentication-request-example)  

### Get access token using existing GTS(e) session.###
The user should be logged in to GTS(e). (not implemented now).
Method *POST*, method name *getToken*, no parameters. In case of this request the service access a current user session, determines user info, log in as REST service user and returns authentication session.  
[(example)](#attach-to-existing-session-example)

### Get User Info ###
Method *GET*, method name *getUser*, method parameters  
1. username - name of user  

Method required associated privileges. The logged in user should be some as requested info user or should have admin role.  
Method returns:  
1. login - user login name  
2. fullName - full user name  
3. roles - array of user roles, one from GlobalAdmin, CompanyAdmin, Dispatcher, ReportViewer
4. timeZone - user type zone.  
5. temperatureUnits - temperature units.  
[(example)](#get-user-info-example)

### Update User Details ###
Method *POST*, method name *updateUserDetails*. JSON request body contains following properties:  
1. fullName - full user name.  
2. password - user password.  
3. user - user login name. It is not changeable parameter. Is used for identify the user to change details.  
4. temperatureUnits - user temparature units.  
5. timeZone - user time zone.  
[(example)](#update-user-details-example)

### Logout ###
Method *GET*, method name *logout*, have not parameters. Closes user REST session and clears all associated resources  
[(example)](#logout-example)

### Refresh access token ###
Method *GET*, method name *refreshToken*, have not parameters. Refresh the access token for current REST session.  
[(example)](#refresh-access-token)

### Get Alert Profiles ###
Method *GET*, method name *getAlertProfiles*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
3. sc - sort column  
4. so - sort order (asc/desc)  
Returns an array of [Alert Profile objects](#alert-profile).  
[(example)](#get-alert-profiles-example)

### Get Alert Profile ###
Method *GET*, method name getAlertProfile. Request parameters:  
1. alertProfileId - alert profile ID.  
Returns [Alert Profile Object](#alert-profile).  
[(example)](#get-alert-profile-example)

### Save alert profile ###
Method *POST*, method name *saveAlertProfile*, request body contains JSON serialized [Alert Profile object](#alert-profile). Response contains ID of just saved Alert Profile.  
[(example)](#save-alert-profile-example)

### Delete Alert Profile ###
Method *GET*, method name *deleteAlertProfile*, method parameters:  
1. alertProfileId - alert profile ID  
[(example)](#delete-alert-profile-example)

### Save Notification Schedule ###
Method *POST*, method name *saveNotificationSchedule*, request body contains JSON serialized [Notification Schedule object](#notification-schedule). Response contains ID of just saved Notification Schedule.  
[(example)](#save-notification-schedule-example)]

### Get Notification Schedules ###
Method *GET*, method name *getNotificationSchedules*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
3. sc - sort column  
4. so - sort order (asc/desc)  
Return array of [Notification Schedule objects](#notification-schedule)  
[(example)](#get-notification-schedules-example)

### Get Notification Schedule ###
Method *GET*, method name *getNotificationSchedule*. Request parameters:  
1. notificationScheduleId - notification schedule ID.  
Returns [Notification Schedule Object](#notification-schedule)  
[(example)](#get-notification-schedule-example)

### Delete Notification schedule ###
Method *GET*, *deleteNotificationSchedule*. Request parameters:  
1. notificationScheduleId - notification schedule ID.  
[(example)](#delete-notification-schedule-example)

### Save Location ###
Method *POST*, method name *saveLocation*, request body contains JSON serialized [Location Object](#location). Response contains ID of just saved Location  
[(example)](#save-location-example)

### Get Locations ###
Method *GET*, method name *getLocations*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
3. sc - sort column  
4. so - sort order  
Returns array of [Location Objects](#location)  
[(example)](#get-locations-example)

### Get Location ###
Method *GET*, method name *getLocation*. Request parameters:  
1. locationId - Location ID.  
Returns [Location Object](#location).  
[(example)](#get-location-example)

### Delete Location ###
Method *GET*, method name *deleteLocation*. Request parameters:  
1. locationId - Location ID.  
[(example)](#delete-location-example)

### Save Shipment template ###
Method *POST*, method name *saveShipmentTemplate*, request body contains JSON serialized [Shipment Template Object](#shipment-template). Response contains ID of just saved Shipment Template  
[(example)](#save-shipment-template-example)

### Get Shipment templates ###
Method *GET*, method name *getShipmentTemplates*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Shipment Template Objects](#shipment-template)  
[(example)](#get-shipment-templates-example)

### Get Shipment Template ###
Method *GET*, method name *getShipmentTemplate*. Request parameters:  
1. id - shipment template ID.  
Returns [Shipment Template Object](#shipment-template)  
[(example)](#get-shipment-template-example)

### Delete Shipment Template ###
Method *GET*, method name *deleteShipmentTemplate*, Request parameters:  
1. shipmentTemplateId - shipment template ID.  
[(example)](#delete-shipment-template-example)

### Save Device ###
Method *POST*, method name *saveDevice*, request body contains JSON serialized [Device Object](#device).  
[(example)](#save-device-example)

### Get Devices ###
Method *GET*, method name *getDevices*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Device Objects](#device).  
[(example)](#get-devices-example)

### Get Device ###
Method *GET*, method name *getDevice*. Request parameters:
1. imei - device IMEI.  
Returns [Device Object](#device)  
[(example)](#get-device-example)

### Delete Device ###
Method *GET*, method name *deleteDevice*. Request parameters:
1. imei - device IMEI.  
[(example)](#delete-device-example)

### Save Shipment ###
Method *POST*, method name saveShipment, request body contains JSON serialized [Save Shipment request](#save-shipment-request). Response contains ID of just saved Shipment and ID of shipment template if the shipment was saved with corresponding option.  
[(example)](#save-shipment-example)

### Get Shipment ###
Method *GET*, method name *getShipment*. Request parameters:  
1. id - shipment ID.  
Returns [Shipment Object](#shipment)  
[(example)](#get-shipment-example)

### Get Shipments ###
Method *GET*, method name getShipments, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Shipment Description Objects](#shipment-description), it is not same as [Shipment Object](#shipment).  
[(example)](#get-shipments-example)

### Delete Shipment ###
Method *GET*, method name deleteShipment, method parameters:  
1. shipmentId - shipment ID  
[(example)](#delete-shipment-example)

### Get Single Shipment ###
Method *GET*, method *getSingleShipment*. Request parameters:  
1 fromDate start selection data  
2. toDate end selection data  
3. shipment shipment ID  
[(example)](#get-single-shipment-example)

### Get Notifications ###
Method *GET*, method name getNotifications, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Notification Objects](#notification)  
[(example)](#get-notifications-example)

### Mark Notification as read ###
Method *POST*, method name *markNotificationsAsRead*. Request body contains JSON array of notification ID.  
[(example)](#mark-notification-as-read-example)

### Send Command to Device ###
Method *POST*, method name *sendCommandToDevice*. Request body contains [Device](#device) ID and device specific command.  
[(example)](#send-command-to-device-example)

### Get Profile ###
Method *GET*, method name *getProfile*, have not parameters. Return [Profile Object](#profile-object)
of current logged in user  
[(example)](#get-profile-example)

### Save Profile ###
Method *POST*, method name *saveProfile*. Request body contains JSON serialized [Profile Object](#profile-object)  
[(example)](#save-profile-example)

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
}
```
see [ResponseStatus](#response-status)
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
  "alertProfileId": 2,
  "alertProfileName": "AnyAlert",
  "alertProfileDescription": "Any description",
  "highTemperature": 5.0,
  "highTemperatureMinutes": 0,
  "highTemperature2": 4.0, //optional
  "highTemperatureMinutes2": 2, //optional
  "criticalHighTemperature": 10.0,
  "criticalHighTemperatureMinutes": 0,
  "criticalHighTemperature2": 9.0, //optional
  "criticalHighTemperatureMinutes2": 1, //optional
  "lowTemperature": -10.0,
  "lowTemperatureMinutes": 40,
  "lowTemperature2": -8.0, //optional
  "lowTemperatureMinutes2": 55, //optional
  "criticalLowTemperature": -15.0,
  "criticalLowTemperatureMinutes": 0,
  "criticalLowTemperature2": -14.0, //optional
  "criticalLowTemperatureMinutes2": 1, //optional
  "watchBatteryLow": true,
  "watchEnterBrightEnvironment": true,
  "watchEnterDarkEnvironment": true,
  "watchMovementStart": true,
  "watchMovementStop": true
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
[(See Person Schedule)](#person-schedule)
### Person Schedule ###
```json
{
  "personScheduleId": 13,
  "firstName": "Alexander",
  "lastName": "Suvorov",
  "company": "Sun",
  "position": "Generalisimus",
  "emailNotification": "asuvorov@sun.com",
  "smsNotification": "1111111117",
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
  "name": "JUnit-tpl",
  "shipmentDescription": "Any Description",
  "alertSuppressionDuringCoolDown": 55,
  "alertProfile": 2,
  "alertsNotificationSchedules": [ // array of ID of [notification schedules](#notification-schedule)
    3
  ],
  "arrivalNotificationWithIn": 11,
  "arrivalNotificationSchedules": [ // array of ID of [notification schedules](#notification-schedule)
    6
  ],
  "excludeNotificationsIfNoAlertsFired": true,
  "shippedFrom": 9,
  "shippedTo": 10,
  "shutdownDevice": 155,
  "assetType": "SeaContainer",
  "addDateShipped": true,
  "useCurrentTimeForDateShipped": true,
  "detectLocationForShippedFrom": true
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
see [Shipment Object](#shipment)
### Shipment ###
```json
{
    "name": "Shipment-1",
    "shipmentDescription": "Any Description",
    "alertSuppressionDuringCoolDown": 55,
    "alertProfile": 2,
    "alertsNotificationSchedules": [ //array of ID of notification schedule objects
      3
    ],
    "arrivalNotificationWithIn": 111,
    "arrivalNotificationSchedules": [//array of ID of notification schedule objects
      6
    ],
    "excludeNotificationsIfNoAlertsFired": true,
    "shippedFrom": 9,
    "shippedTo": 10,
    "shutdownDevice": 155,
    "assetType": "SeaContainer",
    "palletId": "palettid",
    "tripCount": 88,
    "poNum": 893793487,
    "assetNum": "10515",
    "shipmentDate": "2015-10-16T22:55",
    "customFields": {  //map of custom fields
      "field1": "value1"
    },
    "status": "Default", //status Default/InProgress/Completed/Pending
    "device": "234908720394857"
  }
```
### Shipment Description ###
```json
{
  "shipmentId": 11, //ID of associated shipment object
  "status": "InProgress", //status Default/InProgress/Completed/Pending
  "deviceSN": "394857",
  "deviceName": "Device Name",
  "tripCount": 88,
  "shipmentDescription": "Any Description",
  "palletId": "palettid",
  "assetNum": "10515",
  "assetType": "SeaContainer",
  "shippedFrom": "Bankstown Warehouse",
  "shipmentDate": "2015-10-23T03:06",
  "shippedTo": "Coles Perth DC",
  "estArrivalDate": "2015-10-23T03:06",
  "percentageComplete": 0,
  "alertProfileId": 2,
  "alertProfileName": "AnyAlert",
  "alertSummary": {
    "CriticalHot": "1",
    "Battery": "2",
    "LightOn": "1",
    "Cold": "1",
    "MovementStart": "4",
    "CriticalCold": "3",
    "LightOff": "1",
    "Hot": "2"
  }
}
```
See also [Shipment Object](#shipment)
### Notification ###
`{`  
`"id": 18,`  
`"type": "Arrival", // notification type (Alert|Arrival)`  
`"issue":` [Ordinary Alert Object](#alert) ` or ` [Temperature Alert Object](#temperature-alert) ` or ` [Arrival Object](#arrival)  
`}`
### Alert ###
`{`  
`"description": "Battery Low alert",`  
`"name": "Battery-1",`  
`"id": 15,`  
`"date": "2015-10-12T23:57",`  
`"device": "234908720394857", // ID of associated ` [Device](#device)  
`"type": "BatteryLow" // alert type: (EnterBrightEnvironment|EnterDarkEnvironment|Shock|BatteryLow)`  
`}`
### Temperature Alert###
`{`  
`"id": 14,`  
`"type": "Alert",`  
`"issue": {`  
`"description": "Temp Alert",`  
`"name": "TempAlert-1",`  
`"id": 13,`  
`"date": "2015-10-12T23:57",`  
`"device": "234908720394857", // ID of associated ` [Device](#device)  
`"type": "HighTemperature" // alert type (LowTemperature|HighTemperature|CriticalLowTemperature|CriticalHighTemperature)`  
`"temperature": 5.0, //temperature is Celsius degree`  
`"minutes": 55 //number of minutes for this temperature`  
`}`  
`}`
### Arrival ###
`{`  
`"id": 17,`  
`"numberOfMetersOfArrival": 1500, //number of meters of arrival`    
`"date": "2015-10-12T23:57",`  
`"device": "234908720394857" //ID of associated` [Device Object](#device)  
`}`
### Device Event ###
`{`  
`"battery": 1234, //battery level`  
`"id": 13,`  
`"temperature": 56.0, //temperature in Celsius degree`  
`"time": "2015-10-13T00:04",`  
`"type": "AUT" //device specific status`  
`}`
### Profile Object ###
```json
{
    "shipments": [ //array of Shipment ID associated by given user
      77
    ]
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
### Update User Details example ###
**POST /vf/rest/updateUserDetails/${authToken}**  
**Request body:**  
```json
{
  "fullName": "Full User Name",
  "password": "abrakadabra",
  "user": "anylogin",
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
  "highTemperature": 5.0,
  "highTemperatureMinutes": 0,
  "highTemperature2": 4.0,
  "highTemperatureMinutes2": 2,
  "criticalHighTemperature": 10.0,
  "criticalHighTemperatureMinutes": 0,
  "criticalHighTemperature2": 9.0,
  "criticalHighTemperatureMinutes2": 1,
  "lowTemperature": -10.0,
  "lowTemperatureMinutes": 40,
  "lowTemperature2": -8.0,
  "lowTemperatureMinutes2": 55,
  "criticalLowTemperature": -15.0,
  "criticalLowTemperatureMinutes": 0,
  "criticalLowTemperature2": -14.0,
  "criticalLowTemperatureMinutes2": 1,
  "watchBatteryLow": true,
  "watchEnterBrightEnvironment": true,
  "watchEnterDarkEnvironment": true,
  "watchMovementStart": true,
  "watchMovementStop": true
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
    "alertProfileId": 2,
    "alertProfileName": "AnyAlert",
    "alertProfileDescription": "Any description",
    "highTemperature": 5.0,
    "highTemperatureMinutes": 0,
    "highTemperature2": 4.0,
    "highTemperatureMinutes2": 2,
    "criticalHighTemperature": 10.0,
    "criticalHighTemperatureMinutes": 0,
    "criticalHighTemperature2": 9.0,
    "criticalHighTemperatureMinutes2": 1,
    "lowTemperature": -10.0,
    "lowTemperatureMinutes": 40,
    "lowTemperature2": -8.0,
    "lowTemperatureMinutes2": 55,
    "criticalLowTemperature": -15.0,
    "criticalLowTemperatureMinutes": 0,
    "criticalLowTemperature2": -14.0,
    "criticalLowTemperatureMinutes2": 1,
    "watchBatteryLow": true,
    "watchEnterBrightEnvironment": true,
    "watchEnterDarkEnvironment": true,
    "watchMovementStart": true,
    "watchMovementStop": true
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
      "alertProfileId": 2,
      "alertProfileName": "AnyAlert",
      "alertProfileDescription": "Any description",
      "highTemperature": 5.0,
      "highTemperatureMinutes": 0,
      "highTemperature2": 4.0,
      "highTemperatureMinutes2": 2,
      "criticalHighTemperature": 10.0,
      "criticalHighTemperatureMinutes": 0,
      "criticalHighTemperature2": 9.0,
      "criticalHighTemperatureMinutes2": 1,
      "lowTemperature": -10.0,
      "lowTemperatureMinutes": 40,
      "lowTemperature2": -8.0,
      "lowTemperatureMinutes2": 55,
      "criticalLowTemperature": -15.0,
      "criticalLowTemperatureMinutes": 0,
      "criticalLowTemperature2": -14.0,
      "criticalLowTemperatureMinutes2": 1,
      "watchBatteryLow": true,
      "watchEnterBrightEnvironment": true,
      "watchEnterDarkEnvironment": true,
      "watchMovementStart": true,
      "watchMovementStop": true
    },
    {
      "alertProfileId": 3,
      "alertProfileName": "AnyAlert",
      "alertProfileDescription": "Any description",
      "highTemperature": 5.0,
      "highTemperatureMinutes": 0,
      "highTemperature2": 4.0,
      "highTemperatureMinutes2": 2,
      "criticalHighTemperature": 10.0,
      "criticalHighTemperatureMinutes": 0,
      "criticalHighTemperature2": 9.0,
      "criticalHighTemperatureMinutes2": 1,
      "lowTemperature": -10.0,
      "lowTemperatureMinutes": 40,
      "lowTemperature2": -8.0,
      "lowTemperatureMinutes2": 55,
      "criticalLowTemperature": -15.0,
      "criticalLowTemperatureMinutes": 0,
      "criticalLowTemperature2": -14.0,
      "criticalLowTemperatureMinutes2": 1,
      "watchBatteryLow": true,
      "watchEnterBrightEnvironment": true,
      "watchEnterDarkEnvironment": true,
      "watchMovementStart": true,
      "watchMovementStop": true
    }
  ]
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
      "firstName": "Alexander",
      "lastName": "Suvorov",
      "company": "Sun",
      "position": "Generalisimus",
      "emailNotification": "asuvorov@sun.com",
      "smsNotification": "1111111117",
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
      "firstName": "Alexander",
      "lastName": "Suvorov",
      "company": "Sun",
      "position": "Generalisimus",
      "emailNotification": "asuvorov@sun.com",
      "smsNotification": "1111111117",
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
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": {`  
`"id": 2`  
`}`  
`}`  
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
      "notificationScheduleDescription": "JUnit schedule",
      "notificationScheduleId": 2,
      "notificationScheduleName": "Sched",
      "schedules": [
        {
          "personScheduleId": 3,
          "firstName": "Alexander",
          "lastName": "Suvorov",
          "company": "Sun",
          "position": "Generalisimus",
          "emailNotification": "asuvorov@sun.com",
          "smsNotification": "1111111117",
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
          "personScheduleId": 4,
          "firstName": "Alexander",
          "lastName": "Suvorov",
          "company": "Sun",
          "position": "Generalisimus",
          "emailNotification": "asuvorov@sun.com",
          "smsNotification": "1111111117",
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
    },
    {
      "notificationScheduleDescription": "JUnit schedule",
      "notificationScheduleId": 5,
      "notificationScheduleName": "Sched",
      "schedules": [
        {
          "personScheduleId": 6,
          "firstName": "Alexander",
          "lastName": "Suvorov",
          "company": "Sun",
          "position": "Generalisimus",
          "emailNotification": "asuvorov@sun.com",
          "smsNotification": "1111111117",
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
          "personScheduleId": 7,
          "firstName": "Alexander",
          "lastName": "Suvorov",
          "company": "Sun",
          "position": "Generalisimus",
          "emailNotification": "asuvorov@sun.com",
          "smsNotification": "1111111117",
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
  ]
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
    "notificationScheduleId": 2,
    "notificationScheduleName": "Sched",
    "schedules": [
      {
        "personScheduleId": 3,
        "firstName": "Alexander",
        "lastName": "Suvorov",
        "company": "Sun",
        "position": "Generalisimus",
        "emailNotification": "asuvorov@sun.com",
        "smsNotification": "1111111117",
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
        "personScheduleId": 4,
        "firstName": "Alexander",
        "lastName": "Suvorov",
        "company": "Sun",
        "position": "Generalisimus",
        "emailNotification": "asuvorov@sun.com",
        "smsNotification": "1111111117",
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
Response:
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
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": {`  
`"id": 2`  
`}`  
`}`  
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
  ]
}
```  
### Save Shipment Template example ###
**POST /vf/rest/saveShipmentTemplate/${accessToken}**  
**Request body:**  
```json
{
  "saveAsNewTemplate": true,
  "templateName": "NewTemplate.tpl",
  "shipment": {
    "name": "Shipment-1",
    "shipmentDescription": "Any Description",
    "alertSuppressionDuringCoolDown": 55,
    "alertProfile": 2,
    "alertsNotificationSchedules": [
      3
    ],
    "arrivalNotificationWithIn": 111,
    "arrivalNotificationSchedules": [
      6
    ],
    "excludeNotificationsIfNoAlertsFired": true,
    "shippedFrom": 9,
    "shippedTo": 10,
    "shutdownDevice": 155,
    "assetType": "SeaContainer",
    "palletId": "palettid",
    "tripCount": 88,
    "poNum": 893793487,
    "assetNum": "10515",
    "shipmentDate": "2015-10-16T22:55",
    "customFields": {
      "field1": "value1"
    },
    "status": "Default",
    "device": "234908720394857"
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
    "id": 11
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
      "name": "JUnit-tpl",
      "shipmentDescription": "Any Description",
      "alertSuppressionDuringCoolDown": 55,
      "id": 11,
      "alertProfile": 2,
      "alertsNotificationSchedules": [
        3
      ],
      "arrivalNotificationWithIn": 11,
      "arrivalNotificationSchedules": [
        6
      ],
      "excludeNotificationsIfNoAlertsFired": true,
      "shippedFrom": 9,
      "shippedTo": 10,
      "shutdownDevice": 155,
      "assetType": "SeaContainer",
      "addDateShipped": true,
      "useCurrentTimeForDateShipped": true,
      "detectLocationForShippedFrom": true
    }
  ]
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
    "name": "Shipment-1",
    "shipmentDescription": "Any Description",
    "alertSuppressionDuringCoolDown": 55,
    "alertProfile": 2,
    "alertsNotificationSchedules": [
      3
    ],
    "arrivalNotificationWithIn": 111,
    "arrivalNotificationSchedules": [
      6
    ],
    "excludeNotificationsIfNoAlertsFired": true,
    "shippedFrom": 9,
    "shippedTo": 10,
    "shutdownDevice": 155,
    "assetType": "SeaContainer",
    "palletId": "palettid",
    "assetNum": "10515",
    "shipmentDate": "2015-10-09T11:47",
    "customFields": "customFields",
    "status": "Default",
    "devices": [
      "234908720394857",
      "329847983724987"
    ]
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
```gson
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "shipmentId": 11,
      "status": "InProgress",
      "deviceSN": "394857",
      "deviceName": "Device Name",
      "tripCount": 88,
      "shipmentDescription": "Any Description",
      "palletId": "palettid",
      "assetNum": "10515",
      "assetType": "SeaContainer",
      "shippedFrom": "Bankstown Warehouse",
      "shipmentDate": "2015-10-23T03:06",
      "shippedTo": "Coles Perth DC",
      "estArrivalDate": "2015-10-23T03:06",
      "percentageComplete": 0,
      "alertProfileId": 2,
      "alertProfileName": "AnyAlert",
      "alertSummary": {
        "CriticalHot": "1",
        "Battery": "2",
        "LightOn": "1",
        "Cold": "1",
        "MovementStart": "4",
        "CriticalCold": "3",
        "LightOff": "1",
        "Hot": "2"
      }
    },
    {
      "shipmentId": 21,
      "status": "InProgress",
      "deviceSN": "394857",
      "deviceName": "Device Name",
      "tripCount": 88,
      "shipmentDescription": "Any Description",
      "palletId": "palettid",
      "assetNum": "10515",
      "assetType": "SeaContainer",
      "shippedFrom": "Bankstown Warehouse",
      "shipmentDate": "2015-10-23T03:06",
      "shippedTo": "Coles Perth DC",
      "estArrivalDate": "2015-10-23T03:06",
      "percentageComplete": 0,
      "alertProfileId": 12,
      "alertProfileName": "AnyAlert",
      "alertSummary": {}
    }
  ]
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
```  
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
  ]
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
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": [`  
`{`  
`"id": 18,`  
`"type": "Arrival",`  
`"issue": {`  
`"id": 17,`  
`"numberOfMetersOfArrival": 1500,`  
`"date": "2015-10-12T23:57",`  
`"device": "234908720394857"`  
`}`  
`},`  
`{`  
`"id": 16,`  
`"type": "Alert",`  
`"issue": {`  
`"description": "Battery Low alert",`  
`"name": "Battery-1",`  
`"id": 15,`  
`"date": "2015-10-12T23:57",`  
`"device": "234908720394857",`  
`"type": "BatteryLow"`  
`}`  
`},`  
`{`  
`"id": 14,`  
`"type": "Alert",`  
`"issue": {`  
`"description": "Temp Alert",`  
`"name": "TempAlert-1",`  
`"id": 13,`  
`"date": "2015-10-12T23:57",`  
`"device": "234908720394857",`  
`"type": "HighTemperature",`  
`"temperature": 5.0,`  
`"minutes": 55`  
`}`  
`}`  
`]`  
`}`
### Mark Notification as read example ###
**POST /vf/rest/markNotificationsAsRead/${accessToken}**    
**Request body:**  
`[`  
`16, //notification ID`  
`18`  
`]`  
**Response:**  
`{` 
`"status": {` 
`"code": 0,` 
`"message": "Success"` 
`}`  
`}`
### Send Command to Device example ###
**POST /vf/rest/sendCommandToDevice/${accessToken}**  
**Request body:**  
`{`  
`"device": "089723409857032498",`  
`"command": "shutdown"`  
`}`  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`}`  
`}`  
```
### Get Location example ###
**GET /vf/rest/getLocation/${accessToken}?locationId=2**  
**Response:**  
```json
Response:
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
**GET /rest/getShipmentTemplate/${accessToken}?id=77**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "name": "JUnit-tpl",
    "shipmentDescription": "Any Description",
    "alertSuppressionDuringCoolDown": 55,
    "id": 77,
    "alertProfile": 78,
    "alertsNotificationSchedules": [
      91
    ],
    "arrivalNotificationWithIn": 11,
    "arrivalNotificationSchedules": [
      92
    ],
    "excludeNotificationsIfNoAlertsFired": true,
    "shippedFrom": 79,
    "shippedTo": 80,
    "shutdownDevice": 155,
    "assetType": "SeaContainer",
    "addDateShipped": true,
    "useCurrentTimeForDateShipped": true,
    "detectLocationForShippedFrom": true
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
    "name": "Shipment-1",
    "shipmentDescription": "Any Description",
    "alertSuppressionDuringCoolDown": 55,
    "alertProfile": 2,
    "alertsNotificationSchedules": [
      3
    ],
    "arrivalNotificationWithIn": 111,
    "arrivalNotificationSchedules": [
      6
    ],
    "excludeNotificationsIfNoAlertsFired": true,
    "shippedFrom": 9,
    "shippedTo": 10,
    "shutdownDevice": 155,
    "assetType": "SeaContainer",
    "palletId": "palettid",
    "tripCount": 88,
    "poNum": 893793487,
    "assetNum": "10515",
    "shipmentDate": "2015-10-16T22:55",
    "customFields": {
      "field1": "value1"
    },
    "status": "Default",
    "device": "234908720394857"
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
### Get Profile example ###
**GET /vf/rest/getProfile/${accessToken}**  
**Response:**  
```json
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "shipments": [
      77
    ]
  }
}
```
### Save Profile example ###
**POST /vf/rest/saveProfile/${accessToken}**  
**Request body:**  
```json
{
  "shipments": [
    77
  ]
}
Response:
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
    "currentLocation": "Not determined",
    "deviceSN": "394857",
    "deviceName": "Device Name",
    "tripCount": 88,
    "shipmentDescription": "Any Description",
    "palletId": "palettid",
    "assetNum": "10515",
    "assetType": "SeaContainer",
    "poNum": 893793487,
    "shippedFrom": 9,
    "shippedTo": 10,
    "estArrivalDate": "2015-11-05T23:05",
    "actualArrivalDate": "2015-11-05T23:05",
    "percentageComplete": 0,
    "alertProfileId": 2,
    "alertProfileName": "AnyAlert",
    "maxTimesAlertFires": 4,
    "alertSuppressionMinutes": 55,
    "alertsNotificationSchedules": [
      3
    ],
    "alertSummary": {
      "Hot": "1"
    },
    "arrivalNotificationSchedules": [
      6
    ],
    "arrivalNotificationWithinKm": 111,
    "excludeNotificationIfNoAlerts": false,
    "items": [
      {
        "timestamp": "2015-11-03T23:05",
        "location": {
          "latitude": 50.5,
          "longitude": 51.51
        },
        "temperature": 56.0,
        "type": "AUT",
        "alerts": [],
        "arrivas": [
          {
            "numberOfMetersOfArrival": 400,
            "arrivalReportSentTo": ""
          }
        ]
      },
      {
        "timestamp": "2015-11-03T23:05",
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
        "arrivas": []
      }
    ]
  }
}
```
