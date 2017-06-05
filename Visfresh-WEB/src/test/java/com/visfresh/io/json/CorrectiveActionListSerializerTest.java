/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.CorrectiveActionList;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CorrectiveActionListSerializerTest {
    private Company company;
    private CorrectiveActionListSerializer serializer;

    /**
     * Default construtor.
     */
    public CorrectiveActionListSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        company = new Company(7l);
        company.setName("JUnit");

        serializer = new CorrectiveActionListSerializer(company);
    }

    /**
     * Tests list serialization.
     */
    @Test
    public void testSerialize() {
        final Long id = 77l;
        final String name = "JUnit Action list";
        final String a1 = "First Action";
        final String a2 = "Second Action";

        CorrectiveActionList list = new CorrectiveActionList();
        list.setCompany(company);
        list.setId(id);
        list.setName(name);
        list.getActions().add(new CorrectiveAction(a1, true));
        list.getActions().add(new CorrectiveAction(a2, false));

        list = serializer.parseCorrectiveActionList(serializer.toJson(list));

        assertEquals(id, list.getId());
        assertEquals(name, list.getName());
        assertEquals(a1, list.getActions().get(0).getAction());
        assertTrue(list.getActions().get(0).isRequestVerification());
        assertEquals(a2, list.getActions().get(1).getAction());
        assertFalse(list.getActions().get(1).isRequestVerification());
        assertEquals(company.getId(), list.getCompany().getId());
    }
}
