/**
 *
 */
package au.smarttrace.ctrl.req;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import au.smarttrace.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SaveUserRequest extends User {
    private String password;

    /**
     * Default constructor.
     */
    public SaveUserRequest() {
        super();
    }
    /**
     * @return the password
     */
    @JsonGetter("password")
    public String getPassword() {
        return password;
    }
    /**
     * @param password the password to set
     */
    @JsonSetter("password")
    public void setPassword(final String password) {
        this.password = password;
    }
}
