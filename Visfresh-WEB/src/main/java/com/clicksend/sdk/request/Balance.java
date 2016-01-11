package com.clicksend.sdk.request;

/**
 * Balance.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class Balance implements java.io.Serializable {

	private static final long serialVersionUID = 5156751877794107196L;
	
	private final String countryCode;

    /**
     * Creates a new Balance request.
     */
    public Balance() {
    	this.countryCode = null;
    }

    /**
     * Creates a new Balance request.
     * 
     * @param countryCode   A 2-letter country code (ISO 3166-1 Alpha-2 code).
							e.g. "AU" = Australia.
							If provided, the response will show the account balance and the number of SMS messages you can send to the country specified (credit).
							If the country isn't provided, the response will only show the account balance.
     */
    public Balance(final String countryCode) {
    	this.countryCode = countryCode;
    	
    }

	public String getCountryCode() {
		return countryCode;
	}
	
	@Override
	public String toString() {
		return "Balance [countryCode=" + countryCode + "]";
	}

}
