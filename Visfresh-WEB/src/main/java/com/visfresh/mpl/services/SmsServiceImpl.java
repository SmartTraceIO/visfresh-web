/**
 *
 */
package com.visfresh.mpl.services;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.entities.SystemMessage;
import com.visfresh.io.json.SmsSerializer;
import com.visfresh.io.sms.SmsMessage;
import com.visfresh.io.sms.SmsMessagingException;
import com.visfresh.io.sms.SmsServiceHelper;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SmsService;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SmsServiceImpl implements SmsService, SystemMessageHandler {
    @Autowired
    private SmsMessageDispatcher dispatcher;
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final SmsServiceHelper helper = new SmsServiceHelper();
    private final SmsSerializer serializer = new SmsSerializer();

    /**
     * Processor ID.
     */
    protected String processorId;

    /**
     * @param env spring environment.
     */
    @Autowired
    public SmsServiceImpl(final Environment env) {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SmsService#sendMessage(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void sendMessage(final String[] phones, final String subject, final String message) {
        final SmsMessage msg = new SmsMessage();
        msg.setPhones(phones);
        msg.setMessage(message);
        msg.setSubject(subject);

        final String payload = serializer.toJson(msg).toString();
        dispatcher.sendSystemMessage(payload);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final SmsMessage m = serializer.parseSmsMessage(
                SerializerUtils.parseJson(msg.getMessageInfo()));
        log.debug("SMS message dequeued: " + msg.getMessageInfo());

        try {
            helper.sendMessage(m.getPhones(), m.getSubject(), m.getMessage());
        } catch (final SmsMessagingException e) {
            log.error("Failed to send SMS", e);
            throw new RetryableException("Failed to send SMS", e);
        }
    }

    @PreDestroy
    public void stop() {
        dispatcher.setSmsHandler(null);
    }
    @PostConstruct
    public void start() {
        dispatcher.setSmsHandler(this);
    }
}
