/**
 *
 */
package com.visfresh.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TmpSaveMessageServiceTest extends TmpSaveMessageService {
    private Path storage;

    /**
     * Default constructor
     */
    public TmpSaveMessageServiceTest() {
        super();
    }

    @Before
    public void setUp() throws IOException {
        storage = Files.createTempDirectory("junit_");
        setStorage(storage);
    }

    @Test
    public void testDeviceMessageSerialize() {
        final DeviceMessageType type = DeviceMessageType.BAT0;
        final Date time = new Date(System.currentTimeMillis() - 1000000000l);
        final int battery = 15;
        final String imei = "23905847098279";
        final String msg = "message as string";
        final double temperature = 36.6;

        DeviceMessage m = new DeviceMessage();
        m.setBattery(battery);
        m.setImei(imei);
        m.setMessage(msg);
        m.setTemperature(temperature);
        m.setTime(time);
        m.setType(type);
        m.setTypeString(type.toString());

        m = parseDeviceMessage(serialize(m));

        assertEquals(battery, m.getBattery());
        assertEquals(imei, m.getImei());
        assertEquals(msg, m.getMessage());
        assertTrue(Math.abs(temperature - m.getTemperature()) < 0.001);
        assertTrue(Math.abs(time.getTime() - m.getTime().getTime()) < 1001);
        assertEquals(type, m.getType());
        assertEquals(type.toString(), m.getTypeString());
    }
    @Test
    public void testSaveMessage() throws IOException {
        saveMessage(createMessage(1));
        saveMessage(createMessage(2));
        saveMessage(createMessage(3));

        assertEquals(3, Files.list(storage).count());
    }
    @Test
    public void testRemoveAndGetMessage() throws IOException {
        saveMessage(createMessage(1));
        saveMessage(createMessage(2));
        saveMessage(createMessage(3));

        assertEquals(1, removeAndGetMessage().getBattery());
        assertEquals(2, removeAndGetMessage().getBattery());
        assertEquals(3, removeAndGetMessage().getBattery());

        assertEquals(0, Files.list(storage).count());
    }

    /**
     * @param the battery. It is usefully to use battery as ID analog.
     * @return the message.
     */
    protected DeviceMessage createMessage(final int battery) {
        final DeviceMessage m = new DeviceMessage();
        m.setBattery(battery);
        m.setImei("23905847098279");
        m.setMessage("message as string");
        m.setTemperature(36.6);
        m.setTime(new Date(System.currentTimeMillis() - 1000000000l));
        m.setType(DeviceMessageType.BAT0);
        m.setTypeString(DeviceMessageType.BAT0.toString());
        return m;
    }


    @After
    public void tearDown() throws IOException {
        //cleanup temporary folder
        Files.walkFileTree(storage, new FileVisitor<Path>() {
            /* (non-Javadoc)
             * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
             */
            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                deleteFile(dir);
                return FileVisitResult.CONTINUE;
            }
            /* (non-Javadoc)
             * @see java.nio.file.FileVisitor#preVisitDirectory(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
             */
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }
            /* (non-Javadoc)
             * @see java.nio.file.FileVisitor#visitFile(java.lang.Object, java.nio.file.attribute.BasicFileAttributes)
             */
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                deleteFile(file);
                return FileVisitResult.CONTINUE;
            }
            /* (non-Javadoc)
             * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
             */
            @Override
            public FileVisitResult visitFileFailed(final Path file, final IOException exc) throws IOException {
                deleteFile(file);
                return FileVisitResult.CONTINUE;
            }
            private void deleteFile(final Path file) throws IOException {
                Files.delete(file);
            }
        });
    }
}
