/**
 *
 */
package com.visfresh.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
    @Test
    public void testDiffWithNull() {
        final JsonPrimitive primitive = new JsonPrimitive(77);

        final JsonObject obj = new JsonObject();
        obj.add("value", new JsonPrimitive(11));

        final JsonArray array = new JsonArray();
        array.add(obj);

        //test null with null;
        assertNull(SerializerUtils.diff(null, null));

        //test null with primitive
        JsonObject diff = SerializerUtils.diff(null, primitive);
        assertEquals(JsonNull.INSTANCE, diff.get("old"));
        assertEquals(primitive, diff.get("new"));

        diff = SerializerUtils.diff(primitive, null);
        assertEquals(JsonNull.INSTANCE, diff.get("new"));
        assertEquals(primitive, diff.get("old"));

        //test null with object
        diff = SerializerUtils.diff(null, obj);
        assertEquals(JsonNull.INSTANCE, diff.get("old"));
        assertEquals(obj.toString(), diff.get("new").toString());

        diff = SerializerUtils.diff(obj, null);
        assertEquals(JsonNull.INSTANCE, diff.get("new"));
        assertEquals(obj.toString(), diff.get("old").toString());

        //test null with array
        diff = SerializerUtils.diff(null, array);
        assertEquals(JsonNull.INSTANCE, diff.get("old"));
        assertEquals(array.toString(), diff.get("new").toString());

        diff = SerializerUtils.diff(array, null);
        assertEquals(JsonNull.INSTANCE, diff.get("new"));
        assertEquals(array.toString(), diff.get("old").toString());
    }
    @Test
    public void testDiffPrimitive() {
        final JsonPrimitive p1 = new JsonPrimitive(77);
        final JsonPrimitive p2 = new JsonPrimitive(88);

        final JsonObject obj = new JsonObject();
        obj.add("value", new JsonPrimitive(11));

        final JsonArray array = new JsonArray();
        array.add(obj);

        //test equals
        JsonObject diff = SerializerUtils.diff(p1, p1);
        assertNull(diff);

        //test with other primitive
        diff = SerializerUtils.diff(p1, p2);
        assertEquals(p2.toString(), diff.get("new").toString());
        assertEquals(p1.toString(), diff.get("old").toString());

        //test with object
        diff = SerializerUtils.diff(p1, obj);
        assertEquals(obj.toString(), diff.get("new").toString());
        assertEquals(p1.toString(), diff.get("old").toString());

        //test with array
        diff = SerializerUtils.diff(p1, array);
        assertEquals(array.toString(), diff.get("new").toString());
        assertEquals(p1.toString(), diff.get("old").toString());
    }
    @Test
    public void testDiffObject() {
        final JsonObject o1 = new JsonObject();
        o1.addProperty("prop1", 7);
        final JsonObject o2 = new JsonObject();
        o2.addProperty("prop2", 8);

        final JsonObject obj = new JsonObject();
        obj.add("value", new JsonPrimitive(11));

        final JsonArray array = new JsonArray();
        array.add(obj);

        //test equals
        JsonObject diff = SerializerUtils.diff(o1, o1);
        assertNull(diff);

        //test with other object
        diff = SerializerUtils.diff(o1, o2);
        assertEquals(new JsonPrimitive(7), diff.get("prop1").getAsJsonObject().get("old"));
        assertEquals(JsonNull.INSTANCE, diff.get("prop1").getAsJsonObject().get("new"));

        assertEquals(JsonNull.INSTANCE, diff.get("prop2").getAsJsonObject().get("old"));
        assertEquals(new JsonPrimitive(8), diff.get("prop2").getAsJsonObject().get("new"));

        //test with primitive
        final JsonPrimitive primitive = new JsonPrimitive(77);

        diff = SerializerUtils.diff(o1, primitive);
        assertEquals(primitive, diff.get("new"));
        assertEquals(o1.toString(), diff.get("old").toString());

        //test with array
        diff = SerializerUtils.diff(o1, array);
        assertEquals(array.toString(), diff.get("new").toString());
        assertEquals(o1.toString(), diff.get("old").toString());
    }
    @Test
    public void testDiffArray() {
        final JsonArray a1 = new JsonArray();
        a1.add(new JsonPrimitive(5));
        a1.add(new JsonPrimitive(7));
        a1.add(new JsonPrimitive(8));

        final JsonArray a2 = new JsonArray();
        a2.add(new JsonPrimitive(3));
        a2.add(new JsonPrimitive(7));
        a2.add(new JsonPrimitive(6));
        a2.add(new JsonPrimitive(4));

        final JsonObject obj = new JsonObject();
        obj.add("value", new JsonPrimitive(11));

        //test equals
        JsonObject diff = SerializerUtils.diff(a1, a1);
        assertNull(diff);

        //test with other array
        diff = SerializerUtils.diff(a1, a2);
        assertEquals(new JsonPrimitive(5), diff.get("[0]").getAsJsonObject().get("old"));
        assertEquals(new JsonPrimitive(3), diff.get("[0]").getAsJsonObject().get("new"));
        assertNull(diff.get("[1]"));
        assertEquals(new JsonPrimitive(8), diff.get("[2]").getAsJsonObject().get("old"));
        assertEquals(new JsonPrimitive(6), diff.get("[2]").getAsJsonObject().get("new"));
        assertEquals(JsonNull.INSTANCE, diff.get("[3]").getAsJsonObject().get("old"));
        assertEquals(new JsonPrimitive(4), diff.get("[3]").getAsJsonObject().get("new"));

        //test with primitive
        final JsonPrimitive primitive = new JsonPrimitive(77);

        diff = SerializerUtils.diff(a1, primitive);
        assertEquals(primitive, diff.get("new"));
        assertEquals(a1.toString(), diff.get("old").toString());

        //test with object
        diff = SerializerUtils.diff(a1, obj);
        assertEquals(obj.toString(), diff.get("new").toString());
        assertEquals(a1.toString(), diff.get("old").toString());
    }
    @Test
    public void testDiffObjectDeep() {
        final JsonObject obj1 = new JsonObject();
        obj1.addProperty("primitive", 6);

        final JsonObject child1 = new JsonObject();
        child1.addProperty("primitive", 8);
        obj1.add("obj", child1);

        final JsonObject obj2 = new JsonObject();
        obj2.addProperty("primitive", 7);

        final JsonObject child2 = new JsonObject();
        child2.addProperty("primitive", 9);
        obj2.add("obj", child2);

        assertNull(SerializerUtils.diff(obj2, obj2));

        final JsonObject diff = SerializerUtils.diff(obj1, obj2);
        assertEquals(new JsonPrimitive(6), diff.get("primitive").getAsJsonObject().get("old"));
        assertEquals(new JsonPrimitive(7), diff.get("primitive").getAsJsonObject().get("new"));

        final JsonObject childDiff = diff.get("obj").getAsJsonObject();
        assertEquals(new JsonPrimitive(8), childDiff.get("primitive").getAsJsonObject().get("old"));
        assertEquals(new JsonPrimitive(9), childDiff.get("primitive").getAsJsonObject().get("new"));
    }
    @Test
    public void testDiffArrayDeep() {

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
