/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Company;
import com.visfresh.entities.CriticalActionList;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CriticalActionListSerializerTest {
    private Company company;
    private CriticalActionListSerializer serializer;

    /**
     * Default construtor.
     */
    public CriticalActionListSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        company = new Company(7l);
        company.setName("JUnit");

        serializer = new CriticalActionListSerializer(company);
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

        CriticalActionList list = new CriticalActionList();
        list.setCompany(company);
        list.setId(id);
        list.setName(name);
        list.getActions().add(a1);
        list.getActions().add(a2);

        list = serializer.parseCriticalActionList(serializer.toJson(list));

        assertEquals(id, list.getId());
        assertEquals(name, list.getName());
        assertEquals(a1, list.getActions().get(0));
        assertEquals(a2, list.getActions().get(1));
        assertEquals(company.getId(), list.getCompany().getId());
    }
}
