/**
 *
 */
package com.visfresh.checkavailability;

import java.util.concurrent.atomic.AtomicBoolean;

import com.visfresh.cfg.Config;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Checker {
    private final String url;
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
                Thread.sleep(5 * 60 * 1000l);

            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkImpl() {
        final AtomicBoolean isOk = new AtomicBoolean(false);

        synchronized (isOk) {
            final Thread t = new Thread("Async Checker") {
                @Override
                public void run() {
                    synchronized (isOk) {
                        try {
                            sendCheckRequest();
                            isOk.set(true);
                        } catch (final Exception e) {
                        }
                        isOk.notify();
                    }
                };
            };
            t.start();

            try {
                isOk.wait(3000);
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
     *
     */
    protected void sendCheckRequest() {
        // TODO Auto-generated method stub

    }
}
