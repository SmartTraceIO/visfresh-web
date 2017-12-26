/**
 *
 */
package com.visfresh.init.jdbc;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.aspectj.NeedRerunWhenDbDeadlock;
import com.visfresh.dao.MySqlUtilsDao;
import com.visfresh.utils.ExceptionUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Aspect
@Component
public class RerunnerWhenDbDeadLock {
    private static final Logger log = LoggerFactory.getLogger(RerunnerWhenDbDeadLock.class);
    @Autowired
    private MySqlUtilsDao dao;
    /**
     * Default constructor.
     */
    public RerunnerWhenDbDeadLock() {
        super();
    }
    @Around(value = "@annotation(a)")
    public Object around(final ProceedingJoinPoint joinPoint, final NeedRerunWhenDbDeadlock a) throws Throwable {
        int retry = 0;
        while (true) {
            try {
                final Object proceed = joinPoint.proceed();
                if (retry > 0) {
                    log.warn("Method " + joinPoint.getSignature().getName() + " has retried " + retry
                            + " times according of DB deadlock");
                }
                return proceed;
            } catch (final Throwable e) {
                if (retry < a.numRetry() && ExceptionUtils.isMySqlDeadLock(e)) {
                    log.error("Deadlock detected:\n" + dao.getCurrentProcesses(), e);
                    retry++;

                    //sleep 500 ms, then rerun.
                    try {
                        Thread.sleep(500);
                    } catch (final Exception e1) {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
    }
}
