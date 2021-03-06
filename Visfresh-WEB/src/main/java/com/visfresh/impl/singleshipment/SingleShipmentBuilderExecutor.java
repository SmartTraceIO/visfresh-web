/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.visfresh.io.shipment.SingleShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentBuilderExecutor {
    private final List<SingleShipmentPartBuilder> builders = new LinkedList<>();

    private ThreadFactory threadFactory = new ThreadFactory() {
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "pool-singleshipment-thread-";

        {
            final SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            t.setDaemon(false);
            t.setPriority((Thread.NORM_PRIORITY + Thread.MIN_PRIORITY) / 2);
            return t;
        }
    };

    /**
     * @return the single shipment data.
     * @throws Exception
     */
    public SingleShipmentData execute(final SingleShipmentBuildContext context) throws Exception {
        sortBuilders(builders);
        final ExecutorService pool = Executors.newFixedThreadPool(builders.size(), threadFactory);

        try {
            final List<Callable<Void>> tasks = new LinkedList<>();
            for (final SingleShipmentPartBuilder builder : builders) {
                tasks.add(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        builder.fetchData();
                        return null;
                    }
                });
            }

            pool.invokeAll(tasks, 3, TimeUnit.MINUTES);
        } finally {
            pool.shutdown();
        }

        for (final SingleShipmentPartBuilder b : builders) {
            b.build(context);
        }

        return context.getData();
    }
    public void addBuilder(final SingleShipmentPartBuilder b) {
        builders.add(b);
    }
    /**
     * Sorts the single shipment builders.
     */
    protected void sortBuilders(final List<SingleShipmentPartBuilder> builders) {
        builders.sort((b1, b2) -> {
            final int p1 = b1.getPriority();
            final int p2 = b2.getPriority();
            return (p2 < p1) ? -1 : ((p2 == p1) ? 0 : 1);
        });
    }
}
