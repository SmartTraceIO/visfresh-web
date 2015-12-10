/**
 *
 */
package com.visfresh.services.lists;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShortListUserItemTest {
    /**
     * Default constructor.
     */
    public ShortListUserItemTest() {
        super();
    }

    @Test
    public void testPositionCompanyInternalUser() {
        final String companyName = "JUnit company";
        final String position = "Docker";

        final Company c = new Company();
        c.setName(companyName);

        final User u = new User();
        u.setCompany(c);
        u.setExternal(false);
        u.setPosition(position);

        //position is not null
        assertEquals(position + " - " + companyName, new ShortListUserItem(u).getPositionCompany());

        //position is null
        u.setPosition(null);
        assertEquals(companyName, new ShortListUserItem(u).getPositionCompany());
    }
    @Test
    public void testPositionCompanyExternalUser() {
        final String companyName = "JUnit company";
        final String position = "Docker";

        final User u = new User();
        u.setExternal(true);
        u.setPosition(position);
        u.setExternalCompany(companyName);

        //position is not null
        assertEquals(position + " - " + companyName, new ShortListUserItem(u).getPositionCompany());

        //position is null
        u.setPosition(null);
        assertEquals(companyName, new ShortListUserItem(u).getPositionCompany());

        //company is null
        u.setPosition(position);
        u.setExternalCompany(null);
        assertEquals(position, new ShortListUserItem(u).getPositionCompany());

        //both are null
        u.setPosition(null);
        u.setExternalCompany(null);
        assertEquals("", new ShortListUserItem(u).getPositionCompany());
    }
}
