/**
 *
 */
package au.smarttrace.json;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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
        m.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return m;
    }
}
