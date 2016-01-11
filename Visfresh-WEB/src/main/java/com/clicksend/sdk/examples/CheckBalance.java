package com.clicksend.sdk.examples;

import com.clicksend.sdk.ClickSendSmsClient;
import com.clicksend.sdk.request.Balance;
import com.clicksend.sdk.response.BalanceResult;
import com.clicksend.sdk.util.Definitions;

/**
 * CheckBalance.java<br><br>
 *
 * An example of using the ClickSend sms api to retrieve account balance ...<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class CheckBalance {

    public static final String USERNAME = "ClickSendUsername";
    public static final String API_KEY = "ClickSendApiKey";


    public static void main(String[] args) {
        // Create a client for submitting to ClieckSend
        ClickSendSmsClient client = null;
        try {
            client = new ClickSendSmsClient(USERNAME, API_KEY);
        } catch (Exception e) {
            System.err.println("Failed to create a new ClickSend Java Client");
            e.printStackTrace();
            throw new RuntimeException("Failed to create a new ClickSend Java Client");
        }

        // Create a Balance request object ...
        Balance balance = new Balance("AU");

        // Use the ClickSend client to submit the Balance request ...
        BalanceResult result = null;
        
        try {
            result = client.getBalance(balance);
        } catch (Exception e) {
            System.err.println("Failed to communicate with the ClickSend Server");
            e.printStackTrace();
            throw new RuntimeException("Failed to communicate with the ClickSend Server");
        }

            System.out.println(" [ " + result + " ] ...");
            
            System.out.println("Status [ " + result.getStatusCode() + " ] ...");
            if (result.getStatusCode().equals(Definitions.STATUS_OK)){
                System.out.println("SUCCESS");
            }else{
                System.out.println("REQUEST FAILED!");
            }
            
            System.out.println("Balance 			[ " + result.getBalance() + " ] ...");
            System.out.println("Credit 				[ " + result.getCredit() + " ] ...");
            System.out.println("CurrencySymbol		[ " + result.getCurrencySymbol() + " ] ...");
            System.out.println("ErrorDescription 	[ " + result.getErrorDescription() + " ] ...");
            System.out.println("ErrorText 			[ " + result.getErrorText() + " ] ...");
            System.out.println("StatusCode 			[ " + result.getStatusCode() + " ] ...");
            System.out.println("Type 				[ " + result.getType() + " ] ...");
            
    }

}
