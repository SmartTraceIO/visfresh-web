/**
 *
 */
package com.visfresh.tools.injector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.jdbc.JdbcConfig;
import com.visfresh.logs.LogUnit;
import com.visfresh.model.DeviceMessage;
import com.visfresh.tools.LocationResolvedMessageExtractor;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {JdbcConfig.class})
public class FullNativeDcsLogInjector extends NativeDcsMessageInjector {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private final Set<String> devices = new HashSet<>();
    private Long companyId;

    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public FullNativeDcsLogInjector() {
        super();

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /* (non-Javadoc)
     * @see com.visfresh.tools.ExtractedMessageHandler#handle(com.visfresh.tracker.DeviceMessage)
     */
    @Override
    public void handle(final LogUnit u, final DeviceMessage m) {
        if (supportsType(m.getType())) {
            if (!devices.contains(m.getImei())) {
                addDevice(m.getImei());
                devices.add(m.getImei());
            }

            super.handle(u, m);
        }
    }

    /**
     * @param imei
     */
    private void addDevice(final String imei) {
        //get company for device.
        if (companyId == null) {
            final List<Map<String, Object>> rows = jdbc.queryForList(
                    "select id from companies where name = 'Demo'", new HashMap<>());
            companyId = ((Number) rows.get(0).get("id")).longValue();
        }

        //insert device
        final Map<String, Object> params = new HashMap<>();
        params.put("imei", imei);
        params.put("company", companyId);
        //+-------------+--------------+------+-----+---------+-------+
        //| Field       | Type         | Null | Key | Default | Extra |
        //+-------------+--------------+------+-----+---------+-------+
        //| description | varchar(255) | YES  |     | NULL    |       |
        //| imei        | varchar(30)  | NO   | PRI | NULL    |       |
        //| color       | varchar(30)  | YES  |     | NULL    |       |
        //| name        | varchar(127) | NO   |     | NULL    |       |
        //| company     | bigint(20)   | YES  | MUL | NULL    |       |
        //| autostart   | bigint(20)   | YES  |     | NULL    |       |
        //| tripcount   | int(11)      | NO   |     | 0       |       |
        //| active      | tinyint(1)   | NO   |     | 1       |       |
        //+-------------+--------------+------+-----+---------+-------+
        //8 rows in set (0,07 sec)

        jdbc.update("insert ignore into devices(imei, name, company)"
                + " values(:imei, 'Injected by Tool', :company)", params);
    }

    public static void main(final String[] args) throws IOException {
        final LocationResolvedMessageExtractor ext = new LocationResolvedMessageExtractor();

//        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs-root.log");
        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs.log");

        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        try {
            ctx.scan(FullNativeDcsLogInjector.class.getPackage().getName());
            ctx.refresh();

            ext.addExtractedMessageHandler(ctx.getBean(FullNativeDcsLogInjector.class));
            ext.extractMessages(inFile);
        } finally {
            ctx.close();
        }
    }
}
