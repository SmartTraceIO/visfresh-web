/**
 *
 */
package com.visfresh.autodetect;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.model.DeviceMessage;
import com.visfresh.tools.MessageExtractor;
import com.visfresh.unwiredlabs.UnwiredLabsLocationService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NearestEventsSearchRaw extends AbstractNearestEventsSearch {
    private final File messages;
    private final Date startDate;
    private final Date endDate;
    /**
     * Default constructor.
     * @throws IOException
     * @throws ParseException
     */
    public NearestEventsSearchRaw(final double distance, final File messages, final Date startDate,
            final Date endDate) throws IOException, ParseException {
        super(distance);
        this.messages = messages;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    /**
     * @return
     * @throws ParseException
     * @throws IOException
     * @throws Exception
     */
    @Override
    protected List<DeviceMessage> loadMessages() {
        final List<DeviceMessage> messages = new LinkedList<>();

        final MessageExtractor extractor = new MessageExtractor(){
            @Override
            protected boolean containsDevice(final String message) {
                if (!this.devices.isEmpty()) {
                    return super.containsDevice(message);
                }
                return true;
            }
        };

        extractor.addExtractedMessageHandler((u, m) -> {
            final Date time = m.getTime();
            if (!time.after(endDate) && !time.before(startDate)) {
                messages.add(m);
            }
        });

        extractor.setLocationProvider(new UnwiredLabsLocationService() {
            {
                setUrl("https://ap1.unwiredlabs.com/v2/process.php");
                setToken("a93bb2c1f9699a");
            }
        });

        try {
            extractor.extractMessages(this.messages);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return messages;
    }

    public static void main(final String[] args) throws IOException, ParseException {
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        final File messages = new File("/home/soldatov/tmp/logs/354430070008215-visfresh-dcs.log.1");
        final Date startDate = df.parse("2016-02-14 16:24");
        final Date endDate = df.parse("2018-02-15 11:26");

        new NearestEventsSearchRaw(1000, messages, startDate, endDate).run();
    }
}
