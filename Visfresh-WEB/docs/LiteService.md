# Smarttrace Lite Service

### Date format:###
The requests date parameters should have following format `yyyy-MM-dd'T'HH:mm` in current user's time zone if the user is logged in now and UTC time zone otherwise. Example:
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
### Lite methods.
# User should be authorized using REST service. See RestService.md document#

1. [Get Shipments](#markdown-header-get-shipments-example) 

## Special Request objects ##
1. [Get Shipments filter](#markdown-header-get-shipments-filter)

### Get Shipments ###
Method *POST*, method name getShipments, request body [Get Shipments filter](#markdown-header-get-shipments-filter)  
Returns array of [Shipment List items](#markdown-header-shipment-list-item) and total items count,
it is not same as [Shipment Object](#markdown-header-shipment).  
[(example)](#markdown-header-get-shipments-example)

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

## JSON Requests ##
### Get Shipments filter ###
```json
{
  "alertsOnly": true,
  "deviceImei": "283409237873234",
  "last2Days": true,
  "lastDay": true,
  "lastMonth": true,
  "lastWeek": true,
  "shipmentDateFrom": "2015-11-12T18:21",
  "shipmentDateTo": "2015-11-24T07:51",
  "shipmentDescription": "JUnit Shipment",
  "shippedFrom": [], //array of ID of shipped from locations
  "shippedTo": [], //array of ID of shipped to locations
  "status": "InProgress",
  "goods": "JUnit",
  "excludePriorShipments": true,
  "pageIndex": 10,
  "pageSize": 200,
  "sortOrder": "asc",
  "sortColumn": "anyColumn"
}
```

## Examples ##
### Get Shipments example ###
**POST /vf/lite/getShipments/64-6b0f1687e59aa11bb2bf6afc14a8aece**  
**Request:**  
```json
{
  "alertsOnly": false
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
      "deviceSN": "039485",
      "tripCount": 0,
      "shipmentId": 27,
      "shipmentDate": "2:42 17 Mar 2017",
      "shipmentDateISO": "2017-03-17 02:42",
      "shippedFrom": null,
      "shippedTo": null,
      "estArrivalDate": null,
      "estArrivalDateISO": null,
      "actualArrivalDate": null,
      "actualArrivalDateISO": null,
      "percentageComplete": 0,
      "lowerTemperatureLimit": 0.0,
      "upperTemperatureLimit": 5.0,
      "alertSummary": {},
      "siblingCount": 0,
      "keyLocations": [
        {
          "temperature": 2.0,
          "time": "13:42 28 Mar 2017",
          "timeISO": "2017-03-28 13:42"
        },
        {
          "temperature": 2.0,
          "time": "13:42 28 Mar 2017",
          "timeISO": "2017-03-28 13:42"
        },
        {
          "temperature": 6.0,
          "time": "13:42 28 Mar 2017",
          "timeISO": "2017-03-28 13:42"
        },
        {
          "temperature": 6.0,
          "time": "13:42 28 Mar 2017",
          "timeISO": "2017-03-28 13:42"
        },
        {
          "temperature": 5.0,
          "time": "13:42 28 Mar 2017",
          "timeISO": "2017-03-28 13:42"
        },
        {
          "temperature": 4.0,
          "time": "13:42 28 Mar 2017",
          "timeISO": "2017-03-28 13:42"
        },
        {
          "temperature": 2.0,
          "time": "13:42 28 Mar 2017",
          "timeISO": "2017-03-28 13:42"
        },
        {
          "temperature": 2.0,
          "time": "13:42 28 Mar 2017",
          "timeISO": "2017-03-28 13:42"
        },
        {
          "temperature": 2.0,
          "time": "13:42 28 Mar 2017",
          "timeISO": "2017-03-28 13:42"
        }
      ]
    },
    {
      "status": "InProgress",
      "deviceSN": "039485",
      "tripCount": 0,
      "shipmentId": 28,
      "shipmentDate": "2:42 17 Mar 2017",
      "shipmentDateISO": "2017-03-17 02:42",
      "shippedFrom": null,
      "shippedTo": null,
      "estArrivalDate": null,
      "estArrivalDateISO": null,
      "actualArrivalDate": null,
      "actualArrivalDateISO": null,
      "percentageComplete": 0,
      "lowerTemperatureLimit": 0.0,
      "upperTemperatureLimit": 5.0,
      "alertSummary": {},
      "siblingCount": 0,
      "keyLocations": []
    }
  ],
  "totalCount": 2
}
```
