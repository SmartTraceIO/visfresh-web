package com.clicksend.sdk.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Definitions.java<br><br>
 *
 * Definitions of the ClickSend Java SDK<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Huseyin ZAHMACIOGLU
 * @version 1.0
 */

public class Definitions {
	
	 /**
     * SDK Useragent String
     */
	public static final String SDK_USER_AGENT_STRING = "ClickSend Java SDK";
	
	 /**
     * SDK User agent version, actually SDK version
     */	
	public static final String SDK_USER_AGENT_VERSION = "1.0";
	
	 /**
     * Maximum recipient count for sms sending
     */
	public static final int MAXIMUM_RECIPIENT_COUNT = 1000;
	
	 /**
     * Default connection timeout of 5000ms used by this client unless specifically overridden with the constructor
     */
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

    /**
     * Default read timeout of 30000ms used by this client unless specifically overridden with the constructor
     */
    public static final int DEFAULT_SOCKET_TIMEOUT = 30000;
	
	/**
     * https://api.clicksend.com/rest/v2/send.xml<br>
     * SMS Send URL (Secure Connection)
     */
    public static final String DEFAULT_SEND_SMS_BASE_URL_SECURE = "https://api.clicksend.com/rest/v2/send.xml";
    
	/**
     * http://api.clicksend.com/rest/v2/send.xml<br>
     * SMS Send URL (Non Secure Connection)
     */
    public static final String DEFAULT_SEND_SMS_BASE_URL_NONSECURE = "http://api.clicksend.com/rest/v2/send.xml";

	/**
     * https://api.clicksend.com/rest/v2/balance.xml<br>
     * Balance Check URL (Secure Connection)
     */
    public static final String DEFAULT_GET_BALANCE_BASE_URL_SECURE = "https://api.clicksend.com/rest/v2/balance.xml";
    
	/**
     * http://api.clicksend.com/rest/v2/balance.xml<br>
     * Balance Check URL (Non Secure Connection)
     */
    public static final String DEFAULT_GET_BALANCE_BASE_URL_NONSECURE = "http://api.clicksend.com/rest/v2/balance.xml";

	/**
     * https://api.clicksend.com/rest/v2/balance.xml<br>
     * Balance Check URL (Secure Connection)
     */
    public static final String DEFAULT_GET_DLR_BASE_URL_SECURE = "https://api.clicksend.com/rest/v2/delivery.xml";
    
	/**
     * http://api.clicksend.com/rest/v2/balance.xml<br>
     * Balance Check URL (Non Secure Connection)
     */
    public static final String DEFAULT_GET_DLR_BASE_URL_NONSECURE = "http://api.clicksend.com/rest/v2/delivery.xml";

    
	/**
     * https://api.clicksend.com/rest/v2/balance.xml<br>
     * Balance Check URL (Secure Connection)
     */
    public static final String DEFAULT_GET_REPLY_BASE_URL_SECURE = "https://api.clicksend.com/rest/v2/reply.xml";
    
	/**
     * http://api.clicksend.com/rest/v2/balance.xml<br>
     * Balance Check URL (Non Secure Connection)
     */
    public static final String DEFAULT_GET_REPLY_BASE_URL_NONSECURE = "http://api.clicksend.com/rest/v2/reply.xml";

    
    
	/**
     * For "method" parameter<br>
     * Always rest
     */
    public static final String API_TYPE = "rest";
    
	/**
     * For successfully response status code<br>
     * 0000
     */ 
	public static final String STATUS_OK = "0000";
	
	/**
     * For successfully message delivered string<br>
     * Delivered
     */ 
	public static final String STATUS_DELIVERED = "delivered";
	
	
	
	/**
     * Send sms response status codes<br>
     */  
    public static final Map<String, String> SEND_SMS_RESPONSE_CODES_MAP = new HashMap<String, String>();
    static{
    	SEND_SMS_RESPONSE_CODES_MAP.put("LOCAL500", "ERROR");
    	SEND_SMS_RESPONSE_CODES_MAP.put("0000", "Message added to queue OK.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2006", "Not enough information has been supplied for authentication. Please ensure that your Username and Unique Key are supplied in your request.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2007", "Your account has not been activated.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2015", "The destination mobile number is invalid.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2016", "Identical message already sent to this recipient. Please try again in a few seconds.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2017", "Invalid Sender ID. Please ensure Sender ID is no longer than 11 characters (if alphanumeric), and contains no spaces.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2018", "You have reached the end of your message credits. You will need to purchase more message credits.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2022", "Your Username or Unique Key is incorrect.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2051", "Message is empty.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2052", "Too many recipients.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2100", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2101", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2102", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2103", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2104", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2105", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2106", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2107", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2108", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2109", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2110", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2111", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2112", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2113", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2114", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2115", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2116", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2117", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2118", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2119", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2120", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2121", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2122", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2123", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2124", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2125", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2126", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2127", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2128", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2129", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2130", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2131", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2132", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2133", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2134", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2135", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2136", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2137", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2138", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2139", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2140", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2141", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2142", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2143", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2144", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2145", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2146", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2147", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2148", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2149", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2150", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2151", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2152", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2153", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2154", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2155", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2156", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2157", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2158", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2159", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2160", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2161", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2162", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2163", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2164", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2165", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2166", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2167", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2168", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2169", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2170", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2171", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2172", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2173", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2174", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2175", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2176", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2177", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2178", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2179", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2180", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2181", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2182", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2183", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2184", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2185", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2186", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2187", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2188", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2189", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2190", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2191", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2192", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2193", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2194", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2195", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2196", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2197", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2198", "Internal error.");
	    SEND_SMS_RESPONSE_CODES_MAP.put("2199", "Internal error.");
    }
    
	/**
     * Gel balance response status codes<br>
     */  
    public static final Map<String, String> GET_BALANCE_RESPONSE_CODES_MAP = new HashMap<String, String>();
    static{
    	GET_BALANCE_RESPONSE_CODES_MAP.put("LOCAL500", "ERROR");
    	GET_BALANCE_RESPONSE_CODES_MAP.put("0000", "Retrieved account balance OK.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2006", "Not enough information has been supplied for authentication. Please ensure that your Username and Unique Key are supplied in your request.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2007", "Your account has not been activated.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2022", "Your Username or Unique Key is incorrect.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2100", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2101", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2102", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2103", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2104", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2105", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2106", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2107", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2108", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2109", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2110", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2111", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2112", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2113", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2114", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2115", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2116", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2117", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2118", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2119", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2120", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2121", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2122", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2123", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2124", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2125", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2126", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2127", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2128", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2129", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2130", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2131", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2132", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2133", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2134", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2135", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2136", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2137", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2138", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2139", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2140", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2141", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2142", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2143", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2144", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2145", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2146", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2147", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2148", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2149", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2150", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2151", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2152", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2153", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2154", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2155", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2156", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2157", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2158", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2159", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2160", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2161", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2162", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2163", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2164", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2165", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2166", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2167", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2168", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2169", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2170", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2171", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2172", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2173", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2174", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2175", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2176", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2177", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2178", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2179", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2180", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2181", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2182", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2183", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2184", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2185", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2186", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2187", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2188", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2189", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2190", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2191", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2192", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2193", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2194", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2195", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2196", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2197", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2198", "Internal error.");
	    GET_BALANCE_RESPONSE_CODES_MAP.put("2199", "Internal error.");
    }
    
    /**
     * Gel balance response status codes<br>
     */  
    public static final Map<String, String> GET_DLR_RESPONSE_CODES_MAP = new HashMap<String, String>();
    static{
    	GET_DLR_RESPONSE_CODES_MAP.put("LOCAL500", "ERROR");
    	GET_DLR_RESPONSE_CODES_MAP.put("0000", "Checked the system for delivery reports OK. Note: This doesn’t mean the message was delivered successfully – it just means the API has checked for available reports successfully. Even if no delivery reports were available, it will still return 0000 Success. Check the dlrcount value to determine the number of delivery reports in the response.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2006", "Not enough information has been supplied for authentication. Please ensure that your Username and Unique Key are supplied in your request.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2007", "Your account has not been activated.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2022", "Your Username or Unique Key is incorrect.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2100", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2101", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2102", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2103", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2104", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2105", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2106", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2107", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2108", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2109", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2110", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2111", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2112", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2113", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2114", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2115", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2116", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2117", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2118", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2119", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2120", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2121", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2122", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2123", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2124", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2125", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2126", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2127", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2128", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2129", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2130", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2131", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2132", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2133", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2134", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2135", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2136", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2137", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2138", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2139", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2140", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2141", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2142", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2143", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2144", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2145", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2146", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2147", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2148", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2149", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2150", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2151", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2152", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2153", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2154", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2155", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2156", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2157", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2158", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2159", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2160", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2161", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2162", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2163", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2164", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2165", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2166", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2167", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2168", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2169", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2170", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2171", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2172", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2173", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2174", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2175", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2176", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2177", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2178", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2179", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2180", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2181", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2182", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2183", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2184", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2185", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2186", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2187", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2188", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2189", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2190", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2191", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2192", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2193", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2194", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2195", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2196", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2197", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2198", "Internal error.");
	    GET_DLR_RESPONSE_CODES_MAP.put("2199", "Internal error.");
    } 
    
    
    /**
     * Gel balance response status codes<br>
     */  
    public static final Map<String, String> GET_REPLY_RESPONSE_CODES_MAP = new HashMap<String, String>();
    static{
    	GET_REPLY_RESPONSE_CODES_MAP.put("LOCAL500", "ERROR");
    	GET_REPLY_RESPONSE_CODES_MAP.put("0000", "Checked the system for replies OK. Note: This doesn’t mean the response contains replies – it just means the API has checked for available replies successfully. Even if no replies were available, it will still return 0000 Success. Check the replycount value to determine the number of replies in the response.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2006", "Not enough information has been supplied for authentication. Please ensure that your Username and Unique Key are supplied in your request.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2007", "Your account has not been activated.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2022", "Your Username or Unique Key is incorrect.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2100", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2101", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2102", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2103", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2104", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2105", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2106", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2107", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2108", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2109", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2110", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2111", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2112", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2113", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2114", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2115", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2116", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2117", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2118", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2119", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2120", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2121", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2122", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2123", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2124", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2125", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2126", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2127", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2128", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2129", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2130", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2131", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2132", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2133", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2134", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2135", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2136", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2137", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2138", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2139", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2140", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2141", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2142", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2143", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2144", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2145", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2146", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2147", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2148", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2149", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2150", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2151", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2152", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2153", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2154", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2155", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2156", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2157", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2158", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2159", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2160", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2161", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2162", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2163", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2164", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2165", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2166", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2167", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2168", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2169", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2170", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2171", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2172", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2173", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2174", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2175", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2176", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2177", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2178", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2179", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2180", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2181", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2182", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2183", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2184", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2185", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2186", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2187", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2188", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2189", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2190", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2191", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2192", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2193", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2194", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2195", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2196", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2197", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2198", "Internal error.");
	    GET_REPLY_RESPONSE_CODES_MAP.put("2199", "Internal error.");
    }
}