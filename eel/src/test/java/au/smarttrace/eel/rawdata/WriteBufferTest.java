/**
 *
 */
package au.smarttrace.eel.rawdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class WriteBufferTest {
    /**
     * Default constructor,
     */
    public WriteBufferTest() {
        super();
    }

    @Test
    public void testWriteOne() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final int one = 254;
        wr.writeOne(one);

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertEquals(one, r.readOne());
        assertFalse(r.hasData());
    }
    @Test
    public void testWriteTwo() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final int two = 2987;
        wr.writeTwo(two);

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertEquals(two, r.readTwo());
        assertFalse(r.hasData());
    }
    @Test
    public void testWriteFour() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final int four = 878745;
        wr.writeFour(four);

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertEquals(four, r.readFour());
        assertFalse(r.hasData());
    }
    @Test
    public void testWriteImei() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final String imei = "3947098";
        wr.writeImei(imei);

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertEquals(imei, r.readImei());
        assertFalse(r.hasData());
    }
    @Test
    public void testWriteHexString() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final String hex = "23a94b70c987d987";
        wr.writeHexString(hex, 50);

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertTrue(r.readHexString(50).endsWith(hex));
        assertFalse(r.hasData());
    }
    @Test
    public void testWriteString() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final String str = " 23a9 4b70c 987d987";
        wr.writeString(str, 50);

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertTrue(r.readString(50).endsWith(str));
        assertFalse(r.hasData());
    }
    @Test
    public void testWriteBytes() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final String str = " 23a9 4b70c 987d987";
        wr.writeBytes(str.getBytes(), 50);

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertEquals(str, new String(r.readBytes(50), 50 - str.length(), str.length()));
        assertFalse(r.hasData());
    }
    @Test
    public void testWriteMacAddress() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final String max = "00:26:57:00:1f:02";
        wr.writeMacAddress(max);

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertEquals(max, r.readMacAddress());
        assertFalse(r.hasData());
    }
    @Test
    public void testWriteAllAsString() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final String str = " 23a9 4b70c 987d987";
        wr.writeAllAsString(str);

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertEquals(str, r.readAllAsString());
        assertFalse(r.hasData());
    }
    @Test
    public void testWriteAllAsBytes() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final WriteBuffer wr = new WriteBuffer(out);

        final String str = " 23a9 4b70c 987d987";
        wr.writeAllAsBytes(str.getBytes());

        final ReadBuffer r = new ReadBuffer(out.toByteArray());
        assertEquals(str, new String(r.readAllAsBytes()));
        assertFalse(r.hasData());
    }
}
