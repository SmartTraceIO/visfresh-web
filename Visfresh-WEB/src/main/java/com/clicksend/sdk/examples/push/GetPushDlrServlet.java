package com.clicksend.sdk.examples.push;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GetPushDlrServlet.java<br><br>
 *
 * A Java servlet example of using the Clicksend sms api to receive a SMS Delivery result as push ...<br><br>
 *
 * Created on 06 December 2014, 17:26
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class GetPushDlrServlet {

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
		
		String messageid = request.getParameter("messageid"); //SMS message ID. Returned when originally sending the message.
		String status = request.getParameter("status"); //Delivery status. Either 'Delivered' or 'Undelivered'
		String customstring = request.getParameter("customstring"); //A custom string used when sending the original message.
		String username = request.getParameter("username"); //The API username used to send the original message.
		
		//----------- WRITING INCOMING PARAMETERS TO CONSOLE --------------------------//
		System.out.println("Messageid: " + messageid); //SMS message ID. Returned when originally sending the message.
		System.out.println("Status: " + status); //Delivery status. Either 'Delivered' or 'Undelivered'
		System.out.println("Customstring: " + customstring); //A custom string used when sending the original message.
		System.out.println("Username: " + username); //The API username used to send the original message.

		
		//----------- WRITING INCOMING PARAMETERS AS HTML OUTPUT --------------------------//
		response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    out.println("<html>");
	    out.println("<head>");
	    out.println("<title>PUSH SMS</title>");
	    out.println("</head>");
	    out.println("<body bgcolor=\"white\">");
		out.println("Messageid: " + messageid); //SMS message ID. Returned when originally sending the message.
		out.println("Status: " + status); //Delivery status. Either 'Delivered' or 'Undelivered'
		out.println("Customstring: " + customstring); //A custom string used when sending the original message.
		out.println("Username: " + username); //The API username used to send the original message.
	    out.println("</body>");
	    out.println("</html>");
	}
	
	
	
}
