package com.clicksend.request.push;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

/**
 * PushMessage.java<br><br>
 *
 * Created on 29 December 2014, 19:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class PushMessage {

    private final String from;
    private final String message;
	private final String originalMessage;
    private final String originalMessageId;
    private final String originalSenderId;
    private final String customString;
    private final String userName;

	public PushMessage(HttpServletRequest inComingRequest) throws UnsupportedEncodingException  {
		//-------------- CONVERTING REQUEST TO UTF-8 ENCODING ------------------------//
		inComingRequest.setCharacterEncoding("UTF-8");
		
		this.from = inComingRequest.getParameter("from"); //Recipient Mobile Number that sent the reply message.
		this.message = inComingRequest.getParameter("message"); //Reply SMS message body.
		this.originalMessage = inComingRequest.getParameter("originalmessage"); //Original SMS message body.
		this.originalMessageId = inComingRequest.getParameter("originalmessageid"); //Original SMS message ID. Returned when originally sending the message.
		this.originalSenderId = inComingRequest.getParameter("originalsenderid"); //Original mobile number (sender ID) that the SMS was sent from.
		this.customString = inComingRequest.getParameter("customstring"); //A custom string used when sending the original message.
		this.userName = inComingRequest.getParameter("username"); //The API username used to send the original message.
	}

	public String getFrom() {
		return from;
	}

	public String getMessage() {
		return message;
	}

	public String getOriginalMessage() {
		return originalMessage;
	}

	public String getOriginalMessageId() {
		return originalMessageId;
	}

	public String getOriginalSenderId() {
		return originalSenderId;
	}

	public String getCustomString() {
		return customString;
	}

	public String getUserName() {
		return userName;
	}
	
    @Override
	public String toString() {
		return "PushMessage [from=" + from + ", message=" + message
				+ ", originalMessage=" + originalMessage
				+ ", originalMessageId=" + originalMessageId
				+ ", originalSenderId=" + originalSenderId + ", customString="
				+ customString + ", userName=" + userName + "]";
	}
	
}
