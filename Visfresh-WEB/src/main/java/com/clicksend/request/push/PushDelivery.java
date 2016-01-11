package com.clicksend.request.push;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;


/**
 * PushDelivery.java<br><br>
 *
 * Created on 29 December 2014, 19:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class PushDelivery {

    private final String messageId;
    private final String status;
    private final String customString;
    private final String userName;
    
    /**
     * Creates a new delivery request.<br>
     *
     * @param messageId SMS message ID. Returned when originally sending the message.
     * @throws UnsupportedEncodingException 
     */
    public PushDelivery(HttpServletRequest inComingRequest) throws UnsupportedEncodingException {
    	//-------------- CONVERTING REQUEST TO UTF-8 ENCODING ------------------------//
    	inComingRequest.setCharacterEncoding("UTF-8");
		this.messageId = inComingRequest.getParameter("messageid"); //SMS message ID. Returned when originally sending the message.
		this.status = inComingRequest.getParameter("status"); //Delivery status. Either 'Delivered' or 'Undelivered'
		this.customString = inComingRequest.getParameter("customstring"); //A custom string used when sending the original message.
		this.userName = inComingRequest.getParameter("username"); //The API username used to send the original message.
    }

	public String getMessageId() {
		return messageId;
	}

	public String getStatus() {
		return status;
	}

	public String getCustomString() {
		return customString;
	}

	public String getUserName() {
		return userName;
	}

	@Override
	public String toString() {
		return "PushDelivery [messageId=" + messageId + ", status=" + status
				+ ", customString=" + customString + ", userName=" + userName
				+ "]";
	}
}
