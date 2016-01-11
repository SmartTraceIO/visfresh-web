package com.clicksend.sdk.examples.push;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class GetPushMessageServlet {

	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req,resp);
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws RuntimeException {
		//------- ALL PUSH REQUEST COMING WITH POST METHOD SO FOR SECURITY REASONS DO NOT IMPLEMENT GET METHOD ----//
		throw new RuntimeException("Only POST...");
	}

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		//-------------- CONVERTING REQUEST TO UTF-8 ENCODING ------------------------//
		request.setCharacterEncoding("UTF-8");
		
		String from = request.getParameter("from"); //Recipient Mobile Number that sent the reply message.
		String message = request.getParameter("message"); //Reply SMS message body.
		String originalmessage = request.getParameter("originalmessage"); //Original SMS message body.
		String originalmessageid = request.getParameter("originalmessageid"); //Original SMS message ID. Returned when originally sending the message.
		String originalsenderid = request.getParameter("originalsenderid"); //Original mobile number (sender ID) that the SMS was sent from.
		String customstring = request.getParameter("customstring"); //A custom string used when sending the original message.
		String username = request.getParameter("username"); //The API username used to send the original message.
		
		
		//----------- WRITING INCOMING PARAMETERS TO CONSOLE --------------------------//
		System.out.println("From: " + from); //Recipient Mobile Number that sent the reply message.
		System.out.println("Message: " + message); //Reply SMS message body.
		System.out.println("Originalmessage: " + originalmessage); //Original SMS message body.
		System.out.println("Originalmessageid: " + originalmessageid); //Original SMS message ID. Returned when originally sending the message.
		System.out.println("Originalsenderid: " + originalsenderid); //Original mobile number (sender ID) that the SMS was sent from.
		System.out.println("Customstring: " + customstring); //A custom System.out.println("used when sending the original message.
		System.out.println("Username: " + username); //The API username used to send the original message.

		
		//----------- WRITING INCOMING PARAMETERS AS HTML OUTPUT --------------------------//
		response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    out.println("<html>");
	    out.println("<head>");
	    out.println("<title>PUSH SMS</title>");
	    out.println("</head>");
	    out.println("<body bgcolor=\"white\">");
	    out.println("From: " + from); //Recipient Mobile Number that sent the reply message.
	    out.println("Message: " + message); //Reply SMS message body.
		out.println("Originalmessage: " + originalmessage); //Original SMS message body.
		out.println("Originalmessageid: " + originalmessageid); //Original SMS message ID. Returned when originally sending the message.
		out.println("Originalsenderid: " + originalsenderid); //Original mobile number (sender ID) that the SMS was sent from.
		out.println("Customstring: " + customstring); //A custom System.out.println("used when sending the original message.
		out.println("Username: " + username); //The API username used to send the original message.
	    out.println("</body>");
	    out.println("</html>");
	}
	
	
	
}
