/**
 *
 */
package com.visfresh.io.json;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonObject;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShortenerAliasesBuilder implements JsonPropertyNameHandler {
    @FunctionalInterface
    public static interface ShortenerAliaseCollector {
        void aliasCreated(String origin, String alias);
    }

    private final Set<String> aliases = new HashSet<>();
    private final char[] alphabet = createAlphabet();
    private ShortenerAliaseCollector collector;

    /**
     * Default constructor.
     */
    public ShortenerAliasesBuilder() {
        super();
    }

    /**
     * @param collector the collector to set
     */
    public void setCollector(final ShortenerAliaseCollector collector) {
        this.collector = collector;
    }
    /**
     * @return alphabet.
     */
    private static char[] createAlphabet() {
        //create array
        final char[] array = new char[('9' - '0') + ('z' - 'a') + ('Z' - 'A') + 3];

        //fill array
        int offset = 0;
        int len = ('9' - '0') + 1;
        fill(array, '0', len, offset);

        offset += len;
        len = ('z' - 'a') + 1;
        fill(array, 'a', len, offset);

        offset += len;
        len = ('Z' - 'A') + 1;
        fill(array, 'A', len, offset);

        return array;
    }

    /**
     * @param array
     * @param start
     * @param len
     * @param offset
     * @return
     */
    private static void fill(final char[] array, final char start, final int len, final int offset) {
        for (int i = 0; i < len; i++) {
            array[offset + i] = (char) (start + i);
        }
    }

    public void addProperiesFromJson(final JsonObject json) {
        new JsonPropertyNamesExplorer(json).explore(this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.io.json.JsonPropertyNameHandler#handlePropertyName(java.lang.String)
     */
    @Override
    public void handlePropertyName(final String name) {
        if (!aliases.contains(name)) {
            final String alias = "a" + generateNextKey(aliases.size());
            aliases.add(name);
            if (this.collector != null) {
                collector.aliasCreated(name, alias);
            }
        }
    }

    /**
     * @return
     */
    private String generateNextKey(final int counter) {
        final int len = alphabet.length;

        final StringBuilder sb = new StringBuilder();
        int c = counter;
        while (c > 0) {
            sb.append(alphabet[c % len]);
            c /= len;
        }

        return sb.toString();
    }
    public void clear() {
        aliases.clear();
    }
}