package com.clicksend.sdk.response;
import com.clicksend.sdk.util.Definitions;
/**
 * BalanceResult.java<br><br>
 *
 * Created on 24 August 2014, 00:23
 *
 * @author  Hüseyin ZAHMACIOĞLU
 * @version 1.0
 */
public class BalanceResult implements java.io.Serializable {

	private static final long serialVersionUID = -4529120732669975094L;
    private final String statusCode;
    private final String errorText;
    
    private final String balance;
    private final String credit;
    private final String type;
    private final String currencySymbol;
    private final String errorDescription;


    public BalanceResult(final String statusCode,
                                  final String errorText,
                                  final String balance,
                                  final String credit,
                                  final String type,
                                  final String currencySymbol) {
        this.statusCode = statusCode;
        this.errorText = errorText;
        this.balance = balance;
        this.credit = credit;
        this.type = type;
        this.currencySymbol = currencySymbol;

        this.errorDescription = Definitions.GET_BALANCE_RESPONSE_CODES_MAP.get(statusCode);
    }

    public String getStatusCode() {
        return this.statusCode;
    }

    public String getErrorText() {
        return this.errorText;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

	public String getBalance() {
		return balance;
	}

	public String getCredit() {
		return credit;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public String getType() {
		return type;
	}
    
    
    @Override
	public String toString() {
		return "BalanceResult [statusCode=" + statusCode + ", errorText="
				+ errorText + ", balance=" + balance + ", credit=" + credit
				+ ", type=" + type + ", currencySymbol=" + currencySymbol
				+ ", errorDescription=" + errorDescription + "]";
	}

}
