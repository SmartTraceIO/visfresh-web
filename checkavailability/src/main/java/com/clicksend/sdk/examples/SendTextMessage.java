package com.clicksend.sdk.examples;

import com.clicksend.sdk.ClickSendSmsClient;
import com.clicksend.sdk.request.TextMessage;
import com.clicksend.sdk.response.SmsResult;
import com.clicksend.sdk.util.Definitions;

/**
 * SendTextMessage.java<br><br>
 *
 * An example of using the Clicksend sms api to submit a SMS ...<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class SendTextMessage {

    public static final String USERNAME = "ClickSendUsername";
    public static final String API_KEY = "ClickSendApiKey";

    public static final String SMS_FROM = "SMSFrom";
    public static final String SMS_TO = "+61411111111,+8611111111111";
    public static final String SMS_TEXT = "Hello World!";

    public static void main(String[] args) {

        // Create a client for submitting to ClickSend
        ClickSendSmsClient client = null;
        
        try {
            client = new ClickSendSmsClient(USERNAME, API_KEY);
        } catch (Exception e) {
            System.err.println("Failed to create a new ClickSend Java Client");
            e.printStackTrace();
            throw new RuntimeException("Failed to create a new ClickSend Java Client");
        }

        // Create a Text SMS Message request object ...
        TextMessage message = new TextMessage(SMS_TO, SMS_TEXT);

        // Use the ClickSend client to submit the Text Message ...
        SmsResult[] results = null;
        
        try {
            results = client.sendSms(message);
        } catch (Exception e) {
            System.err.println("Failed to communicate with the ClickSend Server");
            e.printStackTrace();
            throw new RuntimeException("Failed to communicate with the ClickSend Server");
        }

        // Evaluate the results of the sms send attempt ...
        System.out.println("Total [ " + results.length + " ] SMS Sent");
        for (int i=0;i<results.length;i++) {
            System.out.println("--------- #SMS [ " + (i + 1) + " ] ------------");
            System.out.println(" [ " + results[i] + " ] ...");
            
            System.out.println("Status [ " + results[i].getStatusCode() + " ] ...");
            if (results[i].getStatusCode().equals(Definitions.STATUS_OK)){
                System.out.println("SUCCESS");
            }else{
                System.out.println("SMS FAILED!");
            }
            System.out.println("Message-Id [ " + results[i].getMessageId() + " ] ...");
            System.out.println("Error-Text [ " + results[i].getErrorText() + " ] ...");
            
        }
    }

}
