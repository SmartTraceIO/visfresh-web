package com.clicksend.sdk.examples.push;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clicksend.request.push.PushMessage;

/**
 * PushMessage.java<br><br>
 *
 * A Java servlet example of using the Clicksend sms api to receive a SMS as push ...<br><br>
 *
 * Created on 06 December 2014, 17:26
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class GetPushMessage {

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		//----------- Pass HttpServletRequest Object to PushMessage Class -----------//
		PushMessage pushMessage = new PushMessage(req);
		
		//----------- WRITING INCOMING PARAMETERS TO CONSOLE --------------------------//
		System.out.println("From: " + pushMessage.getFrom()); //Recipient Mobile Number that sent the reply message.
		System.out.println("Message: " + pushMessage.getMessage()); //Reply SMS message body.
		System.out.println("Originalmessage: " + pushMessage.getOriginalMessage()); //Original SMS message body.
		System.out.println("Originalmessageid: " + pushMessage.getOriginalMessageId()); //Original SMS message ID. Returned when originally sending the message.
		System.out.println("Originalsenderid: " + pushMessage.getOriginalSenderId()); //Original mobile number (sender ID) that the SMS was sent from.
		System.out.println("Customstring: " + pushMessage.getCustomString()); //A custom System.out.println("used when sending the original message.
		System.out.println("Username: " + pushMessage.getUserName()); //The API username used to send the original message.
	}
}
