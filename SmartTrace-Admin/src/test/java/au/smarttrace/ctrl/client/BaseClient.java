/**
 *
 */
package au.smarttrace.ctrl.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.ContentType;

import com.fasterxml.jackson.databind.ObjectMapper;

import au.smarttrace.ctrl.JsonConverter;
import au.smarttrace.ctrl.ServiceResponse;
import au.smarttrace.ctrl.client.multiart.MultipartForm;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BaseClient  {
    private String serviceUrl;
    protected String accessToken;

    private final List<IoListener> listeners = new LinkedList<IoListener>();
    protected ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Default constructor.
     */
    public BaseClient() {
        super();
        objectMapper.setDateFormat(new SimpleDateFormat(JsonConverter.DATE_FORMAT));
        addIoListener(new IoLogger());
    }

    /**
     * @param path URL path.
     * @param params request parameters.
     * @return JSON response.
     * @throws IOException
     * @throws ServiceException
     */
    public final <T> T sendGetRequest(final String path, final Map<String, String> params,
            final Class<? extends ServiceResponse<T>> responseClass)
            throws IOException, ServiceException {
        final byte[] response = doSendGetRequest(path, params);
        return parseResponse(response, responseClass);
    }
    /**
     * @param path path.
     * @param request JSON request body.
     * @return JSON response.
     * @throws ServiceException
     */
    public final <T> T sendPostRequest(final String path, final Object request,
            final Class<? extends ServiceResponse<T>> responseClass)
            throws IOException, ServiceException {
        final byte[] response = doSendPostRequest(path, request);
        return parseResponse(response, responseClass);
    }
    /**
     * @param path
     * @param params
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    public byte[] doSendGetRequest(final String path,
            final Map<String, String> params) throws IOException,
            MalformedURLException {
        final StringBuilder sb = new StringBuilder(getServiceUrl() + path);

        if (params.size() > 0) {
            sb.append('?');

            boolean first = true;
            for (final Map.Entry<String, String> e : params.entrySet()) {
                if (!first) {
                    sb.append('&');
                } else {
                    first = false;
                }

                sb.append(urlEncode(e.getKey()));
                sb.append('=');
                sb.append(urlEncode(e.getValue()));
            }
        }

        final String url = sb.toString();
        fireRequestSending(url, "GET", null);

        final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setDoOutput(false);
        con.setDoInput(true);
        //add cors
        con.setRequestProperty("origin", "null");

        final byte[] response = getContent(con.getInputStream());
        //notify listeners
        fireResponseReceived(new String(response, "UTF-8"));
        return response;
    }
    /**
     * @param path
     * @param req
     * @return
     * @throws IOException
     * @throws MessagingException
     * @throws MalformedURLException
     */
    public byte[] doSendForm(final String path, final MultipartForm req)
            throws IOException, MessagingException {
        final StringBuilder sb = new StringBuilder(getServiceUrl() + path);

        final String url = sb.toString();
        final URLConnection con = new URL(url).openConnection();

        final ContentType ct = new ContentType(req.getContentType());
        final String contentType = "multipart/form-data; boundary=\"" + ct.getParameter("boundary") + "\"";
        con.setRequestProperty("Content-Type", contentType);

        con.setDoOutput(true);
        con.setDoInput(true);
        //add cors
        con.setRequestProperty("origin", "null");

        final OutputStream out = con.getOutputStream();
        try {
            final byte[] bytes = req.toBytes();

            //notify listeners
            fireRequestSending(url, "POST", new String(bytes, "UTF-8"));

            out.write('\r');
            out.write('\n');
            out.write(bytes);
            out.flush();
        } finally {
            out.close();
        }

        final byte[] response = getContent(con.getInputStream());
        //notify listeners
        fireResponseReceived(new String(response, "UTF-8"));
        return response;
    }
    /**
     * @param path
     * @param req
     * @return
     * @throws IOException
     * @throws MalformedURLException
     */
    public byte[] doSendPostRequest(final String path, final Object req)
            throws IOException, MalformedURLException {
        final StringBuilder sb = new StringBuilder(getServiceUrl() + path);

        final String url = sb.toString();
        final URLConnection con = new URL(url).openConnection();
//        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Content-Type", "text/plain");
        con.setDoOutput(true);
        con.setDoInput(true);
        //add cors
        con.setRequestProperty("origin", "null");

        final Writer wr = new OutputStreamWriter(con.getOutputStream());
        try {
            final String requestBody = objectMapper.writeValueAsString(req);
            //notify listeners
            fireRequestSending(url, "POST", requestBody);
            wr.write(requestBody);
            wr.flush();
        } finally {
            wr.close();
        }

        final byte[] response = getContent(con.getInputStream());
        //notify listeners
        fireResponseReceived(new String(response, "UTF-8"));
        return response;
    }
    /**
     * @return
     */
    public String getServiceUrl() {
        return serviceUrl;
    }
    /**
     * @param serviceUrl the serviceUrl to set
     */
    public final void setServiceUrl(final String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
    /**
     * @param con
     * @return
     * @throws IOException
     * @throws ServiceException
     */
    protected <T> T parseResponse(final byte[] response, final Class<? extends ServiceResponse<T>> targetClass)
            throws IOException, ServiceException {
        final ServiceResponse<T> e = objectMapper.readValue(response, targetClass);
        checkError(e);
        return e.getResponseObject();
    }
    /**
     * @param response JSON response.
     * @throws ServiceException
     */
    protected void checkError(final ServiceResponse<?> response) throws ServiceException {
        if (response.getStatus().getCode() != 200) {
            throw new ServiceException(response.getStatus().getCode(),
                    response.getStatus().getMessage());
        }
    }

    /**
     * @param in input stream.
     * @return content of stream as string.
     */
    private byte[] getContent(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        int len;
        final byte[] buff = new byte[512];
        while ((len = in.read(buff)) > -1) {
            out.write(buff, 0, len);
        }

        return out.toByteArray();
    }

    /**
     * @param str string to encode.
     * @return URL encoded string.
     */
    protected String urlEncode(final String str) throws IOException {
        return URLEncoder.encode(str, "UTF-8");
    }
    /**
     * @param methodPath
     * @return
     * @throws ServiceException
     */
    public String getPathWithToken(final String methodPath) {
        if (getAccessToken() == null) {
            return methodPath;
        }
        return getAccessToken() + "/" + methodPath;
    }
    /**
     * @return
     */
    public final String getAccessToken() {
        return accessToken;
    }
    /**
     * @param authToken the authToken to set
     */
    public final void setAccessToken(final String authToken) {
        this.accessToken = authToken;
    }
    public void addIoListener(final IoListener l) {
        listeners.add(l);
    }
    public void removeIoListener(final IoListener l) {
        listeners.remove(l);
    }
    /**
     * @param response
     */
    protected void fireResponseReceived(final String response) {
        for (final IoListener l : listeners) {
            l.receivedResponse(response);
        }
    }
    /**
     * @param url URL string.
     * @param method method name.
     * @param requestBody request body in case of POST request.
     */
    protected void fireRequestSending(final String url, final String method, final String requestBody) {
        for (final IoListener l : listeners) {
            l.sendingRequest(url, requestBody, method);
        }
    }
}
