/**
 *
 */
package com.visfresh.checkavailability;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import com.visfresh.cfg.Config;
import com.visfresh.mail.EmailSender;
import com.visfresh.sms.SmsSender;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Checker {
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
        if (!checkImpl()) {
            //do pause and recheck
            try {
                final long t = getTimeOut();
                if (t > 0) {
                    Thread.sleep(t);
                }

                if (!checkImpl()) {
                    sendNotification();
                }
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        final String subject = "Service Availability checker";
        final String message = "Service not available during " + getTimeOut() + " ms";

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
     *
     */
    protected void sendCheckRequest() throws IOException {
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
    }
}
