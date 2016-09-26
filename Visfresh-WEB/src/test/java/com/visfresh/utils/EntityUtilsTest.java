/**
 *
 */
package com.visfresh.utils;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EntityUtilsTest {

    /**
     * Default constructor.
     */
    public EntityUtilsTest() {
        super();
    }

    @Test
    public void testCompare() {
        //compare empty
        final List<Integer> origin = new LinkedList<>();
        final List<Integer> modified = new LinkedList<>();

        CompareResult<Integer> result = EntityUtils.compare(origin, modified);

        assertEquals(0, result.getAdded().size());
        assertEquals(0, result.getDeleted().size());

        //compare not empty
        origin.add(0);
        origin.add(1);
        origin.add(2);

        modified.add(1);
        modified.add(2);
        modified.add(3);

        result = EntityUtils.compare(origin, modified);

        assertEquals(1, result.getAdded().size());
        assertEquals(1, result.getDeleted().size());

        assertEquals(origin.get(0), result.getDeleted().get(0));
        assertEquals(modified.get(2), result.getAdded().get(0));
    }
}
