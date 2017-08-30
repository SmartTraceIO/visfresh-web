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
    private static char[] createFullAlphabet() {
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
        if (!aliases.contains(name) && !"version".equals(name)) {
            final String alias = generateNextKey(aliases.size());
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
        final StringBuilder sb = new StringBuilder();
        final char[] fullAlphabet = createFullAlphabet();
        toString(counter, sb, fullAlphabet);
        return sb.toString();
    }
    /**
     * @param cipher
     * @param sb
     * @param ab
     */
    protected void toString(final int cipher, final StringBuilder sb, final char[] ab) {
        if (cipher == 0) {
            sb.append(ab[0]);
        } else {
            final int len = ab.length;

            int c = cipher;
            while (c > 0) {
                sb.insert(0, ab[c % len]);
                c /= len;
            }
        }
    }
    public void clear() {
        aliases.clear();
    }
}
