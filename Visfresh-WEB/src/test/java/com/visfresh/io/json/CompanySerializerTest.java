/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.visfresh.entities.Company;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompanySerializerTest extends AbstractSerializerTest {
    private CompanySerializer serializer;
    /**
     * Default constructor.
     */
    public CompanySerializerTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        serializer = new CompanySerializer(UTC);
    }
    @Test
    public void testCompany() {
        final String description = "Company Description";
        final Long id = 77l;
        final String name = "CompanyName";

        Company c = new Company();
        c.setDescription(description);
        c.setId(id);
        c.setName(name);

        final JsonElement json = serializer.toJson(c);
        c = serializer.parseCompany(json);

        assertEquals(description, c.getDescription());
        assertEquals(id, c.getId());
        assertEquals(name, c.getName());
    }
}
