/**
 *
 */
package au.smarttrace.ctrl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JsonConverter extends MappingJackson2HttpMessageConverter {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    public JsonConverter() {
        super();
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));

        //add support of text/plain meda type.
        final List<MediaType> media = new LinkedList<>(getSupportedMediaTypes());
        media.add(MediaType.TEXT_PLAIN);
        setSupportedMediaTypes(media);
    }

    /* (non-Javadoc)
     * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#writeInternal(java.lang.Object, java.lang.reflect.Type, org.springframework.http.HttpOutputMessage)
     */
    @Override
    protected void writeInternal(final Object object, final Type type, final HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        final ServiceResponse<Object> resp = new ServiceResponse<Object>();
        resp.setStatus(new Status(HttpServletResponse.SC_OK, "OK"));
        resp.setResponseObject(object);
        super.writeInternal(resp, type, outputMessage);
    }
    /* (non-Javadoc)
     * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#readInternal(java.lang.Class, org.springframework.http.HttpInputMessage)
     */
    @Override
    protected Object readInternal(final Class<?> clazz, final HttpInputMessage inputMessage)
            throws IOException, HttpMessageNotReadableException {
        return super.readInternal(clazz, inputMessage);
    }
}
