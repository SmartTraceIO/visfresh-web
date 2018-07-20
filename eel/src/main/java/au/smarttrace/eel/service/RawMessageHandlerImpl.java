/**
 *
 */
package au.smarttrace.eel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import au.smarttrace.eel.rawdata.EelMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class RawMessageHandlerImpl implements EelMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(RawMessageHandlerImpl.class);

    /**
     * Default constructor.
     */
    public RawMessageHandlerImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see au.smarttrace.eel.service.EelMessageHandler#handleMessage(au.smarttrace.eel.rawdata.EelMessage)
     */
    @Override
    public void handleMessage(final EelMessage msg) {
        log.debug("EEl message received IMEI: " + msg.getImei()
            + "\nNumber of packages: " + msg.getPackages());
    }
}
