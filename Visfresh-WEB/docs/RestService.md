# Visfresh Rest Service

### Date format:###
The date should have following format `yyyy-MM-dd'T'HH:mm:ss.SSSZ` with RFC 822 time zone as for requests as for responses. Example:
`2015-09-30T01:19:56.060+0300`
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
"expired": "2015-09-30T01:19:56.060+0300"
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
1. [Authentication token](#markdown-header-authentication-token-response)  
2. [Alert Profile](#markdown-header-alert-profile)  
3. [Notification Schedule](#markdown-header-notification-schedule)   
4. [Location Profile](#markdown-header-location-profile)  
5. [Shipment Template](#markdown-header-shipment-template)  
6. [Device](#markdown-header-device)  
7. [Shipment](#markdown-header-shipment)  
8. [Notification](#markdown-header-notification)  
9. [Alert](#markdown-header-alert)  
10. [Temperature Alert](#markdown-header-temperature-alert)  
11. [Arrival](#markdown-header-arrival)  
12. [Shipment data](#markdown-header-shipment-data)  
13. [Device Event](#markdown-header-device-event)  

## Rest Service methods.
1. [Authentication](#markdown-header-authentication).  
2. [Get access token using existing GTS(e) session.](#markdown-header-get-access-token-using-existing-gts-e-session)  
3. [Get User Info](#markdown-header-get-user-info)  
4. [Logout](#markdown-header-logout)  
5. [Refresh access token](#markdown-header-refresh-access-token)  
6. [Save alert profile](#markdown-header-save-alert-profile)  
7. [Get Alert Profiles](#markdown-header-get-alert-profiles)  
8. [Save notification schedule](#markdown-header-save-notification-schedule)  
9. [Get notification schedules](#markdown-header-get-notification-schedules)  
10. [Save Location](#markdown-header-save-location)  
11. [Get Locations](#markdown-header-get-locations)  
12. [Save Shipment Template](#markdown-header-save-shipment-template)  
13. [Get Shipment templates](#markdown-header-get-shipment-templates)  
14. [Save Device](#markdown-header-save-device)  
15. [Get Devices](#markdown-header-get-devices)  
16. [Save Shipment](#markdown-header-save-shipment)  
17. [Get Shipments](#markdown-header-get-shipments)  
18. [Get Notifications](#markdown-header-get-notifications)  
19. [Mark Notification as read](#markdown-header-mark-notification-as-read)  
20. [Get Shipment Data](#markdown-header-get-shipment-data)  
22. [Send Command to Device](#markdown-header-send-command-to-device)  
22. [Get Alert Profile](#markdown-header-get-alert-profile)  
23. [Get Location Profile](#markdown-header-get-location-profile)  
24. [Get Shipment Template](#markdown-header-get-shipment-template)  
25. [Get Device](#markdown-header-get-device)  
26. [Get Shipment](#markdown-header-get-shipment)  
27. [Send Notification Schedule](#markdown-header-get-notification-schedule)  
28. [Get Profile](#markdown-header-get-profile)  
29. [Save Profile](#markdown-header-save-profile)  


### Authentication.###
Method *GET*, method name *login*, request parameters login - the user login name and password - the user password  
1. login - user name of logged in user  
2. password - password  

are contained in [authentication request body](#markdown-header-authentication-request-body). Returns [Authentication token response](#markdown-header-authentication-token-response).  
[(example)](#markdown-header-authentication-request-example)  
### Get access token using existing GTS(e) session.###
The user should be logged in to GTS(e). (not implemented now).
Method *POST*, method name *getToken*, no parameters. In case of this request the service access a current user session, determines user info, log in as REST service user and returns authentication session.  
[(example)](#markdown-header-attach-to-existing-session-example)
### Get User Info ###
Method *GET*, method name *getUser*, method parameters  
1. username - name of user  

Method required associated privileges. The logged in user should be some as requested info user or should have admin role.  
Method returns:  
1. login - user login name  
2. fullName - full user name  
3. roles - array of user roles, one from GlobalAdmin, CompanyAdmin, Dispatcher, ReportViewer

[(example)](#markdown-header-get-user-info-example)

### Logout ###
Method *GET*, method name *logout*, have not parameters. Closes user REST session and clears all associated resources  
[(example)](#markdown-header-logout-example)

### Refresh access token ###
Method *GET*, method name *refreshToken*, have not parameters. Refresh the access token for current REST session.  
[(example)](#markdown-header-refresh-access-token)

### Save alert profile ###
Method *POST*, method name *saveAlertProfile*, request body contains JSON serialized [Alert Profile object](#markdown-header-alert-profile). Response contains ID of just saved Alert Profile.  
[(example)](#markdown-header-save-alert-profile-example)

### Get Alert Profiles ###
Method *GET*, method name *getAlertProfiles*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns an array of [Alert Profile objects](#markdown-header-alert-profile).  
[(example)](#markdown-header-get-alert-profiles-example)

### Save notification schedule ###
Method *POST*, method name *saveNotificationSchedule*, request body contains JSON serialized [Notification Schedule object](#markdown-header-notification-schedule). Response contains ID of just saved Notification Schedule.  
[(example)](#markdown-header-save-notification-schedule-example)]

### Get notification schedules ###
Method *GET*, method name *getNotificationSchedules*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Return array of [Notification Schedule objects](#markdown-header-notification-schedule)  
[(example)](#markdown-header-get-notification-schedules-example)

### Save Location ###
Method *POST*, method name *saveLocationProfile*, request body contains JSON serialized [Location Profile Object](#markdown-header-location-profile). Response contains ID of just saved Location Profile  
[(example)](#markdown-header-save-location-example)

### Get Locations ###
Method *GET*, method name *getLocationProfiles*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Location Profile Objects](#markdown-header-location-profile)  
[(example)](#markdown-header-get-location-profiles-example)

### Save Shipment template ###
Method *POST*, method name *saveShipmentTemplate*, request body contains JSON serialized [Shipment Template Object](#markdown-header-shipment-template). Response contains ID of just saved Shipment Template  
[(example)](#markdown-header-save-shipment-template-example)

### Get Shipment templates ###
Method *GET*, method name *getShipmentTemplates*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Shipment Template Objects](#markdown-header-shipment-template)  
[(example)](#markdown-header-get-shipment-templates-example)

### Save Device ###
Method *POST*, method name *saveDevice*, request body contains JSON serialized [Device Object](#markdown-header-device). Response contains ID of just saved Device  
[(example)](#markdown-header-save-device-example)

### Get Devices ###
Method *GET*, method name *getDevices*, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Device Objects](#markdown-header-device).  
[(example)](#markdown-header-get-devices-example)

### Save Shipment ###
Method *POST*, method name saveShipment, request body contains JSON serialized [Save Shipment request](#markdown-header-save-shipment-request). Response contains ID of just saved Shipment and ID of shipment template if the shipment was saved with corresponding option.  
[(example)](#markdown-header-save-shipment-example)

### Get Shipments ###
Method *GET*, method name getShipments, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Shipment Objects](#markdown-header-shipment).  
[(example)](#markdown-header-get-shipments-example)

### Get Notifications ###
Method *GET*, method name getNotifications, method parameters:  
1. pageIndex - number of page  
2. pageSize - size of page  
Returns array of [Notification Objects](#markdown-header-notification)  
[(example)](#markdown-header-get-notifications-example)

### Mark Notification as read ###
Method *POST*, method name *markNotificationsAsRead*. Request body contains JSON array of notification ID.  
[(example)](#markdown-header-mark-notification-as-read-example)

### Get Shipment Data ###
Method *GET*, method name *getShipmentData*. Request parameters:  
1. fromDate - start date in 'yyyy-MM-dd'T'HH:mm:ss.SSSZ' format  
2. toDate - end date in 'yyyy-MM-dd'T'HH:mm:ss.SSSZ' format  
3. onlyWithAlerts - only select shipment data with alerts flag  
Returns array of [Shipment Data Objects](#markdown-header-shipment-data)  
[(example)](#markdown-header-get-shipment-data-example)

### Send Command to Device ###
Method *POST*, method name *sendCommandToDevice*. Request body contains [Device](#markdown-header-device) ID and device specific command.  
[(example)](#markdown-header-send-command-to-device-example)

### Get Alert Profile ###
Method *GET*, method name getAlertProfile. Request parameters:  
1. id - alert profile ID.  
Returns [Alert Profile Object](#markdown-header-alert-profile).  
[(example)](#markdown-header-get-alert-profile-example)

### Get Location Profile ###
Method *GET*, method name *getLocationProfile*. Request parameters:  
1. id - alert profile ID.  
Returns [Location Profile Object](#markdown-header-location-profile).  
[(example)](#markdown-header-get-location-profile-example)

### Get Shipment Template ###
Method *GET*, method name *getShipmentTemplate*. Request parameters:  
1. id - shipment template ID.  
Returns [Shipment Template Object](#markdown-header-shipment-template)  
[(example)](#markdown-header-get-shipment-template-example)

### Get Device ###
Method *GET*, method name *getDevice*. Request parameters:
1. id - device ID.  
Returns [Device Object](#markdown-header-device)  
[(example)](#markdown-header-get-device-example)

### Get Shipment ###
Method *GET*, method name *getShipment*. Request parameters:  
1. id - shipment ID.  
Returns [Shipment Object](#markdown-header-shipment)  
[(example)](#markdown-header-get-shipment-example)

### Get Notification Schedule ###
Method *GET*, method name *getNotificationSchedule*. Request parameters:  
1. id - notification schedule ID.  
Returns [Notification Schedule Object](#markdown-header-notification-schedule)  
[(example)](#markdown-header-get-notification-schedule-example)

### Get Profile ###
Method *GET*, method name *getProfile*, have not parameters. Return [Profile Object](#markdown-header-profile-object)
of current logged in user  
[(example)](#markdown-header-get-profile-example)

### Save Profile ###
Method *POST*, method name *saveProfile*. Request body contains JSON serialized [Profile Object](#markdown-header-profile-object)  
[(example)](#markdown-header-save-profile-example)

## Objects
### Response message ###
`{` 
` "status": ` [ResponseStatus](#markdown-header-response-status)`,`  
`"response": {`  
`"token": "token_100001",`  
`"expired": "2015-09-30T01:19:56.060+0300"`  
`}`   
`}`
### Response status ###
`{`  
`"code": 0, // 0 - success, error for other codes`  
`"message": "Success"`  
`}`
### Authentication request body ###
`{`  
`"login": "asuvorov", //user name`  
`"password": "password" // password`  
`}`
### Authentication token response ###
`{`  
`"token": "token_100002", // authentication token`  
`"expired": "2015-10-12T23:39:29.946+0300" // expiration time`  
`}`
### Alert Profile ###
`{`  
`"id:" 77`
`"description": "Any description",`  
`"name": "AnyAlert",`  
`"criticalHighTemperatureForMoreThen": 0,`  
``"criticalHighTemperature": 5.0,`  
`"criticalLowTemperatureForMoreThen": 0,`  
`"criticalLowTemperature": -15.0,`  
`"highTemperature": 1.0,`  
`"highTemperatureForMoreThen": 55,`  
`"lowTemperature": -10.0,`  
`"lowTemperatureForMoreThen": 55,`  
`"watchBatteryLow": true,`  
`"watchEnterBrightEnvironment": true,`  
`"watchEnterDarkEnvironment": true,`  
`"watchShock": true`  
`}`
### Notification Schedule ###
`{`  
`"id:" 77`
`"description": "JUnit schedule",`  
`"name": "Sched",`  
`"schedules": [`  
[PersonalScheduleObject](#markdown-header-personal-schedule),  
[PersonalScheduleObject](#markdown-header-personal-schedule)  
`]`  
`}`
### Personal Schedule ###
`{`  
`"id:" 77`
`"company": "Sun",`  
`"emailNotification": "asuvorov@sun.com",`  
`"firstName": "Alexander",`  
`"lastName": "Suvorov",`  
`"position": "Generalisimus",`  
`"smsNotification": "1111111117",`  
`"toTime": 17,`  
`"fromTime": 1,`  
`"pushToMobileApp": true,`  
`"weekDays": [`  
`true,`  
`false,`  
`false,`  
`true,`  
`false,`  
`false,`  
`false`  
`]`  
`}`
### Location Profile ###
`{`  
`"id": 2,`  
`"company": "Sun Microsystems",`  
`"name": "Loc-1",`  
`"notes": "Any notes",`  
`"address": "Odessa, Deribasovskaya 1, apt.1",`  
`"location": {`  
`"lat": 100.5,`  
`"lon": 100.501`  
`},`  
`"radius": 1000`  
`}`
### Shipment Template ###
```
{
  "name": "JUnit-tpl",
  "shipmentDescription": "Any Description",
  "alertSuppressionDuringCoolDown": 55,
  "alertProfile": 2,
  "alertsNotificationSchedules": [ // array of ID of [notification schedules](#markdown-header-notification-schedule)
    3
  ],
  "arrivalNotificationWithIn": 11,
  "arrivalNotificationSchedules": [ // array of ID of [notification schedules](#markdown-header-notification-schedule)
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
`{`  
`"description": "Device description",`  
`"id": "1209898347987.123", // device ID`  
`"imei": "1209898347987", // device IMEI`  
`"name": "Device Name",`  
`"sn": "1"`  
`}`
### Save Shipment request ###
`{`  
`"saveAsNewTemplate": true,`  
`"templateName": "NewTemplate.tpl", // template name in case of save also as new template`  
`"shipment":` [Shipment Object](#markdown-header-shipment)
`}`
### Shipment ###
```
{
    "name": "Shipment-1",
    "shipmentDescription": "Any Description",
    "alertSuppressionDuringCoolDown": 55,
    "alertProfile": 2,
    "alertsNotificationSchedules": [ // Array of ID of [Notification Schedule Object](#markdown-header-notification-schedule)
      3
    ],
    "arrivalNotificationWithIn": 111,
    "arrivalNotificationSchedules": [ // Array of ID of [Notification Schedule Object](#markdown-header-notification-schedule)
      6
    ],
    "excludeNotificationsIfNoAlertsFired": true,
    "shippedFrom": 9,
    "shippedTo": 10,
    "shutdownDevice": 155,
    "assetType": "SeaContainer",
    "palletId": "palettid",
    "assetNum": "10515",
    "shipmentDate": "2015-10-09T11:47:21.945+0300",
    "customFields": "customFields",
    "status": "Default",
    "devices": [
      "234908720394857.123",
      "329847983724987.123"
    ]
}
```
### Notification ###
`{`  
`"id": 18,`  
`"type": "Arrival", // notification type (Alert|Arrival)`  
`"issue":` [Ordinary Alert Object](#markdown-header-alert) ` or ` [Temperature Alert Object](#markdown-header-temperature-alert) ` or ` [Arrival Object](#markdown-header-arrival)  
`}`
### Alert ###
`{`  
`"description": "Battery Low alert",`  
`"name": "Battery-1",`  
`"id": 15,`  
`"date": "2015-10-12T23:57:45.105+0300",`  
`"device": "234908720394857.123", // ID of associated ` [Device](#markdown-header-device)  
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
`"date": "2015-10-12T23:57:45.104+0300",`  
`"device": "234908720394857.123", // ID of associated ` [Device](#markdown-header-device)  
`"type": "HighTemperature" // alert type (LowTemperature|HighTemperature|CriticalLowTemperature|CriticalHighTemperature)`  
`"temperature": 5.0, //temperature is Celsius degree`  
`"minutes": 55 //number of minutes for this temperature`  
`}`  
`}`
### Arrival ###
`{`  
`"id": 17,`  
`"numberOfMetersOfArrival": 1500, //number of meters of arrival`    
`"date": "2015-10-12T23:57:45.105+0300",`  
`"device": "234908720394857.123" //ID of associated` [Device Object](#markdown-header-device)  
`}`
### Shipment data ###
`{`  
`"shipment": 11, // ID of associated` [Shipment](#markdown-header-shipment)  
`"data": [`  
`{`  
`"device": "234908720394857.123", // ID of` [Device](#markdown-header-device)
`"alerts": [], // array of` [Alert Objects](#markdown-header-alert)  
`"events": [] // array of ` [Device Event Objects](#markdown-header-device-event)
`}`  
`]`  
`}`
### Device Event ###
`{`  
`"battery": 1234, //battery level`  
`"id": 13,`  
`"temperature": 56.0, //temperature in Celsius degree`  
`"time": "2015-10-13T00:04:03.015+0300",`  
`"type": "AUT" //device specific status`  
`}`
### Profile Object ###
```
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
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": {`  
`"token": "token_100002",`  
`"expired": "2015-10-12T23:39:29.946+0300"`  
`}`  
`}`
### Attach to existing session example ###
**GET /vf/rest/getToken**  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": {`  
`"token": "token_100001",`  
`"expired": "2015-09-30T01:19:56.060+0300"`  
`}`  
`}`  
### Get user info example ###
**GET /vf/rest/getUser/${authToken}?username=asuvorov**  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": {`  
`"login": "asuvorov",`  
`"fullName": "Alexander Suvorov",`  
`"roles": [`  
`"Dispatcher",`  
`"ReportViewer"`  
`]`  
`}`  
`}`  
### Logout example ###
**GET /vf/rest/logout/${authToken}**  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`}`  
`}`  
### Refresh access token ###
**GET /vf/rest/refreshToken/${authToken}**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": {`  
`"token": "token_100001",`  
`"expired": "2015-09-30T01:19:56.060+0300"`  
`}`  
`}`  
### Save alert profile example ###
**POST  /vf/rest/saveAlertProfile/${accessToken}**  
**Request body:**  
`{`  
`"description": "Any description",`  
`"name": "AnyAlert",`  
`"criticalHighTemperatureForMoreThen": 0,`  
`"criticalHighTemperature": 5.0,`  
`"criticalLowTemperatureForMoreThen": 0,`  
`"criticalLowTemperature": -15.0,`  
`"highTemperature": 1.0,`  
`"highTemperatureForMoreThen": 55,`  
`"lowTemperature": -10.0,`  
`"lowTemperatureForMoreThen": 55,`  
`"watchBatteryLow": true,`  
`"watchEnterBrightEnvironment": true,`  
`"watchEnterDarkEnvironment": true,`   
`"watchShock": true`  
`}`  
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
### Get Alert Profiles example ###
**GET  /vf/rest/getAlertProfiles/${accessToken}**  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": [`  
`{`  
`"id": 2,`  
`"description": "Any description",`  
`"name": "AnyAlert",`  
`"criticalHighTemperatureForMoreThen": 0,`  
`"criticalHighTemperature": 5.0,`  
`"criticalLowTemperatureForMoreThen": 0,`  
`"criticalLowTemperature": -15.0,`  
`"highTemperature": 1.0,`  
`"highTemperatureForMoreThen": 55,`  
`"lowTemperature": -10.0,`  
`"lowTemperatureForMoreThen": 55,`  
`"watchBatteryLow": true,`  
`"watchEnterBrightEnvironment": true,`  
`"watchEnterDarkEnvironment": true,`  
`"watchShock": true`  
`}`  
`]`  
`}`  
### Save Notification Schedule example ###
**POST /vf/rest/saveNotificationSchedule/${accessToken}**  
**Request body:**  
`{`  
`"description": "JUnit schedule",`  
`"name": "Sched",`  
`"schedules": [`  
`{`  
`"company": "Sun",`  
`"emailNotification": "asuvorov@sun.com",`  
`"firstName": "Alexander",`  
`"lastName": "Suvorov",`  
`"position": "Generalisimus",`  
`"smsNotification": "1111111117",`  
`"toTime": 17,`  
`"fromTime": 1,`  
`"pushToMobileApp": true,`  
`"weekDays": [`  
`true,`  
`false,`  
`false,`  
`true,`  
`false,`  
`false,`  
`false`  
`]`  
`},`  
`{`  
`"company": "Sun",`  
`"emailNotification": "asuvorov@sun.com",`  
`"firstName": "Alexander",`  
`"lastName": "Suvorov",`  
`"position": "Generalisimus",`  
`"smsNotification": "1111111117",`  
`"toTime": 17,`  
`"fromTime": 1,`  
`"pushToMobileApp": true,`  
`"weekDays": [`  
`true,`  
`false,`  
`false,`  
`true,`  
`false,`  
`false,`  
`false`  
`]`  
`}`  
`]`  
`}`  
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
**GET /vf/rest/getNotificationSchedules/${accessToken}**  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": [`  
`{`  
`"description": "JUnit schedule",`  
`"name": "Sched",`  
`"id": 2,`  
`"schedules": [`  
`{`  
`"company": "Sun",`  
`"emailNotification": "asuvorov@sun.com",`  
`"firstName": "Alexander",`  
`"lastName": "Suvorov",`  
`"position": "Generalisimus",`  
`"smsNotification": "1111111117",`  
`"toTime": 17,`  
`"fromTime": 1,`  
`"id": 3,`  
`"pushToMobileApp": true,`  
`"weekDays": [`  
`true,`  
`false,`  
`false,`  
`true,`  
`false,`  
`false,`  
`false`  
`]`  
`},`  
`{`  
`"company": "Sun",`  
`"emailNotification": "asuvorov@sun.com",`  
`"firstName": "Alexander",`  
`"lastName": "Suvorov",`  
`"position": "Generalisimus",`  
`"smsNotification": "1111111117",`  
`"toTime": 17,`  
`"fromTime": 1,`  
`"id": 4,`  
`"pushToMobileApp": true,`  
`"weekDays": [`  
`true,`  
`false,`  
`false,`  
`true,`  
`false,`  
`false,`  
`false`  
`]`  
`}`  
`]`  
`}`  
`]`  
`}`  
### Save Location example ###
**POST /vf/rest/saveLocationProfile/${accessToken}**  
**Request body:**  
`{`  
`"company": "Sun Microsystems",`  
`"name": "Loc-1",`  
`"notes": "Any notes",`  
`"address": "Odessa, Deribasovskaya 1, apt.1",`  
`"location": {`  
`"lat": 100.5,`  
`"lon": 100.501`  
`},`  
`"radius": 1000`  
`}`  
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
### Get Location Profiles example ###
**GET /vf/rest/getLocationProfiles/${accessToken}**  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": [`  
`{`  
`"id": 2,`  
`"company": "Sun Microsystems",`  
`"name": "Loc-1",`  
`"notes": "Any notes",`  
`"address": "Odessa, Deribasovskaya 1, apt.1",`  
`"location": {`  
`"lat": 100.5,`  
`"lon": 100.501`  
`},`  
`"radius": 1000`  
`}`  
`]`  
`}`  
### Save Shipment Template example ###
**POST /vf/rest/saveShipmentTemplate/${accessToken}**  
**Request body:**  
```
{
  "name": "JUnit-tpl",
  "shipmentDescription": "Any Description",
  "alertSuppressionDuringCoolDown": 55,
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
```  
**Response:**  
```
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
**GET /vf/rest/getShipmentTemplates/${accessToken}**  
**Response:**  
```
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
### Save Device example ###
**POST /vf/rest/saveDevice/${accessToken}**  
**Request body:**  
`{`  
`"description": "Device description",`  
`"id": "1209898347987.123",`  
`"imei": "1209898347987",`  
`"name": "Device Name",`  
`"sn": "1"`  
`}`  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`}`  
`}`  
### Get Devices example ###
**GET  /vf/rest/getDevices/${accessToken}**  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": [`  
`{`  
`"description": "Device description",`  
`"id": "1209898347987.123",`  
`"imei": "1209898347987",`  
`"name": "Device Name",`  
`"sn": "1"`  
`}`  
`]`  
`}`  
### Save Shipment example ###
**POST  /vf/rest/saveShipment/${accessToken}**  
**Request body:**  
```
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
    "shipmentDate": "2015-10-09T11:47:21.945+0300",
    "customFields": "customFields",
    "status": "Default",
    "devices": [
      "234908720394857.123",
      "329847983724987.123"
    ]
  }
}
```
**Response:**  
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
### Get Shipments example ###
**GET /vf/rest/getShipments/${accessToken}**  
**Response:**  
```
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": [
    {
      "name": "Shipment-1",
      "shipmentDescription": "Any Description",
      "alertSuppressionDuringCoolDown": 55,
      "id": 11,
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
      "shipmentDate": "2015-10-09T11:54:22.953+0300",
      "customFields": "customFields",
      "status": "Default",
      "devices": [
        "234908720394857.123",
        "329847983724987.123"
      ]
    }
  ]
}
```
### Get Notifications example ###
**GET  /vf/rest/getNotifications/${accessToken}?shipment=11**  
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
`"date": "2015-10-12T23:57:45.105+0300",`  
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
`"date": "2015-10-12T23:57:45.105+0300",`  
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
`"date": "2015-10-12T23:57:45.104+0300",`  
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
### Get Shipment Data example ###
**GET /vf/rest/getShipmentData/${accessToken}?fromDate=2015-10-11T20%3A17%3A23.016%2B0300&onlyWithAlerts=false&toDate=2015-10-13T02%3A50%3A43.016%2B0300**  
**Response:**  
`{`  
`"status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": [`  
`{`  
`"shipment": 11,`  
`"data": [`  
`{`  
`"device": "234908720394857",`  
`"alerts": [`  
`{`  
`"description": "Alert description",`  
`"name": "Alert-BatteryLow",`  
`"id": 14,`  
`"date": "2015-10-11T20:17:23.016+0300",`  
`"device": "234908720394857",`  
`"type": "BatteryLow"`  
`}`  
`],`  
`"events": [`  
`{`  
`"battery": 1234,`  
`"id": 13,`  
`"temperature": 56.0,`  
`"time": "2015-10-13T00:04:03.015+0300",`  
`"type": "AUT"`  
`}`  
`]`  
`}`  
`]`  
`}`  
`]`  
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
### Get Alert Profile example ###
**GET /vf/rest/getAlertProfile/${accessToken}?id=77**  
**Response:**  
```
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "id": 77,
    "description": "Any description",
    "name": "AnyAlert",
    "criticalHighTemperatureForMoreThen": 0,
    "criticalHighTemperature": 5.0,
    "criticalLowTemperatureForMoreThen": 0,
    "criticalLowTemperature": -15.0,
    "highTemperature": 1.0,
    "highTemperatureForMoreThen": 55,
    "lowTemperature": -10.0,
    "lowTemperatureForMoreThen": 55,
    "watchBatteryLow": true,
    "watchEnterBrightEnvironment": true,
    "watchEnterDarkEnvironment": true,
    "watchShock": true
  }
}
```
### Get Location Profile example ###
**GET /vf/rest/getLocationProfile/${accessToken}?id=77**  
**Response:**  
```
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "id": 77,
    "companyDescription": "Sun Microsystems",
    "name": "Loc-1",
    "notes": "Any notes",
    "address": "Odessa, Deribasovskaya 1, apt.1",
    "location": {
      "lat": 100.5,
      "lon": 100.501
    },
    "radius": 1000
  }
}
```
### Get Shipment Template example ###
**GET /rest/getShipmentTemplate/${accessToken}?id=77**  
**Response:**  
```
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
**GET /vf/rest/getDevice/${accessToken}?id=923487509328.123**  
**Response:**  
```
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "description": "Device description",
    "id": "923487509328.123",
    "imei": "923487509328",
    "name": "Device Name",
    "sn": "1"
  }
}
```
### Get Shipment example ###
**GET /vf/rest/getShipment/${accessToken}?id=77**  
**Response:**  
```
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "name": "Shipment-1",
    "shipmentDescription": "Any Description",
    "alertSuppressionDuringCoolDown": 55,
    "id": 77,
    "alertProfile": 78,
    "alertsNotificationSchedules": [
      91
    ],
    "arrivalNotificationWithIn": 111,
    "arrivalNotificationSchedules": [
      92
    ],
    "excludeNotificationsIfNoAlertsFired": true,
    "shippedFrom": 79,
    "shippedTo": 80,
    "shutdownDevice": 155,
    "assetType": "SeaContainer",
    "palletId": "palettid",
    "assetNum": "10515",
    "shipmentDate": "2015-10-09T11:53:13.146+0300",
    "customFields": "customFields",
    "status": "Default",
    "devices": [
      "234908720394857.123",
      "329847983724987.123"
    ]
  }
}
```
### Get Notification Schedule example ###
**GET /vf/rest/getNotificationSchedule/${accessToken}?id=77**  
**Response:**  
```
{
  "status": {
    "code": 0,
    "message": "Success"
  },
  "response": {
    "description": "JUnit schedule",
    "name": "Sched",
    "id": 77,
    "schedules": [
      {
        "company": "Sun",
        "emailNotification": "asuvorov@sun.com",
        "firstName": "Alexander",
        "lastName": "Suvorov",
        "position": "Generalisimus",
        "smsNotification": "1111111117",
        "toTime": 17,
        "fromTime": 1,
        "pushToMobileApp": true,
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
        "company": "Sun",
        "emailNotification": "asuvorov@sun.com",
        "firstName": "Alexander",
        "lastName": "Suvorov",
        "position": "Generalisimus",
        "smsNotification": "1111111117",
        "toTime": 17,
        "fromTime": 1,
        "pushToMobileApp": true,
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
### Get Profile example ###
**GET /vf/rest/getProfile/${accessToken}**  
**Response:**  
```
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
```
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
