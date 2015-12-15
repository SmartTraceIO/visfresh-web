/**
 *
 */
package com.visfresh.services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultTimeZoneService implements TimeZoneService {
    private static final String FILE = "timezones.xml";
    private static final Logger log = LoggerFactory.getLogger(DefaultTimeZoneService.class);

    private SoftReference<List<TimeZone>> timeZones;
    /**
     * Default constructor.
     */
    public DefaultTimeZoneService() {
        super();
    }

    @PostConstruct
    public void initialize() {

    }
    /* (non-Javadoc)
     * @see com.visfresh.services.TimeZoneService#getSupportedTimeZones()
     */
    @Override
    public List<TimeZone> getSupportedTimeZones() {
        synchronized (this) {
            final SoftReference<List<TimeZone>> tzs = timeZones;
            List<TimeZone> zones = tzs == null ? null : tzs.get();

            if (zones == null) {
                zones = loadTimeZones();
                timeZones = new SoftReference<List<TimeZone>>(zones);
            }

            return zones;
        }
    }

    /**
     * @return
     */
    private List<TimeZone> loadTimeZones() {
        Set<String> ids;
        try {
            ids = loadTimeZoneIds();
        } catch (final Exception e) {
            log.error("Failed to load time zone IDs");
            throw new RuntimeException(e);
        }

        //add UTC if need
        if (!ids.contains("UTC")) {
            ids.add("UTC");
        }

        final List<TimeZone> list = new LinkedList<>();
        for (final String id : ids) {
            list.add(TimeZone.getTimeZone(id));
        }
        return list;
    }

    /**
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    protected Set<String> loadTimeZoneIds() throws SAXException, IOException,
            ParserConfigurationException {
        //create document builder factory.
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setCoalescing(false);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);

        //load XML
        Document doc;
        final Reader r = new InputStreamReader(DefaultTimeZoneService.class.getResourceAsStream(FILE));
        try {
            final InputSource is = new InputSource(r);
            doc = dbf.newDocumentBuilder().parse(is);
        } finally {
            r.close();
        }

        final NodeList els = doc.getElementsByTagName("tz");

        final Set<String> ids = new HashSet<>();
        final int len = els.getLength();
        for (int i = 0; i < len; i++) {
            final Element e = (Element) els.item(i);
            ids.add(e.getAttribute("id"));
        }
        return ids;
    }
}
