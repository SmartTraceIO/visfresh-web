package com.clicksend.sdk.examples;

import com.clicksend.sdk.ClickSendSmsClient;
import com.clicksend.sdk.response.ReplyResult;

/**
 * GetDeliveryResults.java<br><br>
 *
 * An example of using the clicksend sms api to retrieve replies ...<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class GetRepliesResults {

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


        ReplyResult result = null;
        try {
            result = client.getReplies();
        } catch (Exception e) {
            System.err.println("Failed to communicate with the ClickSend Server");
            e.printStackTrace();
            throw new RuntimeException("Failed to communicate with the ClickSend Server");
        }

        
        // Evaluate the results of the submission attempt ...
        System.out.println("... [ " + result.getReplyResultItems().size() + " ] REPLY received");
        
        System.out.println("ErrorDescription 	[ " + result.getErrorDescription() + " ] ...");
        System.out.println("ErrorText 			[ " + result.getErrorText() + " ] ...");
        System.out.println("Result 				[ " + result.getResult() + " ] ...");
        
        
        for (int i=0;i<result.getReplyResultItems().size();i++) {
            System.out.println("--------- REPLY [ " + (i + 1) + " ] ------------");
            System.out.println(" [ " + result.getReplyResultItems().get(i) + " ] ...");
            
            
            System.out.println("CustomString 		[ " + result.getReplyResultItems().get(i).getCustomString() + " ] ...");
            System.out.println("Message 			[ " + result.getReplyResultItems().get(i).getMessage() + " ] ...");
            System.out.println("From				[ " + result.getReplyResultItems().get(i).getFrom()+ " ] ...");
            System.out.println("OriginalMessage		[ " + result.getReplyResultItems().get(i).getOriginalMessage()+ " ] ...");
            System.out.println("OriginalMessageId	[ " + result.getReplyResultItems().get(i).getOriginalMessageId()+ " ] ...");
            System.out.println("OriginalSenderId	[ " + result.getReplyResultItems().get(i).getOriginalSenderId()+ " ] ...");
            System.out.println("TO String	[ " + result.getReplyResultItems().get(i)+ " ] ...");
        }
    }

}
