/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
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
import com.visfresh.lists.ExpandedListUserItem;
import com.visfresh.lists.ShortListUserItem;
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
        final String title = "Mrs";
        final String externalCompany = "JUnit External company";
        final boolean external = true;

        User u = new User();
        u.setId(login);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setExternalCompany(externalCompany);
        u.setExternal(external);
        u.setPosition(position);
        u.setTimeZone(timeZone);
        u.setTemperatureUnits(temperatureUnits);
        u.setDeviceGroup(deviceGroup);
        u.setLanguage(language);
        u.setMeasurementUnits(measurementUnits);
        u.setTitle(title);
        u.setRoles(new HashSet<Role>());
        u.getRoles().add(Role.BasicUser);
        u.getRoles().add(Role.NormalUser);

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
        assertEquals(title, u.getTitle());
        assertEquals(externalCompany, u.getExternalCompany());
        assertEquals(external, u.getExternal());
    }
    @Test
    public void testExpandedListUserItem() {
        final long login = 77l;
        final String firstName = "firstname";
        final String lastName = "LastName";
        final String email = "abra@cada.bra";
        final String position = "Manager";
        final String companyName = "Unit Company";
        final boolean external = true;

        ExpandedListUserItem u = new ExpandedListUserItem();
        u.setId(login);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmail(email);
        u.setPosition(position);
        u.getRoles().add(Role.BasicUser);
        u.getRoles().add(Role.NormalUser);
        u.setCompanyName(companyName);
        u.setExternal(external);

        final JsonObject obj = serializer.toJson(u);
        u = serializer.parseExpandedListUserItem(obj);

        assertEquals((Long) login, u.getId());
        assertEquals(2, u.getRoles().size());
        assertEquals(firstName, u.getFirstName());
        assertEquals(lastName, u.getLastName());
        assertEquals(email, u.getEmail());
        assertEquals(position, u.getPosition());
        assertEquals(companyName, u.getCompanyName());
        assertEquals(external, u.isExternal());
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
        final Boolean resetOnLogin = true;

        SaveUserRequest r = new SaveUserRequest();
        final User user = new User();
        user.setId(login);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPosition(position);

        r.setUser(user);
        r.setPassword(password);
        r.setResetOnLogin(resetOnLogin);

        final JsonElement json = serializer.toJson(r);
        r = serializer.parseSaveUserRequest(json);

        assertNotNull(user);
        assertEquals(password, r.getPassword());
        assertEquals(resetOnLogin, r.getResetOnLogin());
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
    public void testShortListUserItem() {
        final String fullName = "Full User Name";
        final String positionCompany = "Position in JUnit company";
        final Long id = 7777l;

        ShortListUserItem item = new ShortListUserItem();
        item.setFullName(fullName);
        item.setPositionCompany(positionCompany);
        item.setId(id);

        final JsonObject json = serializer.toJson(item);
        item = serializer.parseListUserItem(json);

        assertEquals(id, item.getId());
        assertEquals(fullName, item.getFullName());
        assertEquals(positionCompany, item.getPositionCompany());
    }
}
