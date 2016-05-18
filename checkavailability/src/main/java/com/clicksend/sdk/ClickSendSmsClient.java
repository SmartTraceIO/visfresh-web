package com.clicksend.sdk;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.clicksend.sdk.http.HttpClientUtils;
import com.clicksend.sdk.request.Balance;
import com.clicksend.sdk.request.Delivery;
import com.clicksend.sdk.request.Message;
import com.clicksend.sdk.response.BalanceResult;
import com.clicksend.sdk.response.DeliveryResult;
import com.clicksend.sdk.response.DeliveryResultItem;
import com.clicksend.sdk.response.ReplyResult;
import com.clicksend.sdk.response.ReplyResultItem;
import com.clicksend.sdk.response.SmsResult;
import com.clicksend.sdk.util.Definitions;

/**
 * ClickSendSmsClient.java<br><br>
 *
 * Client for talking to the ClickSend REST interface<br><br>
 *
 * Usage
 *
 * To submit a message, first you should initialize a ClickSendSmsClient, passing the credentials for your ClickSend account on the constructor.
 * Then, you should initialize a TextMessage {@link com.clicksend.sdk.request.TextMessage} subclass.<br>
 *
 * Once you have a {@link com.clicksend.sdk.request.Message} object, you simply pass this to the {@link #sendSms(Message)} method in the ClickSendSmsClient instance.
 * This will construct and post the request to the ClickSend REST service.<br>
 * This method will return an array of {@link com.clicksend.sdk.response.SmsResult}, with 1 entry for every sms message that was sent.<br>
 * Each entry in this array will contain an individual messageId as well as an individual status detailing the success or reason for failure of each message.<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class ClickSendSmsClient {

    private static final Log log = LogFactory.getLog(ClickSendSmsClient.class);

    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;

    private String baseUrlHttp;
    private String baseUrlHttps;
    private final String authHeader;

    private final int connectionTimeout;
    private final int socketTimeout;
    private final boolean useSsl;
    private HttpClient httpClient = null;

    /**
     * Initialize a new ClickSendSmsClient instance that will communicate using the supplied credentials.
     *
     * @param apiUsername Your ClickSend account username
     * @param apiPassword Your ClickSend account api password
     */
    public ClickSendSmsClient(final String apiUsername,final String apiPassword) throws Exception {
        this(apiUsername,apiPassword,true,Definitions.DEFAULT_CONNECTION_TIMEOUT,Definitions.DEFAULT_SOCKET_TIMEOUT); 
    }

    /**
     * Initialize a new ClickSendSmsClient instance that will communicate using the supplied credentials, and will use the supplied connection and read timeout values.
     *
     * @param apiUsername Your ClickSend account username
     * @param apiPassword Your ClickSend account api password
     * @param connectionTimeout over-ride the default connection timeout with this value (in milliseconds)
     * @param socketTimeout over-ride the default read-timeout with this value (in milliseconds)
     */
    public ClickSendSmsClient(final String apiUsername,final String apiPassword,final int connectionTimeout,final int socketTimeout) throws Exception {
        this(apiUsername,apiPassword,true,connectionTimeout,socketTimeout);
    }

    /**
     * Initialize  a new ClickSendSmsClient instance that will communicate using the supplied credentials, and will use the supplied connection and read timeout values.<br>
     *
     * @param apiUsername Your ClickSend account username
     * @param apiPassword Your ClickSend account api password
     * @param connectionTimeout over-ride the default connection timeout with this value (in milliseconds)
     * @param socketTimeout over-ride the default read-timeout with this value (in milliseconds)
     * @param useSsl do we use a SSL / HTTPS connection for submitting requests
     */
    public ClickSendSmsClient(final String apiUsername,final String apiPassword,boolean useSsl,final int connectionTimeout,final int socketTimeout) throws Exception {
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
        this.useSsl = useSsl;
        try {
            this.documentBuilderFactory = DocumentBuilderFactory.newInstance();
            this.documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            throw new Exception("ERROR initializing XML Document builder!", e);
        }
        
        this.authHeader = DatatypeConverter.printBase64Binary((apiUsername+":"+apiPassword).getBytes("UTF-8"));
        
    }

    /**
     * submit a message submission request object.
     * This will use the supplied object to construct a request and post it to the ClickSend REST interface.<br>
     * This method will respond with an array of SmsResult objects. 
     * The array of SmsResult objects will contain a SmsResult object for every actual sms that was required to submit the message.
     * each message can potentially have a different status result, and each message will have a different message id.
     * Delivery notifications will be generated for each sms message within this set and will be posted to your application containing the appropriate message id.
     *
     * @param message The message request object that describes the type of message and the contents to be submitted.
     *
     * @return SmsResult[] an array of results, 1 object for each sms message that was required to submit this message in its entirety
     *
     * @throws Exception There has been a general failure either within the Client class, or while attempting to communicate with the ClickSend service (eg, Network failure)
     */
    public SmsResult[] sendSms(Message message) throws Exception {
        this.baseUrlHttps = Definitions.DEFAULT_SEND_SMS_BASE_URL_SECURE;
        this.baseUrlHttp = Definitions.DEFAULT_SEND_SMS_BASE_URL_NONSECURE;
        log.debug("HTTP-SMS-Submission Client .. from [ " + message.getSenderId() + " ] to [ " + message.getTo() + " ] msg [ " + message.getMessage() + " ] ");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("method", Definitions.API_TYPE));
        params.add(new BasicNameValuePair("to", message.getTo()));
        params.add(new BasicNameValuePair("message", message.getMessage()));
//        params.add(new BasicNameValuePair("messagetype", "Unicode"));
        
        
        if (message.getSenderId() != null)
        	 params.add(new BasicNameValuePair("senderid", message.getSenderId()));
        
        if (message.getSchedule() != null)
            params.add(new BasicNameValuePair("schedule", message.getSchedule()));
        
        if (message.getCustomString() != null)
            params.add(new BasicNameValuePair("customstring", message.getCustomString()));   
        
        if (message.getReturn() != null)
            params.add(new BasicNameValuePair("return", message.getReturn())); 
  
        String baseUrl = useSsl ? this.baseUrlHttps : this.baseUrlHttp;

        String response = null;
        for (int pass=1;pass<=2;pass++) {
            HttpUriRequest method = null;
            HttpPost httpPost = new HttpPost(baseUrl);
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            httpPost.setHeader("Authorization", "Basic " + this.authHeader);
            method = httpPost;
            String url = baseUrl + "?" + URLEncodedUtils.format(params, "utf-8");
            try {
                if (this.httpClient == null){
                    this.httpClient = HttpClientUtils.getInstance(this.connectionTimeout, this.socketTimeout).getNewHttpClient();
                }
                HttpResponse httpResponse = this.httpClient.execute(method);
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status != 200){
                    throw new Exception("got a non-200 response [ " + status + " ] from ClickSend-HTTP for url [ " + url + " ] ");
                }
                response = new BasicResponseHandler().handleResponse(httpResponse);
                log.info("SMS SEND CLICKSEND-HTTP URL [ " + url + " ] -- response [ " + response + " ] ");
                break;
            } catch (Exception e) {
                method.abort();
                log.info("communication failure: " + e);
                String exceptionMsg = e.getMessage();
                if (exceptionMsg.indexOf("Read timed out") >= 0) {
                    log.info("we're still connected, but the target did not respond in a timely manner ..  drop ...");
                } else {
                    if (pass == 1) {
                        log.info("... re-establish http client ...");
                        this.httpClient = null;
                        continue;
                    }
                }
                SmsResult[] results = new SmsResult[1];
                results[0] = new SmsResult("LOCAL500",null,null,"Failed to communicate with CLICKSEND-HTTP url [ " + url + " ] ..." + e);
                return results;
            }
        }
        /*
            We receive a response from the api that looks like this, parse the document
            and turn it into an array of SmsResult, one object per <message> node
				<?xml version='1.0' encoding='UTF-8' ?>
				<xml>
					<messages recipientcount='2'>
						<message>
							<to>+61411111111</to>
							<messageid>D9F15F83-34EC-6A31-A57E-7E8FB0966D78</messageid>
							<result>0000</result>
							<errortext>Success</errortext>
						</message>
						<message>
							<to>+61422222222</to>
							<messageid>F15F83H8-15AC-3R31-777E-7E8FB09SSDP2</messageid>
							<result>0000</result>
							<errortext>Success</errortext>
						</message>	
					</messages>
				</xml>
        */

        List<SmsResult> results = new ArrayList<SmsResult>();
        Document doc = null;
        synchronized(this.documentBuilder) {
            try {
                doc = this.documentBuilder.parse(new InputSource(new StringReader(response)));
            } catch (Exception e) {
                throw new Exception("Failed to build a DOM doc for the xml document [ " + response + " ] ", e);
            }
        }

        NodeList replies = doc.getElementsByTagName("xml");
        for (int i=0;i<replies.getLength();i++) {
            Node reply = replies.item(i);
            NodeList messageLists = reply.getChildNodes();
            for (int i2=0;i2<messageLists.getLength();i2++) {
                Node messagesNode = messageLists.item(i2);
                if (messagesNode.getNodeType() != Node.ELEMENT_NODE) continue;
                if (messagesNode.getNodeName().equals("messages")) {
                    NodeList messages = messagesNode.getChildNodes();
                    for (int i3=0;i3<messages.getLength();i3++) {
                        Node messageNode = messages.item(i3);
                        if (messageNode.getNodeType() != Node.ELEMENT_NODE) continue;

                        String result = "";
                        String messageId = null;
                        String to = null;
                        String errorText = null;

                        NodeList nodes = messageNode.getChildNodes();
                        for (int i4=0;i4<nodes.getLength();i4++) {
                            Node node = nodes.item(i4);
                            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                            
                            if (node.getNodeName().equals("messageid")) {
                                messageId = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("to")) {
                            	to = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("result")) {
                            	result = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("errortext")) {
                                errorText = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else {
                                log.error("xml parser .. unknown node found in status-return, expected [ messageId, to, status, errorText, clientRef, messagePrice, remainingBalance, reachability ] -- found [ " + node.getNodeName() + " ] ");
                            }
                        }

                        if (result.equals("")){
                            throw new Exception("Xml Parser - did not find a <result> node");
                        }

                        results.add(new SmsResult(result,to,messageId,errorText));
                    }
                }
            }
        }
        return results.toArray(new SmsResult[0]);
    }

    /**
     * submit a get balance request object.
     * This will use the supplied object to construct a request and post it to the ClickSend REST interface.<br>
     * This method will respond with BalanceResult object.
     *
     * @param balance The Balance request object that describes the getBalance request.
     *
     * @return BalanceResult
     *
     * @throws Exception There has been a general failure either within the Client class, or whilst attempting to communicate with the ClickSend service (eg, Network failure)
     */
    public BalanceResult getBalance(Balance balance) throws Exception {
    	
        this.baseUrlHttps = Definitions.DEFAULT_GET_BALANCE_BASE_URL_SECURE;
        this.baseUrlHttp = Definitions.DEFAULT_GET_BALANCE_BASE_URL_NONSECURE;

        log.debug("HTTP-Get-Balance Client .. from [ " + balance.getCountryCode() + " ] ");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        boolean doPost = false;
        if (balance.getCountryCode() != null)
        	 params.add(new BasicNameValuePair("country", balance.getCountryCode()));
        
        doPost = true;

        String baseUrl = useSsl ? this.baseUrlHttps : this.baseUrlHttp;

        String response = null;
        for (int pass=1;pass<=2;pass++) {
            HttpUriRequest method = null;
            doPost = true;
            String url = null;
            if (doPost) {
                HttpPost httpPost = new HttpPost(baseUrl);
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                httpPost.setHeader("Authorization", "Basic " + this.authHeader);
                method = httpPost;
                url = baseUrl + "?" + URLEncodedUtils.format(params, "utf-8");
            } else {
                String query = URLEncodedUtils.format(params, "utf-8");
                method = new HttpGet(baseUrl + "?" + query);
                method.setHeader("Authorization", "Basic " + this.authHeader);
                url = method.getRequestLine().getUri();
            }
            
            try {
                if (this.httpClient == null)
                    this.httpClient = HttpClientUtils.getInstance(this.connectionTimeout, this.socketTimeout).getNewHttpClient();
                
                HttpResponse httpResponse = this.httpClient.execute(method);
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status != 200){
                    throw new Exception("got a non-200 response [ " + status + " ] from ClickSend-HTTP for url [ " + url + " ] ");
                }
                response = new BasicResponseHandler().handleResponse(httpResponse);
                log.info("GET BALANCE CLICKSEND-HTTP URL [ " + url + " ] -- response [ " + response + " ] ");
                break;
            } catch (Exception e) {
                method.abort();
                log.info("communication failure: " + e);
                String exceptionMsg = e.getMessage();
                if (exceptionMsg.indexOf("Read timed out") >= 0) {
                    log.info("we're still connected, but the target did not respond in a timely manner ..  drop ...");
                } else {
                    if (pass == 1) {
                        log.info("... re-establish http client ...");
                        this.httpClient = null;
                        continue;
                    }
                }
                BalanceResult bResult = new BalanceResult("LOCAL500","Failed to communicate with CLICKSEND-HTTP url [ " + url + " ] ..." + e,null,null,null,null);
                return bResult;
            }
        }

        /*
			<?xml version='1.0' encoding='UTF-8' ?>
			<xml>
				<result>0000</result>
				<errortext>Success</errortext>
				<balance>125.00</balance>
				<credit>1785</credit>	
				<type>Prepay</type>	
				<currency_symbol>$</currency_symbol>		
			</xml>
        */
        Document doc = null;
        synchronized(this.documentBuilder) {
            try {
                doc = this.documentBuilder.parse(new InputSource(new StringReader(response)));
            } catch (Exception e) {
                throw new Exception("Failed to build a DOM doc for the xml document [ " + response + " ] ", e);
            }
        }

        BalanceResult bResult = new BalanceResult(null,null,null,null,null,null);
       
        NodeList replies = doc.getElementsByTagName("xml");
        for (int i=0;i<replies.getLength();i++) {
            Node reply = replies.item(i);
            NodeList messageLists = reply.getChildNodes();
            
            String errorText = null;
            String result = "";
            String rBalance = null;
            String credit = null;
            String type = null;
            String currencySymbol = null;
            
            for (int i2=0;i2<messageLists.getLength();i2++) {
                Node messagesNode = messageLists.item(i2);
                if (messagesNode.getNodeType() != Node.ELEMENT_NODE) continue;

                    if (messagesNode.getNodeName().equals("balance")) {
                    	rBalance = messagesNode.getFirstChild() == null ? null : messagesNode.getFirstChild().getNodeValue();
                    } else if (messagesNode.getNodeName().equals("credit")) {
                    	credit = messagesNode.getFirstChild() == null ? null : messagesNode.getFirstChild().getNodeValue();
                    } else if (messagesNode.getNodeName().equals("type")) {
                    	type = messagesNode.getFirstChild() == null ? null : messagesNode.getFirstChild().getNodeValue();
                    } else if (messagesNode.getNodeName().equals("currency_symbol")) {
                    	currencySymbol = messagesNode.getFirstChild() == null ? null : messagesNode.getFirstChild().getNodeValue();
                    } else if (messagesNode.getNodeName().equals("result")) {
                    	result = messagesNode.getFirstChild() == null ? null : messagesNode.getFirstChild().getNodeValue();                    	
                    } else if (messagesNode.getNodeName().equals("errortext")) {
                        errorText = messagesNode.getFirstChild() == null ? null : messagesNode.getFirstChild().getNodeValue();
                    } else {
                        log.error("xml parser .. unknown node found in status-return, expected [ messageId, to, status, errorText, clientRef, messagePrice, remainingBalance, reachability ] -- found [ " + messagesNode.getNodeName() + " ] ");
                    }
            }
            
            if (result.equals("")){
                throw new Exception("Xml Parser - did not find a <result> node");
            }
            bResult= new BalanceResult(result,errorText,rBalance,credit,type,currencySymbol);
        }
        return bResult;
    }
    
    
    /**
     * submit a message submission request object.
     * This will use the supplied object to construct a request and post it to the ClickSend REST interface.<br>
     * This method will respond with an array of SmsResult objects. 
     * The array of SmsResult objects will contain a SmsResult object for every actual sms that was required to submit the message.
     * each message can potentially have a different status result, and each message will have a different message id.
     * Delivery notifications will be generated for each sms message within this set and will be posted to your application containing the appropriate message id.
     *
     * @param message The message request object that describes the type of message and the contents to be submitted.
     *
     * @return SmsResult[] an array of results, 1 object for each sms message that was required to submit this message in its entirety
     *
     * @throws Exception There has been a general failure either within the Client class, or while attempting to communicate with the ClickSend service (eg, Network failure)
     */
    public DeliveryResult getDlr(Delivery delivery) throws Exception {
        this.baseUrlHttps = Definitions.DEFAULT_GET_DLR_BASE_URL_SECURE;
        this.baseUrlHttp = Definitions.DEFAULT_GET_DLR_BASE_URL_NONSECURE;
        log.debug("HTTP-SMS-Submission Client .. MSGID [ " + delivery.getMessageId() + " ]");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (delivery.getMessageId() != null)
        	 params.add(new BasicNameValuePair("messageid", delivery.getMessageId()));
        
        String baseUrl = useSsl ? this.baseUrlHttps : this.baseUrlHttp;

        String response = null;
        for (int pass=1;pass<=2;pass++) {
            HttpUriRequest method = null;
            HttpPost httpPost = new HttpPost(baseUrl);
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            httpPost.setHeader("Authorization", "Basic " + this.authHeader);
            method = httpPost;
            String url = baseUrl + "?" + URLEncodedUtils.format(params, "utf-8");
            try {
                if (this.httpClient == null){
                    this.httpClient = HttpClientUtils.getInstance(this.connectionTimeout, this.socketTimeout).getNewHttpClient();
                }
                HttpResponse httpResponse = this.httpClient.execute(method);
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status != 200){
                    throw new Exception("got a non-200 response [ " + status + " ] from ClickSend-HTTP for url [ " + url + " ] ");
                }
                response = new BasicResponseHandler().handleResponse(httpResponse);
                log.info("GET DLR CLICKSEND-HTTP URL [ " + url + " ] -- response [ " + response + " ] ");
                break;
            } catch (Exception e) {
                method.abort();
                log.info("communication failure: " + e);
                String exceptionMsg = e.getMessage();
                if (exceptionMsg.indexOf("Read timed out") >= 0) {
                    log.info("we're still connected, but the target did not respond in a timely manner ..  drop ...");
                } else {
                    if (pass == 1) {
                        log.info("... re-establish http client ...");
                        this.httpClient = null;
                        continue;
                    }
                }
                DeliveryResult results = new DeliveryResult("LOCAL500",null,null);
                return results;
            }
        }
        /*
			<?xml version='1.0' encoding='UTF-8' ?>
			<xml>
				<dlrs dlrcount='2'>
					<dlr>
						<messageid>D9F15F83-34EC-6A31-A57E-7E8FB0966D78</messageid>
						<status>Delivered</status>
						<customstring></customstring>
					</dlr>
					<dlr>
						<messageid>F15F83H8-15AC-3R31-777E-7E8FB09SSDP2</messageid>
						<status>Undelivered</status>
						<customstring></customstring>
					</dlr>		
				</dlrs>
				<result>0000</result>
				<errortext>Success</errortext>
			</xml>

        */
        List<DeliveryResultItem> results = new ArrayList<DeliveryResultItem>();
        Document doc = null;
        synchronized(this.documentBuilder) {
            try {
                doc = this.documentBuilder.parse(new InputSource(new StringReader(response)));
            } catch (Exception e) {
                throw new Exception("Failed to build a DOM doc for the xml document [ " + response + " ] ", e);
            }
        }
        
        String result = "";
        String errorText = "";
        NodeList replies = doc.getElementsByTagName("xml");
        for (int i=0;i<replies.getLength();i++) {
            Node reply = replies.item(i);
            NodeList dlrLists = reply.getChildNodes();
            for (int i2=0;i2<dlrLists.getLength();i2++) {
                Node dlrsNode = dlrLists.item(i2);
                if (dlrsNode.getNodeType() != Node.ELEMENT_NODE) continue;
                if (dlrsNode.getNodeName().equals("dlrs")) {
                    NodeList messages = dlrsNode.getChildNodes();
                    for (int i3=0;i3<messages.getLength();i3++) {
                        Node dlrNode = messages.item(i3);
                        if (dlrNode.getNodeType() != Node.ELEMENT_NODE) continue;

                        String status = "";
                        String messageId = null;
                        String customString = null;
                        String username = null;

                        NodeList nodes = dlrNode.getChildNodes();
                        for (int i4=0;i4<nodes.getLength();i4++) {
                            Node node = nodes.item(i4);
                            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                            	
                            if(node.getNodeName().equals("username")){
                            	username = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            }else if (node.getNodeName().equals("messageid")) {
                                messageId = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("customstring")) {
                            	customString = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("status")) {
                            	status = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else {
                                log.error("xml parser .. unknown node found in status-return, expected [ messageid, status, customstring ] -- found [ " + node.getNodeName() + " ] ");
                            }
                        }

                        if (status.equals("")){
                            throw new Exception("Xml Parser - did not find a <status> node");
                        }
                        results.add(new DeliveryResultItem(status,customString,messageId,username));
                    }
                }else if(dlrsNode.getNodeName().equals("result")){
                	result = dlrsNode.getFirstChild() == null ? null : dlrsNode.getFirstChild().getNodeValue();
                }else if(dlrsNode.getNodeName().equals("errortext")){
                	errorText = dlrsNode.getFirstChild() == null ? null : dlrsNode.getFirstChild().getNodeValue();
                }
            }
        }
        return new DeliveryResult(result,errorText,results);
    }
    
    
    
    /**
     * submit a message submission request object.
     * This will use the supplied object to construct a request and post it to the ClickSend REST interface.<br>
     * This method will respond with an array of SmsResult objects. 
     * The array of SmsResult objects will contain a SmsResult object for every actual sms that was required to submit the message.
     * each message can potentially have a different status result, and each message will have a different message id.
     * Delivery notifications will be generated for each sms message within this set and will be posted to your application containing the appropriate message id.
     *
     * @param message The message request object that describes the type of message and the contents to be submitted.
     *
     * @return SmsResult[] an array of results, 1 object for each sms message that was required to submit this message in its entirety
     *
     * @throws Exception There has been a general failure either within the Client class, or while attempting to communicate with the ClickSend service (eg, Network failure)
     */
    public ReplyResult getReplies() throws Exception {
        this.baseUrlHttps = Definitions.DEFAULT_GET_REPLY_BASE_URL_SECURE;
        this.baseUrlHttp = Definitions.DEFAULT_GET_REPLY_BASE_URL_NONSECURE;
        log.debug("HTTP-SMS-Submission Client .. ]");
        
        String baseUrl = useSsl ? this.baseUrlHttps : this.baseUrlHttp;

        String response = null;
        for (int pass=1;pass<=2;pass++) {
            HttpUriRequest method = null;
            HttpPost httpPost = new HttpPost(baseUrl);
            httpPost.setHeader("Authorization", "Basic " + this.authHeader);
            method = httpPost;
            String url = baseUrl;
            try {
                if (this.httpClient == null){
                    this.httpClient = HttpClientUtils.getInstance(this.connectionTimeout, this.socketTimeout).getNewHttpClient();
                }
                HttpResponse httpResponse = this.httpClient.execute(method);
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status != 200){
                    throw new Exception("got a non-200 response [ " + status + " ] from ClickSend-HTTP for url [ " + url + " ] ");
                }
                response = new BasicResponseHandler().handleResponse(httpResponse);
                log.info("SMS SEND CLICKSEND-HTTP URL [ " + url + " ] -- response [ " + response + " ] ");
                break;
            } catch (Exception e) {
                method.abort();
                log.info("communication failure: " + e);
                String exceptionMsg = e.getMessage();
                if (exceptionMsg.indexOf("Read timed out") >= 0) {
                    log.info("we're still connected, but the target did not respond in a timely manner ..  drop ...");
                } else {
                    if (pass == 1) {
                        log.info("... re-establish http client ...");
                        this.httpClient = null;
                        continue;
                    }
                }
                ReplyResult results = new ReplyResult("LOCAL500",null,null);
                return results;
            }
        }
        /*
			<?xml version='1.0' encoding='UTF-8' ?>
			<xml>
				<replies replycount='2'>
					<reply>
						<from>+61411111111</from>
						<message>This is a reply</message>
						<originalmessage>Hello. Please reply</originalmessage>
						<originalmessageid>D9F15F83-34EC-6A31-A57E-7E8FB0966D78</originalmessageid>
						<originalsenderid>+61400000000</originalsenderid>
						<customstring></customstring>
					</reply>
					<reply>
						<from>+61422222222</from>
						<message>This is another reply</message>
						<originalmessage>Hello. Please reply</originalmessage>
						<originalmessageid>F15F83H8-15AC-3R31-777E-7E8FB09SSDP2</originalmessageid>
						<originalsenderid>+61400000000</originalsenderid>
						<customstring></customstring>
					</reply>
				</replies>
				<result>0000</result>
				<errortext>Success</errortext>
			</xml>
        */
        List<ReplyResultItem> results = new ArrayList<ReplyResultItem>();
        Document doc = null;
        synchronized(this.documentBuilder) {
            try {
                doc = this.documentBuilder.parse(new InputSource(new StringReader(response)));
            } catch (Exception e) {
                throw new Exception("Failed to build a DOM doc for the xml document [ " + response + " ] ", e);
            }
        }
        
        String result = "";
        String errorText = "";
        NodeList replies = doc.getElementsByTagName("xml");
        for (int i=0;i<replies.getLength();i++) {
            Node reply = replies.item(i);
            NodeList dlrLists = reply.getChildNodes();
            for (int i2=0;i2<dlrLists.getLength();i2++) {
                Node dlrsNode = dlrLists.item(i2);
                if (dlrsNode.getNodeType() != Node.ELEMENT_NODE) continue;
                if (dlrsNode.getNodeName().equals("replies")) {
                    NodeList messages = dlrsNode.getChildNodes();
                    for (int i3=0;i3<messages.getLength();i3++) {
                        Node dlrNode = messages.item(i3);
                        if (dlrNode.getNodeType() != Node.ELEMENT_NODE) continue;

                        
                        String from = "";
                        String customString = null;
                        String originalMessageId = null;
                        
                        
                        String message = null;
                        String originalMessage = null;
                        String originalSenderId = null;
                        

                        NodeList nodes = dlrNode.getChildNodes();
                        for (int i4=0;i4<nodes.getLength();i4++) {
                            Node node = nodes.item(i4);
                            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
                            
                            if (node.getNodeName().equals("from")) {
                            	from = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("customstring")) {
                            	customString = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("originalmessageid")) {
                            	originalMessageId = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("message")) {
                            	message = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("originalmessage")) {
                            	originalMessage = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else if (node.getNodeName().equals("originalsenderid")) {
                            	originalSenderId = node.getFirstChild() == null ? null : node.getFirstChild().getNodeValue();
                            } else {
                                log.error("xml parser .. unknown node found in status-return, expected [ from, message, originalmessage, originalmessageid, originalsenderid, customstring ] -- found [ " + node.getNodeName() + " ] ");
                            }
                        }

                        if (from.equals("")){
                            throw new Exception("Xml Parser - did not find a <from> node");
                        }

                        results.add(new ReplyResultItem(from,originalSenderId,message,originalMessage,originalMessageId,customString));
                    }
                }else if(dlrsNode.getNodeName().equals("result")){
                	result = dlrsNode.getFirstChild() == null ? null : dlrsNode.getFirstChild().getNodeValue();
                }else if(dlrsNode.getNodeName().equals("errortext")){
                	errorText = dlrsNode.getFirstChild() == null ? null : dlrsNode.getFirstChild().getNodeValue();
                }
            }
        }
        return new ReplyResult(result,errorText,results);
    }
       
}
