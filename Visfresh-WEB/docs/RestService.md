# Visfresh Rest Service

### Date format:
The date should have following format `yyyy-MM-dd'T'HH:mm:ss.SSSZ` with RFC 822 time zone as for requests as for responses. Example:
`2015-09-30T01:19:56.060+0300`
### Requests and responses:
The GET request parameters of URL link should be URL encoded to, but JSON body of request and response should be
sent as is without URL encoding.  
For all POST JSON requests the “Content-Type: application/json” HTTP header should be used.
### Server [Responses](#markdown-header-response-message):
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
### Authentication.
Method *POST*, method name *'login'*, method parameters  
*   login - user name of logged in user  
*   password - password  

are contained in [authentication request body](#markdown-header-authentication-request-body). Returns [Authentication token response](#markdown-header-authentication-token-response). [(example)](#markdown-header-authentication-request-example)  
### Get access token using existing GTS(e) session.###
The user should be logged in to GTS(e). (not implemented now).
Method *POST*, method name *getToken*, no parameters. In case of this request the service access a current user session, determines user info, log in as REST service user and returns authentication session. [(example)](#markdown-header-attach-to-existing-session-example)

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

