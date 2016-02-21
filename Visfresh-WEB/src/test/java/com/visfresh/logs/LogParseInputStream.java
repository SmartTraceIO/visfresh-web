package com.visfresh.logs;
import java.io.InputStream;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LogParseInputStream extends AbstractBoundaryInputStream {
    private static final String SAMPLE = "2016-02-17 06:27:41,276 WARN  [DeviceCommunicationServlet]";
    private static final int DATE_LENGTH = "2016-02-17 06:27:41,276".length();
    private int prefixOffset = -1;
    private int prefixLength = -1;

    /**
     * @param originStream the origin input stream.
     */
    public LogParseInputStream(final InputStream originStream) {
        super(originStream, 2 * SAMPLE.length());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean searchBounds() {
        final int len = buffer.length - DATE_LENGTH;
        for (int i = 0; i < len; i++) {
            final int plen = getPrifixLength(i);
            if (plen > 0) {
                this.prefixOffset = i;
                this.prefixLength = plen;
                return true;
            }
        }
        return false;
    }
    /**
     * @return
     */
    private int getPrifixLength(final int offset) {
//      2016-02-17 06:27:41,276 WARN  [DeviceCommunicationServlet]
        if (isCorrectDate(offset)) {
            final int nameOffset = indexOf(offset + DATE_LENGTH, '[');
            if (nameOffset > 0) {
                final int nameEnd = indexOf(nameOffset, ']');
                if (isJavaIdentifier(nameOffset + 1, nameEnd - 1)) {
                    return nameEnd - offset + 1;
                }
            }
        }

        return -1;
    }

    /**
     * @param offset substring offset.
     * @param end substring end.
     * @return true of the substring is java identifier.
     */
    private boolean isJavaIdentifier(final int offset, final int end) {
        for (int i = offset; i <= end; i++) {
            if (!Character.isJavaIdentifierPart(buffer[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param offset
     * @param c
     * @return
     */
    private int indexOf(final int offset, final char c) {
        int index = offset;
        while (index < buffer.length) {
            if (buffer[index] == c) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * @param offset
     * @return
     */
    private boolean isCorrectDate(final int offset) {
        return
            buffer.length - offset > DATE_LENGTH
            //2016-02-17 06:27:41,276
            && isCipher(buffer[offset + 0])//2
            && isCipher(buffer[offset + 1])//0
            && isCipher(buffer[offset + 2])//1
            && isCipher(buffer[offset + 3])//6
            && buffer[offset + 4] == '-'//-
            && isCipher(buffer[offset + 5])//0
            && isCipher(buffer[offset + 6])//2
            && buffer[offset + 7] == '-'//-
            && isCipher(buffer[offset + 8])//1
            && isCipher(buffer[offset + 9])//7
            && buffer[offset + 10] == ' '//:
            && isCipher(buffer[offset + 11])//0
            && isCipher(buffer[offset + 12])//6
            && buffer[offset + 13] == ':'//:
            && isCipher(buffer[offset + 14])//2
            && isCipher(buffer[offset + 15])//7
            && buffer[offset + 16] == ':'//:
            && isCipher(buffer[offset + 17])//4
            && isCipher(buffer[offset + 18])//1
            && buffer[offset + 19] == ','//,
            && isCipher(buffer[offset + 20])//2
            && isCipher(buffer[offset + 21])//7
            && isCipher(buffer[offset + 22])//6
            ;
    }
    /**
     * @param b byte.
     * @return true if cipher.
     */
    private boolean isCipher(final byte b) {
        final int i = b - (byte)'0';
        return i >= 0 && i <= 9;
    }

    /**
     * @return the bound string.
     */
    public String getBound() {
        if (prefixOffset > -1 && prefixLength > -1) {
            return new String(buffer, prefixOffset, prefixLength);
        }
        return null;
    }
}
