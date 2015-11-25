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
import com.visfresh.entities.Language;
import com.visfresh.entities.MeasurementUnits;
import com.visfresh.entities.Role;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.SaveUserRequest;
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
        final long login = 77l;
        final TimeZone timeZone = TimeZone.getTimeZone("Europe/Moscow");
        final TemperatureUnits temperatureUnits = TemperatureUnits.Fahrenheit;
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String phone = "1111111117";
        final String position = "Manager";
        final String deviceGroup = "DeviceGroupName";
        final Language language = Language.English;
        final MeasurementUnits measurementUnits = MeasurementUnits.English;
        final String scale = "scale";
        final String title = "Mrs";

        User u = new User();
        u.setId(login);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPosition(position);
        u.setTimeZone(timeZone);
        u.setTemperatureUnits(temperatureUnits);
        u.setDeviceGroup(deviceGroup);
        u.setLanguage(language);
        u.setMeasurementUnits(measurementUnits);
        u.setScale(scale);
        u.setTitle(title);
        u.getRoles().add(Role.Dispatcher);
        u.getRoles().add(Role.ReportViewer);

        final JsonObject obj = serializer.toJson(u);
        u = serializer.parseUser(obj);

        assertEquals((Long) login, u.getId());
        assertEquals(2, u.getRoles().size());
        assertEquals(timeZone, u.getTimeZone());
        assertEquals(temperatureUnits, u.getTemperatureUnits());
        assertEquals(firstName, u.getFirstName());
        assertEquals(lastName, u.getLastName());
        assertEquals(email, u.getEmail());
        assertEquals(phone, u.getPhone());
        assertEquals(position, u.getPosition());
        assertEquals(deviceGroup, u.getDeviceGroup());
        assertEquals(language, u.getLanguage());
        assertEquals(measurementUnits, u.getMeasurementUnits());
        assertEquals(scale, u.getScale());
        assertEquals(title, u.getTitle());
    }
    @Test
    public void testCreateUserRequest() {
        final Company c = new Company();
        c.setId(7l);
        c.setName("JUnit");
        c.setDescription("Test company");
        resolver.add(c);

        final Long login = 78l;
        final String password = "anypassword";
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String phone = "1111111117";
        final String position = "Manager";

        SaveUserRequest r = new SaveUserRequest();
        final User user = new User();
        user.setId(login);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPosition(position);

        r.setCompany(c);
        r.setUser(user);
        r.setPassword(password);

        final JsonElement json = serializer.toJson(r);
        r = serializer.parseSaveUserRequest(json);

        assertNotNull(user);
        assertNotNull(r.getCompany());
        assertEquals(password, r.getPassword());
    }
    @Test
    public void getUpdateUserDetailsRequest() {
        final String password = "password";
        final TemperatureUnits tu = TemperatureUnits.Fahrenheit;
        final TimeZone tz = TimeZone.getTimeZone("GMT+3");
        final Long login = 79l;
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String phone = "1111111117";
        final String position = "Manager";
        final MeasurementUnits units = MeasurementUnits.English;
        final Language language = Language.English;
        final String scale = "scale";
        final String title = "Developer";

        UpdateUserDetailsRequest req = new UpdateUserDetailsRequest();
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setEmail(email);
        req.setPhone(phone);
        req.setPosition(position);
        req.setPassword(password);
        req.setTemperatureUnits(tu);
        req.setTimeZone(tz);
        req.setUser(login);
        req.setMeasurementUnits(units);
        req.setLanguage(language);
        req.setScale(scale);
        req.setTitle(title);

        final JsonElement json = serializer.toJson(req);
        req = serializer.parseUpdateUserDetailsRequest(json);

        assertEquals(password, req.getPassword());
        assertEquals(tu, req.getTemperatureUnits());
        assertEquals(tz, req.getTimeZone());
        assertEquals(login, req.getUser());
        assertEquals(firstName, req.getFirstName());
        assertEquals(lastName, req.getLastName());
        assertEquals(email, req.getEmail());
        assertEquals(phone, req.getPhone());
        assertEquals(position, req.getPosition());
        assertEquals(units, req.getMeasurementUnits());
        assertEquals(language, req.getLanguage());
        assertEquals(scale, req.getScale());
        assertEquals(title, req.getTitle());
    }
}
