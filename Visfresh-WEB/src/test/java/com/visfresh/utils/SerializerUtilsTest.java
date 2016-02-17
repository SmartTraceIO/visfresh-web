/**
 *
 */
package com.visfresh.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SerializerUtilsTest {
    /**
     * Default constructor.
     */
    public SerializerUtilsTest() {
        super();
    }

    /**
     * pattern:
     * {
     *     "existingField": 10,
     *     "replaceByPrimitive": {},
     *     "nullPrimitive": 10,
     *     "nullObject": {},
     *     "absentInSourcePrimitive": 7,
     *     "absentInSourceObject": {},
     *     "replaceRecursive": {
     *          "recursive": 9
     *      }
     *  }

     * source:
     * {
     *     "existingField": 11,
     *     "notExistingField": 12,
     *     "replaceByPrimitive": 77,
     *     "nullPrimitive": null,
     *     "nullObject": null,
     *     "replaceRecursive": {
     *          "recursive": 99
     *      }
     *  }
     * @throws IOException
     */
    @Test
    public void testMerge() throws IOException {
        final JsonObject from = loadJson("from.json");
        final JsonObject pattern = loadJson("pattern.json");


        final JsonObject result = SerializerUtils.merge(from, pattern);

        //test existing field in pattern is copied from source
        assertEquals(11, result.get("existingField").getAsInt());

        //test not existing field in pattern is not copied from source.
        assertFalse(result.has("notExistingField"));

        //test JSON object value is replaced by primitive from source
        assertEquals(77, result.get("replaceByPrimitive").getAsInt());

        //test JSON null primitive is copied
        assertEquals(JsonNull.INSTANCE, result.get("nullPrimitive"));

        //test JSON null object is copied.
        assertEquals(JsonNull.INSTANCE, result.get("nullObject"));

        //absent in source primitive not replaces to null
        assertNotNull(result.get("absentInSourcePrimitive"));

        //absent in source object not replaces to null
        assertNotNull(result.get("absentInSourceObject"));

        //test replace recursive
        assertEquals(99, result.get("replaceRecursive").getAsJsonObject().get("recursive").getAsInt());
    }

    /**
     * @param string
     * @return
     * @throws IOException
     */
    private JsonObject loadJson(final String string) throws IOException {
        String text;
        final InputStream in = SerializerUtilsTest.class.getResourceAsStream(string);
        try {
            text = StringUtils.getContent(in, "UTF-8");
        } finally {
            in.close();
        }
        return SerializerUtils.parseJson(text).getAsJsonObject();
    }
}
