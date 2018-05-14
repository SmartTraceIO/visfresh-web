/**
 *
 */
package com.visfresh.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.visfresh.controllers.svcimpl.DefaultTimeZoneService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultTimeZoneServiceTest extends DefaultTimeZoneService {
    /**
     * Default constructor.
     */
    public DefaultTimeZoneServiceTest() {
        super();
    }

    @Test
    public void testAllLive() throws SAXException, IOException, ParserConfigurationException {
        //create set of existing time zone ID.
        final Set<String> actualIds = new HashSet<String>();
        for (final String id: TimeZone.getAvailableIDs()) {
            actualIds.add(id);
        }

        for(final String id: loadTimeZoneIds()) {
            if (!actualIds.contains(id)) {
                throw new AssertionFailedError("Time zone with "
                        + id + " ID is not supported in given version of Java");
            }
        }
    }
}
