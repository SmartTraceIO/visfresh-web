/**
 *
 */
package au.smarttrace.ctrl.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.smarttrace.User;
import au.smarttrace.ctrl.client.resp.AnyResponse;
import au.smarttrace.ctrl.client.resp.UserListResponse;
import au.smarttrace.ctrl.req.SaveUserRequest;
import au.smarttrace.ctrl.res.ListResponse;
import au.smarttrace.user.GetUsersRequest;

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
        params.put("user", userId.toString());
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
    /**
     * @param req request to get user list.
     * @return user list response.
     * @throws IOException
     * @throws ServiceException
     */
    public ListResponse<User> getUsers(final GetUsersRequest req) throws IOException, ServiceException {
        return sendPostRequest(getPathWithToken("getUsers"), req, UserListResponse.class);
    }
    /**
     * @return
     * @throws ServiceException
     * @throws IOException
     */
    public List<?> getRoles() throws IOException, ServiceException {
        return (List<?>) sendGetRequest(getPathWithToken("getRoles"), new HashMap<>(), AnyResponse.class);
    }
}
