/**
 * (c) Drop In Media LLC, All Rights Reserved.
 */

package au.smarttrace.ctrl.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * @author Vyacheslav Soldatov <soldatov@sertolovo.ru>
 * @since 13.02.2009 20:07:28
 */
public class ByteDataSource implements DataSource {
    /**
     * The content type.
     */
    protected String contentType;
    /**
     * The data.
     */
    private final byte[] data;
    /**
     * The source name.
     */
    private String name;

    /**
     * @param bytes the data.
     * @param mimetype MimeType).
     * @param name the data source name.
     */
    public ByteDataSource(final byte[] bytes, final String mimetype, final String name) {
        super();
        this.contentType = mimetype;
        this.data = bytes;
        this.name = name;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public String getContentType() {
        return contentType;
    }

    /**
     * @param type the content type.
     */
    public void setContentType(final String type) {
        this.contentType = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name == null ? "ByteDataSource: " + getContentType() : name;
    }

    /**
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the byte data.
     */
    public byte[] getData() {
        return data;
    }
    /* (non-Javadoc)
     * @see javax.activation.DataSource#getOutputStream()
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }
}
