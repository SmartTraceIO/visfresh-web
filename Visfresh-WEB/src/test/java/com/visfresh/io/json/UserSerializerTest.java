/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.Company;
import com.visfresh.entities.Role;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.io.UpdateUserDetailsRequest;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UserSerializerTest extends AbstractSerializerTest {
    private UserSerializer serializer;
    /**
     * Default constructor.
     */
    public UserSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new UserSerializer(SerializerUtils.UTС);
        serializer.setCompanyResolver(resolver);
        serializer.setShipmentResolver(resolver);
    }

    @Test
    public void testUser() {
        final String login = "login";
        final String fullName = "Full Name";
        final TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");
        final TemperatureUnits temperatureUnits = TemperatureUnits.Fahrenheit;

        User u = new User();
        u.setLogin(login);
        u.setFullName(fullName);
        u.setTimeZone(timeZone);
        u.setTemperatureUnits(temperatureUnits);
        u.getRoles().add(Role.Dispatcher);
        u.getRoles().add(Role.ReportViewer);

        final JsonObject obj = serializer.toJson(u);
        u = serializer.parseUser(obj);

        assertEquals(login, u.getLogin());
        assertEquals(fullName, u.getFullName());
        assertEquals(2, u.getRoles().size());
        assertEquals(timeZone, u.getTimeZone());
        assertEquals(temperatureUnits, u.getTemperatureUnits());
    }
    public void testUserProfile() {
        UserProfile p = new UserProfile();
        p.getShipments().add(createShipment());
        p.getShipments().add(createShipment());

        final JsonElement obj = serializer.toJson(p);
        p = serializer.parseUserProfile(obj);

        assertEquals(2, p.getShipments().size());
    }
    @Test
    public void testCreateUserRequest() {
        final Company c = new Company();
        c.setId(7l);
        c.setName("JUnit");
        c.setDescription("Test company");
        resolver.add(c);

        final String login = "newuser";
        final String fullName = "Full User Name";
        final String password = "anypassword";

        CreateUserRequest r = new CreateUserRequest();
        final User user = new User();
        user.setLogin(login);
        user.setFullName(fullName);

        r.setCompany(c);
        r.setUser(user);
        r.setPassword(password);

        final JsonElement json = serializer.toJson(r);
        r = serializer.parseCreateUserRequest(json);

        assertNotNull(user);
        assertNotNull(r.getCompany());
        assertEquals(password, r.getPassword());
    }
    @Test
    public void getUpdateUserDetailsRequest() {
        final String fullName = "Full User Name";
        final String password = "password";
        final TemperatureUnits tu = TemperatureUnits.Fahrenheit;
        final TimeZone tz = TimeZone.getTimeZone("GMT+3");
        final String login = "login";

        UpdateUserDetailsRequest req = new UpdateUserDetailsRequest();
        req.setFullName(fullName);
        req.setPassword(password);
        req.setTemperatureUnits(tu);
        req.setTimeZone(tz);
        req.setUser(login);

        final JsonElement json = serializer.toJson(req);
        req = serializer.parseUpdateUserDetailsRequest(json);

        assertEquals(fullName, req.getFullName());
        assertEquals(password, req.getPassword());
        assertEquals(tu, req.getTemperatureUnits());
        assertEquals(tz, req.getTimeZone());
        assertEquals(login, req.getUser());
    }
}
