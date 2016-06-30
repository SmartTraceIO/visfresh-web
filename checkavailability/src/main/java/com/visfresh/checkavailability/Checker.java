/**
 *
 */
package com.visfresh.checkavailability;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.visfresh.cfg.Config;
import com.visfresh.mail.EmailSender;
import com.visfresh.sms.SmsSender;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Checker {
    private static final Logger log = LoggerFactory.getLogger(Checker.class);
    private String subject = "Service Availability checker";
    private String message;

    private final String url;
    private long timeOut = 5 * 60 * 1000l;
    private long waitForResponseTimeOut = 10000; // 10 secs

    /**
     * Default constructor.
     */
    public Checker() {
        super();
        url = Config.getProperty("service.url");
    }

    public void check() {
        log.debug("Checker started");

        if (!checkImpl()) {
            log.debug("First availability check not passed. Will wait for "
                    + getTimeOut() + " ms and check again");
            //do pause and recheck
            try {
                final long t = getTimeOut();
                if (t > 0) {
                    Thread.sleep(t);
                }

                if (!checkImpl()) {
                    log.debug("Second availability check not passed. Alarm will send");
                    sendNotification();
                    log.debug("Not availability alarm has sent");
                }
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.debug("Checker finished");
    }

    /**
     * @return
     */
    private long getTimeOut() {
        return timeOut;
    }
    /**
     * @param timeOut the timeOut to set
     */
    public void setTimeOut(final long timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * Sends service not available notification.
     */
    protected void sendNotification() {
        final String message = getMessage() == null
                ? "Service not available during " + getTimeOut() + " ms"
                : getMessage();
        try {
            new SmsSender().sendSms(subject, message);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
        try {
            new EmailSender().sendMessage(subject, message);
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }

    private boolean checkImpl() {
        final AtomicBoolean isOk = new AtomicBoolean(false);

        synchronized (isOk) {
            final Thread t = new Thread("Async Checker") {
                @Override
                public void run() {
                    try {
                        sendCheckRequest();
                        isOk.set(true);
                    } catch (final Exception e) {
                    }

                    synchronized (isOk) {
                        isOk.notify();
                    }
                };
            };
            t.start();

            try {
                isOk.wait(getWaitForResponseTimeOut());
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }

            if (t.isAlive()) {
                t.interrupt();
            }
        }

        return isOk.get();
    }

    /**
     * @return wait for response time out.
     */
    private long getWaitForResponseTimeOut() {
        return waitForResponseTimeOut;
    }
    /**
     * @param timeOut the waitForResponseTimeOut to set
     */
    public void setWaitForResponseTimeOut(final long timeOut) {
        this.waitForResponseTimeOut = timeOut;
    }
    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }
    /**
     * @param subject the subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     *
     */
    protected void sendCheckRequest() throws IOException {
        log.debug("Sending check request");

        final byte[] buff = new byte[128];

        final InputStream in = new URL(url).openStream();
        //read all from stream.
        try {
            while (in.read(buff) > -1) {
                //nothing
            }
        } finally {
            in.close();
        }

        log.debug("Check request successfully has sent");
    }
}
