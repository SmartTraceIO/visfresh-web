/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.io.json.SmsSerializer;
import com.visfresh.services.RetryableException;
import com.visfresh.services.SmsService;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.sms.SmsMessage;
import com.visfresh.sms.SmsMessagingException;
import com.visfresh.sms.SmsSender;
import com.visfresh.sms.clicksend.ClickSendSmsAdapter;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {ClickSendSmsAdapter.class})
public class SmsServiceImpl implements SmsService, SystemMessageHandler {
    @Autowired
    private SmsMessageDispatcher dispatcher;
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);
    @Autowired
    private SmsSender helper ;
    private final SmsSerializer serializer = new SmsSerializer();

    /**
     * Processor ID.
     */
    protected String processorId;

    /**
     * @param env spring environment.
     */
    public SmsServiceImpl() {
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
        dispatcher.sendSystemMessage(payload, SystemMessageType.SMS);
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
            helper.sendSms(Arrays.asList(m));
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
