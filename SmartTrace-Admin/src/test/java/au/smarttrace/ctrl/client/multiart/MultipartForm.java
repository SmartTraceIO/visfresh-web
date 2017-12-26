package au.smarttrace.ctrl.client.multiart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 */
public class MultipartForm {
    final MimeMultipart root = new MimeMultipart();

    /**
     * The constructor.
     */
    public MultipartForm() {
        super();
    }

    public String getContentType() {
        return root.getContentType();
    }
    public byte[] toBytes() throws IOException, MessagingException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        root.writeTo(bout);
        return bout.toByteArray();
    }
    /**
     * @param name form field name.
     * @param value form value.
     * @throws MessagingException
     * @throws IOException
     */
    public void addFormField(final String name, final String value) throws MessagingException, IOException {
        final MimeBodyPart mbp = new MimeBodyPart();
        mbp.setDataHandler(new DataHandler(new ByteDataSource(value.getBytes(), "text/plain", name)));
        mbp.setHeader("Content-Type", "text/plain");
        mbp.addHeader("Content-Disposition", "form-data; name=\"" + name + "\"");
        root.addBodyPart(mbp);
    }
    /**
     * @param paramName field name.
     * @param attachment attachement.
     * @param contentType attachment content type.
     * @throws MessagingException
     * @throws IOException
     */
    public void addAttachment(final String paramName, final String fileName,  final byte[] attachment, final String contentType) throws MessagingException, IOException {
        final MimeBodyPart mbp = new MimeBodyPart();
        mbp.setDataHandler(new DataHandler(new ByteDataSource(attachment, contentType, paramName)));
        mbp.setHeader("Content-Type", contentType);
        mbp.addHeader("Content-Disposition", "form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"");
        root.addBodyPart(mbp);
    }
}
