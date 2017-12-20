/**
 *
 */
package au.smarttrace.ctrl.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import au.smarttrace.ctrl.req.SaveUserRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserClient extends BaseClient {
    /**
     * Default constructor.
     */
    public UserClient() {
        super();
    }

    /**
     * @param req save user request.
     * @throws ServiceException
     * @throws IOException
     */
    public void createUser(final SaveUserRequest req) throws IOException, ServiceException {
        sendPostRequest(getPathWithToken("createUser"), req, AnyResponse.class);
    }
    /**
     * @param userId user's email
     * @throws ServiceException
     * @throws IOException
     */
    public void deleteUser(final Long userId) throws IOException, ServiceException {
        final Map<String, String> params = new HashMap<>();
        params.put("id", userId.toString());
        sendGetRequest(getPathWithToken("deleteUser"), params, AnyResponse.class);
    }
    /**
     * @param req save user request.
     * @throws ServiceException
     * @throws IOException
     */
    public void updateUser(final SaveUserRequest req) throws IOException, ServiceException {
        sendPostRequest(getPathWithToken("updateUser"), req, AnyResponse.class);
    }
}
