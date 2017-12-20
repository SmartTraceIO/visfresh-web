/**
 *
 */
package au.smarttrace.ctrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.smarttrace.Roles;
import au.smarttrace.ctrl.req.SaveUserRequest;
import au.smarttrace.user.UsersService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("users")
@RequestMapping(produces = "application/json;charset=UTF-8")
public class UsersController {
    @Autowired
    private UsersService service;

    /**
     * Default constructor.
     */
    public UsersController() {
        super();
    }

    /**
     * Creates new user.
     * @param user user.
     * @param password password.
     */
    @RequestMapping(value = "/createUser", method = RequestMethod.POST)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public Long createUser(final @RequestBody SaveUserRequest req) {
        service.createUser(req, req.getPassword());
        return req.getId();
    }
    /**
     * Creates new user.
     * @param user user.
     * @param password password.
     */
    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public Long updateUser(final @RequestBody SaveUserRequest req) {
        service.updateUser(req, req.getPassword());
        return req.getId();
    }
    /**
     * Deletes user by given email.
     * @param email user's email.
     */
    @RequestMapping(value = "/deleteUser", method = RequestMethod.GET)
    @Secured({"ROLE_" + Roles.SmartTraceAdmin})
    public String deleteUser(final @RequestParam Long user) {
        service.deleteUser(user);
        return "OK";
    }
}
