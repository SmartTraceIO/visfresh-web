/**
 *
 */
package com.visfresh.tools.injector;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.visfresh.jdbc.JdbcConfig;
import com.visfresh.jdbc.TrackerEventDao;
import com.visfresh.logs.LogUnit;
import com.visfresh.tools.ExtractedMessageHandler;
import com.visfresh.tools.MessageExtractor;
import com.visfresh.tracker.DeviceMessage;
import com.visfresh.unwiredlabs.UnwiredLabsLocationService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEventInjector implements ExtractedMessageHandler {
    private final Set<String> types = new HashSet<>();
    private List<DeviceMessage> messages = new LinkedList<>();
    private final Date endDate;
    private final Date startDate;

    /**
     * Default constructor.
     * @throws ParseException
     */
    public TrackerEventInjector() throws ParseException {
        super();
        this.startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-01-01 00:07:00");
        this.endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2017-02-12 22:07:16");
        types.add("INIT");
        types.add("AUT");
        types.add("RSP");
        types.add("VIB");
        types.add("STP");
        types.add("BRT");
        types.add("DRK");
        types.add("BAT0");
        types.add("BAT1");
        types.add("BAT2");
        types.add("CRG0");
        types.add("CRG1");
    }

    /* (non-Javadoc)
     * @see com.visfresh.tools.ExtractedMessageHandler#handle(com.visfresh.tracker.DeviceMessage)
     */
    @Override
    public void handle(final LogUnit u, final DeviceMessage m) {
        if (m.getTime().after(endDate) || m.getTime().before(startDate)){
            return;
        }

        if (!types.contains(m.getType())) {
            m.setType("UNDEF");
        }

        messages.add(m);
    }
    /**
     * @return the messages
     */
    public List<DeviceMessage> getMessages() {
        return messages;
    }

    public static void main(final String[] args) throws Exception {
        final MessageExtractor ext = new MessageExtractor();
        ext.addDevice("354430070005245");


//        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs-root.log");
        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs.log");

        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        try {
            //initialize context
            ctx.scan(
                JdbcConfig.class.getPackage().getName(),
                UnwiredLabsLocationService.class.getPackage().getName()
                );
            ctx.refresh();

            //collect messages
            final TrackerEventInjector collector = new TrackerEventInjector();
            ext.addExtractedMessageHandler(collector);
            ext.setLocationProvider(ctx.getBean(UnwiredLabsLocationService.class));
            ext.extractMessages(inFile);

            //insert messages into DB.
            final TrackerEventDao dao = ctx.getBean(TrackerEventDao.class);
            for (final DeviceMessage msg : collector.getMessages()) {
                dao.save(msg, 4181l);
            }
        } finally {
            ctx.close();
        }
    }
}
