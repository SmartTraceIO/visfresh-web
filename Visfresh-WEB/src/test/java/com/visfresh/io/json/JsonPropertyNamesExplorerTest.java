/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JsonPropertyNamesExplorerTest {

    /**
     * Default constructor.
     */
    public JsonPropertyNamesExplorerTest() {
        super();
    }

    @Test
    public void testExplore() {
        final JsonObject root = createJson("A", "B");
        final JsonObject c1 = createJson("D", "E");
        root.add("C", c1);

        final JsonArray array = new JsonArray();
        final JsonObject c2 = createJson("F", "G");
        array.add(c2);
        c1.add("K", array);

        c2.add("H", createJson("I"));
        c2.addProperty("L", Boolean.TRUE);

        final Set<String> names = new HashSet<>();
        new JsonPropertyNamesExplorer(root).explore(name -> names.add(name));

        //check result
        assertTrue(names.contains("A"));
        assertTrue(names.contains("B"));
        assertTrue(names.contains("C"));
        assertTrue(names.contains("D"));
        assertTrue(names.contains("E"));
        assertTrue(names.contains("F"));
        assertTrue(names.contains("G"));
        assertTrue(names.contains("H"));
        assertTrue(names.contains("I"));
        assertTrue(names.contains("K"));
        assertTrue(names.contains("L"));

        assertEquals(11, names.size());
    }

    /**
     * @param props
     * @return
     */
    private JsonObject createJson(final String... props) {
        final JsonObject json = new JsonObject();
        for (final String name : props) {
            json.addProperty(name, "AnyValue");
        }
        return json;
    }
}
