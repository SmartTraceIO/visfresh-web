/**
 *
 */
package au.smarttrace.json;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ObjectMapperFactory {
    /**
     * @return JSON object mapper.
     */
    public static ObjectMapper craeteObjectMapper() {
        final ObjectMapper m = new ObjectMapper();
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        m.setLocale(Locale.US);
        return m;
    }
}
