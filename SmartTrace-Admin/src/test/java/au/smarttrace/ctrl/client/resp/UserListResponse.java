/**
 *
 */
package au.smarttrace.ctrl.client.resp;

import au.smarttrace.User;
import au.smarttrace.ctrl.ServiceResponse;
import au.smarttrace.ctrl.res.ListResponse;

/**
 * Wrapper class for use it by fastxml JSON deserializer (ObjectMapper).
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserListResponse extends ServiceResponse<ListResponse<User>> {
    /**
     * Default constructor.
     */
    public UserListResponse() {
        super();
    }
}
