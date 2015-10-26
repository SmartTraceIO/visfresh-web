/**
 *
 */
package com.visfresh;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TestTool {

    /**
     *
     */
    public TestTool() {
        // TODO Auto-generated constructor stub
    }

    public static void main(final String[] args) throws MalformedURLException, IOException {
        final URLConnection con = new URL("http://localhost:8080/web/vf/rest/login?login=l&password=p").openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.addRequestProperty("origin", "dubna");

        final Map<String, List<String>> headers = con.getHeaderFields();
        for (final Map.Entry<String, List<String>> h : headers.entrySet()) {
            System.out.println(h.getKey() + ": " + StringUtils.combine(h.getValue(), ";"));
        }

        final InputStream in = con.getInputStream();
        try {
            System.out.println(getAsString(in));
        } finally {
            in.close();
        }
    }

    /**
     * @param in
     * @return
     * @throws IOException
     */
    private static String getAsString(final InputStream in) throws IOException {
        final Reader r = new InputStreamReader(in);
        final char[] buff = new char[256];
        int len;
        final Writer wr = new StringWriter();
        while ((len = r.read(buff)) > -1) {
            wr.write(buff, 0, len);
        }

        return wr.toString();
    }
}
