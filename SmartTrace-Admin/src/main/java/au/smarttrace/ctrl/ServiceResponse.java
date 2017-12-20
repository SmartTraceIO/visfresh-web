/**
 *
 */
package au.smarttrace.ctrl;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 * @param <T> response type. Is used only on client side for correct deserialization,
 * i.e. for JUnit java request.
 */
public class ServiceResponse<T> {
    private Status status;
    private T responseObject;

    /**
     * Default constructor.
     */
    public ServiceResponse() {
        super();
    }
    /**
     * @return the status
     */
    @JsonGetter("status")
    public Status getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    @JsonSetter("status")
    public void setStatus(final Status status) {
        this.status = status;
    }
    /**
     * @return the responseObject
     */
    @JsonGetter("data")
    public T getResponseObject() {
        return responseObject;
    }
    /**
     * @param responseObject the responseObject to set
     */
    @JsonSetter("data")
    public void setResponseObject(final T responseObject) {
        this.responseObject = responseObject;
    }
}
