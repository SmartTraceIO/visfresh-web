/**
 *
 */
package com.visfresh.impl.services;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.visfresh.init.instance.Instance;
import com.visfresh.services.EmailService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class UserHomeChecker {
    private static Logger log = LoggerFactory.getLogger(UserHomeChecker.class);

    private static final long TIME_OUT = 10 * 60 * 1000l; // 10 minutes

    @Autowired
    private EmailService emailer;
    @Autowired
    private Instance instance;

    /**
     * Default constructor.
     */
    public UserHomeChecker() {
        super();
    }

    /**
     * Check user home.
     */
    @Scheduled(fixedDelay = TIME_OUT)
    public void checkUserHome() {
        log.debug("Check of user home folder");

        final String userHome = System.getProperty("user.home");
        if (userHome.indexOf("root") > -1) {
            sendAlarm("Unexpected user home folder '" + userHome
                    + "'\nPossible server was started under root."
                    + " Please restart it by correct user, by following steps:\n"
                    + "1. service tomcat stop\n"
                    + "2. make small pause\n"
                    + "3. check service stoped by 'ps -Af | grep java'\n"
                    + "4. if not stoped, stop it by 'kill -9 <processname>' under root user\n"
                    + "5. then check again is it stopped and start then by command\n"
                    + "service tomcat start");
        }
    }

    /**
     * @param message alert message.
     */
    protected void sendAlarm(final String message) {
        try {
            emailer.sendMessageToSupport("Alert from instance " + instance.getId(), message);
        } catch (final MessagingException e) {
            log.error("Failed to send message\n"
                    + message + "\n to support", e);
        }
    }
}
