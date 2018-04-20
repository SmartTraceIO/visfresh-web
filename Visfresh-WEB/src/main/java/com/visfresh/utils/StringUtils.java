/**
 *
 */
package com.visfresh.utils;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class StringUtils {
    /**
     * Default constructor.
     */
    private StringUtils() {
        super();
    }

    /**
     * @param objects
     * @param delim
     * @return
     */
    public static <M> String combine(final Iterable<M> objects, final String delim) {
        final StringBuilder sb = new StringBuilder();
        for (final M i : objects) {
            if (sb.length() > 0) {
                sb.append(delim);
            }
            sb.append(String.valueOf(i));
        }
        return sb.toString();
    }
    /**
     * @param objects
     * @param delim
     * @return
     */
    public static <S> String combine(final S[] objects, final String delim) {
        final StringBuilder sb= new StringBuilder();
        for (final S s : objects) {
            if (sb.length() > 0) {
                sb.append(delim);
            }
            sb.append(s);
        }
        return sb.toString();
    }
    /**
     * @param template template string.
     * @param replacements map of replacements.
     * @return
     */
    public static String getMessage(final String template, final Map<String, String> replacements) {
        if (template == null) {
            return null;
        }

        String result = template;
        for (final Map.Entry<String, String> e : replacements.entrySet()) {
            final String v = e.getValue() != null ? e.getValue() : "";
            result = result.replaceAll(Pattern.quote("${" + e.getKey() + "}"), v);
        }
        return result;
    }
    /**
     * @param in input string.
     * @param encoding character encoding.
     * @return stream content as string.
     * @throws IOException
     */
    public static String getContent(final InputStream in, final String encoding) throws IOException {
        final Reader r = new InputStreamReader(in, encoding);
        return getContent(r);
    }
    /**
     * @param in input.
     * @param encoding character encoding.
     * @return stream content as string.
     * @throws IOException
     */
    public static String getContent(final URL in, final String encoding) throws IOException {
        final Reader r = new InputStreamReader(in.openStream(), encoding);
        try {
            return getContent(r);
        } finally {
            r.close();
        }
    }
    /**
     * @param r reader.
     * @return stream content as string.
     * @throws IOException
     */
    public static String getContent(final Reader r) throws IOException {
        final char[] buff = new char[128];
        final StringWriter sw = new StringWriter();

        int len;
        while ((len = r.read(buff)) > -1) {
            sw.write(buff, 0, len);
        }

        return sw.getBuffer().toString();
    }

    /**
     * @param color java color.
     * @return HTML color string.
     */
    public static String toHtmlColor(final Color color) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(color.getRGB() & 0xffffff));
        while (hex.length() < 6) {
            hex = hex.insert(0, '0');
        }
        hex.insert(0, '#');
        return hex.toString();
    }

    /**
     * @param firstName
     * @param lastName
     * @return
     */
    public static String createShortenedFullUserName(final String firstName, final String lastName) {
        final StringBuilder sb = new StringBuilder();
        if (firstName != null) {
            sb.append(firstName);
        }
        if (lastName != null && lastName.length() > 0) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(lastName.charAt(0));
        }
        return sb.toString();
    }
    /**
     * @param firstName
     * @param lastName
     * @return
     */
    public static String createFullUserName(final String firstName, final String lastName) {
        final StringBuilder sb = new StringBuilder();
        if (firstName != null) {
            sb.append(firstName);
        }
        if (lastName != null && lastName.length() > 0) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(lastName);
        }
        return sb.toString();
    }

    /**
     * @param name resource name without extension and path.
     * @return
     */
    public static String loadSql(final String name) {
        try {
            return getContent(
                    StringUtils.class.getClassLoader().getResource("sql/" + name + ".sql"), "UTF-8");
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load sql resource " + name, e);
        }
    }
}
