/**
 *
 */
package au.smarttrace.eel.db;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class BeaconChannelLockDao {
    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public BeaconChannelLockDao() {
        super();
    }

    /**
     * @param date should be unlocked all channels with unlockon field before given date.
     */
    public void clearLocksOldestThan(final Date date) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("unlockon", date);
        jdbc.update("delete from beaconchannels where unlockon < :unlockon", params);
    }

    /**
     * @param beacons set of beacons.
     * @param gateway gateway.
     * @param t time of unlock channels.
     */
    public void createOrUpdateLocks(final Set<String> beacons, final String gateway, final Date t) {
        if (beacons.size() == 0) {
            return;
        }

        final HashMap<String, Object> params = new HashMap<>();
        params.put("unlockon", t);
        params.put("gateway", gateway);

        final StringBuilder update = new StringBuilder(
                "update beaconchannels set unlockon = :unlockon where gateway = :gateway and beacon in (");
        final StringBuilder insert = new StringBuilder(
                "insert ignore into beaconchannels (gateway, beacon, unlockon) values ");

        int i = 0;
        for (final String beacon : beacons) {
            final String key = "b_" + i;
            params.put(key, beacon);

            if (i > 0) {
                insert.append(',');
                update.append(',');
            }

            insert.append("\n(:gateway,:").append(key).append(",:unlockon)");
            update.append(':').append(key);
            i++;
        }
        update.append(')');

        jdbc.update(insert.toString(), params);
        jdbc.update(update.toString(), params);
    }

    /**
     * @param gateway
     * @param t
     * @return
     */
    public Set<String> getLocked(final String gateway, final Date t) {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("unlockon", t);
        params.put("gateway", gateway);

        final Set<String> locked = new HashSet<>();
        final List<Map<String, Object>> rows = jdbc.queryForList(
                "select beacon from beaconchannels where gateway = :gateway and unlockon >= :unlockon",
                params);
        for (final Map<String, Object> row : rows) {
            locked.add((String) row.get("beacon"));
        }

        return locked;
    }
}
