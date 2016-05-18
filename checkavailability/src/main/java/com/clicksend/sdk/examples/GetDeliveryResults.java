package com.clicksend.sdk.examples;

import com.clicksend.sdk.ClickSendSmsClient;
import com.clicksend.sdk.request.Delivery;
import com.clicksend.sdk.response.DeliveryResult;
import com.clicksend.sdk.util.Definitions;

/**
 * GetDeliveryResults.java<br><br>
 *
 * An example of using the clicksend sms api to retrieve delivery reports ...<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class GetDeliveryResults {

    public static final String USERNAME = "ClickSendUsername";
    public static final String API_KEY = "ClickSendApiKey";

    public static void main(String[] args) {

        ClickSendSmsClient client = null;
        try {
            client = new ClickSendSmsClient(USERNAME, API_KEY);
        } catch (Exception e) {
            System.err.println("Failed to create a new ClickSend Java Client");
            e.printStackTrace();
            throw new RuntimeException("Failed to create a new ClickSend Java Client");
        }

        Delivery delivery = new Delivery("2CF8D933-6F3A-F080-981D-12FB2B3CAA6A");

        DeliveryResult result = null;
        try {
            result = client.getDlr(delivery);
        } catch (Exception e) {
            System.err.println("Failed to communicate with the ClickSend Server");
            e.printStackTrace();
            throw new RuntimeException("Failed to communicate with the ClickSend Server");
        }

        
        // Evaluate the results of the submission attempt ...
        System.out.println("... [ " + result.getDeliveryResultItems().size() + " ] DLR received");
        System.out.println("ErrorDescription 	[ " + result.getErrorDescription() + " ] ...");
        System.out.println("ErrorText 			[ " + result.getErrorText() + " ] ...");
        System.out.println("Result 				[ " + result.getResult() + " ] ...");
        for (int i=0;i<result.getDeliveryResultItems().size();i++) {
            System.out.println("--------- DLR [ " + (i + 1) + " ] ------------");
            System.out.println(" [ " + result.getDeliveryResultItems().get(i) + " ] ...");
            System.out.println("Status [ " + result.getDeliveryResultItems().get(i).getStatusCode() + " ] ...");
            if (result.getDeliveryResultItems().get(i).getStatusCode().equalsIgnoreCase(Definitions.STATUS_DELIVERED)){
                System.out.println("SUCCESS");
            }else{
                System.out.println("REQUEST FAILED!");
            }
            System.out.println("CustomString 	[ " + result.getDeliveryResultItems().get(i).getCustomString() + " ] ...");
            System.out.println("MessageId 		[ " + result.getDeliveryResultItems().get(i).getMessageId() + " ] ...");
            System.out.println("StatusCode		[ " + result.getDeliveryResultItems().get(i).getStatusCode()+ " ] ...");
            System.out.println("Username		[ " + result.getDeliveryResultItems().get(i).getUserName()+ " ] ...");
            
        }
    }

}
