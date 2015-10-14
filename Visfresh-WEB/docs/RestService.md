# Visfresh Rest Service

### Date format:
The date should have following format `yyyy-MM-dd'T'HH:mm:ss.SSSZ` with RFC 822 time zone as for requests as for responses. Example:
`2015-09-30T01:19:56.060+0300`
### Requests and responses:
The GET request parameters of URL link should be URL encoded to, but JSON body of request and response should be
sent as is without URL encoding.  
For all POST JSON requests the “Content-Type: application/json” HTTP header should be used.
### Server [Responses](#response-message):
Each server response has structure:  
`{`  
` "status": {`  
`"code": 0,`  
`"message": "Success"`  
`},`  
`"response": {`  
`"token": "token_100001",`  
`"expired": "2015-09-30T01:19:56.060+0300"`  
`}`   
`}`  
  The `response` element can be absent if there is not any response, but status code should be always in answer.  
0 - status code is a “Success” code  
other code - is an error code. The list of possible error codes will determined in next releases 
In case of error the “response” element of JSON response is absent.  
### Security methods.
An authentication can be performed as from REST client using login method, as from GTSE page (Will implemented in future). If the GTSE authentication has done, then access token can be get by getToken method in some HTTP session. In this case the REST client will attached to REST service using existing GTSE session.
## Rest Service methods.
1. [Authentication](#authentication).  
2. [Get access token using existing GTS(e) session.](#get-access-token-using-existing-gts-e-session)  
3. [Get User Info](#get-user-info)  
4. [Logout](#logout)  
5. [Refresh access token](#refresh-access-token)  
6. [Save alert profile](#save-alert-profile)  
7. [Get Alert Profiles](#get-alert-profiles)  
8. [Save notification schedule](#save-notification-schedule)  
9. [Get notification schedules](#get-notification-schedules)  
10. [Save Location](#save-location)  
11. [Get Locations](#get-locations)  
12. [Save shipment template](#save-shipment-template)  
13. [Get Shipment templates](#get-shipment-templates)  
14. [Save Device](#save-device)  
15. [Get Devices](#get-devices)  
16. [Save Shipment](#save-shipment)  
17. [Get Shipments](#get-shipments)  
18. [Get Notifications](#get-notifications)  
19. [Mark Notification as read](#mark-notification-as-read)  
20. [Get Shipment Data](#get-shipment-data)  
21. [Send Command to Device](#send-command-to-device)  

### Authentication.###
Method *POST*, method name *login*, method parameters  
1. login - user name of logged in user  
2. password - password  

are contained in [authentication request body](#authentication-request-body). Returns [Authentication token response](#authentication-token-response).  
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
3. roles - array of user roles  

[(example)](#get-user-info-example)

### Logout ###
Method *GET*, method name *logout*, have not parameters. Closes user REST session and clears all associated resources  
[(example)](#logout-example)

### Refresh access token ###
Method *GET*, method name *refreshToken*, have not parameters. Refresh the access token for current REST session.  
[(example)](#refresh-access-token)

### Save alert profile ###
Method *POST*, method name *saveAlertProfile*, request body contains JSON serialized [Alert Profile object](#alert-profile). Response contains ID of just saved alert profile.  
[(example)](#save-alert-profile-example)

### Get Alert Profiles ###
Method *GET*, method name *getAlertProfiles*, have not parameters. Returns an array of [Alert Profile objects](#alert-profile).  
[(example)](#get-alert-profiles-example)

### Save notification schedule ###
Method *POST*, method name *saveNotificationSchedule*, request body contains JSON serialized [Notification Schedule object](#notification-schedule). Response contains ID of just saved notification schedule.  
[(example)](#save-notification-schedule-example)]

### Get notification schedules ###
Method *GET*, method name *getNotificationSchedules*, have not parameters. Return array of [Notification Schedule objects](#notification-schedule)  
[(example)](#get-notification-schedule-example)

### Save Location ###
Method *POST*, method name *saveLocationProfile*, request body contains JSON serialized [Location Profile Object](#location-profile). Response contains ID of just saved location profile  
[(example)](#save-location-example)

### Get Locations ###
Method *GET*, method name *getLocationProfiles*, have not parameters. Returns array of [Location Profile Objects](#location-profile)  
[(example)](#get-location-profiles-example)

### Save shipment template ###

### Get Shipment templates ###

### Save Device ###

### Get Devices ###

### Save Shipment ###

### Get Shipments ###

### Get Notifications ###

### Mark Notification as read ###

### Get Shipment Data ###

### Send Command to Device ###

## Objects
### Response message ###
`{` 
` "status": ` [ResponseStatus](#response-status)`,`  
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
### Notification Schedule ##
`{`  
`"id:" 77`
`"description": "JUnit schedule",`  
`"name": "Sched",`  
`"schedules": [`  
[PersonalScheduleObject](#personal-schedule),  
[PersonalScheduleObject](#personal-schedule)  
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

## Examples ##
### Authentication request example ###
**POST /vf/rest/login**   
`{`  
`"login": "asuvorov",`  
`"password": "password"`  
`}`  
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
`"admin",`  
`"user"`  
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
### Get Notification Schedule example ###
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

