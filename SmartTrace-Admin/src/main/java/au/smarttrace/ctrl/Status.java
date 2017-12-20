/**
 *
 */
package au.smarttrace.ctrl;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Status {
    private int code;
    private String message;

    /**
     * Default constructor.
     */
    public Status() {
        this(0, null);
    }

    /**
     * @param statusCode
     * @param msg
     */
    public Status(final int statusCode, final String msg) {
        this.code = statusCode;
        this.message = msg;
    }

    /**
     * @return the code
     */
    @JsonGetter("code")
    public int getCode() {
        return code;
    }
    /**
     * @param code the code to set
     */
    @JsonSetter("code")
    public void setCode(final int code) {
        this.code = code;
    }
    /**
     * @return the message
     */
    @JsonGetter("message")
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    @JsonSetter("message")
    public void setMessage(final String message) {
        this.message = message;
    }
}
