/**
 *
 */
package com.visfresh.impl.services;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AsyncEventHelper<E> {
    private final List<E> events = new LinkedList<>();
    private volatile boolean isDestroyed = false;
    private Thread thread;
    private long timeOut = 30000l;

    /**
     * Default constructor.
     */
    public AsyncEventHelper() {
        super();
    }

    protected void addToHandle(final E e) throws IllegalStateException {
        synchronized (events) {
            if (isDestroyed) {
                throw new IllegalStateException("Helper is destroyed.");
            }
            events.add(e);
            events.notify();
            ensureThreadStarted();
        }
    }

    /**
     * Warning! Should be run in synchronized context.
     */
    private void ensureThreadStarted() {
        if (thread == null) {
            thread = new Thread(() -> processEvents(), "AsyncHandler-" + getClass().getName());
            thread.start();
        }
    }

    public void destroy() {
        while (true) {
            //wait for process all events.
            synchronized(events) {
                isDestroyed = true;
                if (events.size() == 0 && thread == null) {
                    return;
                }
            }
        }
    }

    private void processEvents() {
        while (true) {
            List<E> toProcess;
            synchronized(events) {
                if (events.size() == 0 && !isDestroyed) {
                    try {
                        events.wait(getTimeOut());
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                toProcess = new LinkedList<>(events);
                events.clear();
                //stop given thread if queue is empty
                if (toProcess.size() == 0) {
                    thread = null;
                    return;
                }
            }

            //process events
            final Iterator<E> iter = toProcess.iterator();
            while (iter.hasNext()) {
                processEvent(iter.next());
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
     * @param e
     */
    protected abstract void processEvent(E e);

    public static void main(final String[] args) {
        final AsyncEventHelper<String> helper = new AsyncEventHelper<String>() {
            /* (non-Javadoc)
             * @see com.visfresh.impl.services.AsyncEventHelper#processEvent(java.lang.Object)
             */
            @Override
            protected void processEvent(final String e) {
                System.out.println("Event " + e + " has processed");
                try {
                    Thread.sleep(10);
                } catch (final InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            /* (non-Javadoc)
             * @see com.visfresh.impl.services.AsyncEventHelper#destroy()
             */
            @Override
            public void destroy() {
                System.out.println("Pre destroy");
                super.destroy();
                System.out.println("Post destroy");
            }
        };

        for (int i = 0; i < 3; i++) {
            final Thread t = new Thread("T" + i) {
                /* (non-Javadoc)
                 * @see java.lang.Thread#run()
                 */
                @Override
                public void run() {
                    for (int j = 0; j < 15; j++) {
                        helper.addToHandle(getName() + ": event-" + j);
                    }
                }
            };
            t.start();
        }

        helper.destroy();
    }
}
