package com.clicksend.sdk.examples.push;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clicksend.request.push.PushDelivery;

/**
 * GetPushDlr.java<br><br>
 *
 * A Java servlet example of using the Clicksend sms api to receive a SMS Delivery result as push ...<br><br>
 *
 * Created on 06 December 2014, 17:26
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class GetPushDlr {

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws UnsupportedEncodingException {
		
		//----------- Pass HttpServletRequest Object to PushDelivery Class -----------//
		PushDelivery delivery = new PushDelivery(req);
		//----------- WRITING INCOMING PARAMETERS TO CONSOLE --------------------------//
		System.out.println("Messageid: " + delivery.getMessageId()); //SMS message ID. Returned when originally sending the message.
		System.out.println("Status: " + delivery.getStatus()); //Delivery status. Either 'Delivered' or 'Undelivered'
		System.out.println("Customstring: " + delivery.getCustomString()); //A custom string used when sending the original message.
		System.out.println("Username: " + delivery.getUserName()); //The API username used to send the original message.
	}
}
