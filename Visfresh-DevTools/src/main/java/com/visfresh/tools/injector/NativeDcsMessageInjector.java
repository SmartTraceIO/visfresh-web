/**
 *
 */
package com.visfresh.tools.injector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.visfresh.jdbc.JdbcConfig;
import com.visfresh.tools.ExtractedMessageHandler;
import com.visfresh.tools.LocationProviderBuilder;
import com.visfresh.tools.MessageExtractor;
import com.visfresh.tracker.DeviceMessage;
import com.visfresh.tracker.Location;
import com.visfresh.tracker.LocationProvider;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {JdbcConfig.class})
public class NativeDcsMessageInjector implements ExtractedMessageHandler {
    private final Set<String> types = new HashSet<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final String TABLE = "systemmessages";

    public static final String TYPE_FIELD = "type";
    public static final String TIME_FIELD = "time";
    public static final String PROCESSOR_FIELD = "processor";
    public static final String RETRYON_FIELD = "retryon";
    public static final String NUMRETRY_FIELD = "numretry";
    public static final String MESSAGE_FIELD = "message";

    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public NativeDcsMessageInjector() {
        super();
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

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /* (non-Javadoc)
     * @see com.visfresh.tools.ExtractedMessageHandler#handle(com.visfresh.tracker.DeviceMessage)
     */
    @Override
    public void handle(final DeviceMessage m) {
        if (types.contains(m.getType())) {
            final String payload = buildSystemMessagePayload(m);
            saveSystemMessage(payload);
        }
    }

    /**
     * @param payload
     */
    private void saveSystemMessage(final String payload) {
        final Map<String, Object> paramMap = new HashMap<String, Object>();

        final String sql = "insert into " + TABLE + " (" +
                TIME_FIELD
                + "," + TYPE_FIELD
                + "," + RETRYON_FIELD
                + "," + MESSAGE_FIELD
             + ")" + " values("
                + ":"+ TIME_FIELD
                + ", :" + TYPE_FIELD
                + ", :" + RETRYON_FIELD
                + ", :" + MESSAGE_FIELD
                + ")";

        paramMap.put(TIME_FIELD, new Date());
        paramMap.put(TYPE_FIELD, "Tracker");
        paramMap.put(RETRYON_FIELD, new Date());
        paramMap.put(MESSAGE_FIELD, payload);

        jdbc.update(sql, paramMap);
    }

    /**
     * @param e
     * @return
     */
    private String buildSystemMessagePayload(final DeviceMessage e) {
        final Location loc = e.getLocation();

        final JsonObject obj = new JsonObject();
        obj.addProperty("battery", e.getBattery());
        obj.addProperty("temperature", e.getTemperature());
        obj.addProperty("time", sdf.format(e.getTime()));
        obj.addProperty("type", e.getType());
        if (loc != null) {
            obj.addProperty("latitude", loc.getLatitude());
            obj.addProperty("longitude", loc.getLongitude());
        }
        obj.addProperty("imei", e.getImei());
        obj.addProperty("createdOn", sdf.format(e.getLoggTime()));
        return obj.toString();
    }

    public static void main(final String[] args) throws IOException {
        final MessageExtractor ext = new MessageExtractor();
        ext.addDevice("354430070001467");
        ext.addDevice("354430070001541");
        ext.addDevice("354430070001558");
        ext.addDevice("354430070001921");
        ext.addDevice("354430070002564");
        ext.addDevice("354430070002788");
        ext.addDevice("354430070005534");
        ext.addDevice("354430070005807");
        ext.addDevice("354430070006680");
        ext.addDevice("354430070007001");
        ext.addDevice("354430070007555");
        ext.addDevice("354430070010542");


//        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs-root.log");
        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs.log");

        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        try {
            ctx.scan(NativeDcsMessageInjector.class.getPackage().getName());
            ctx.refresh();

            ext.addExtractedMessageHandler(ctx.getBean(NativeDcsMessageInjector.class));
            ext.setLocationProvider(buildLocationProvider(inFile, ext.getDevices()));
            ext.extractMessages(inFile);
        } finally {
            ctx.close();
        }
    }

    /**
     * @param file
     * @param devices
     * @return
     * @throws IOException
     */
    private static LocationProvider buildLocationProvider(final File file,
            final Set<String> devices) throws IOException {
        final LocationProviderBuilder b = new LocationProviderBuilder();
        for (final String device : devices) {
            b.addDevice(device);
        }
        return b.build(file);
    }
}
