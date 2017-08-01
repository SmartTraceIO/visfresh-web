/**
 *
 */
package com.visfresh.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.DeviceMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MessageSnapshootDao {
    private static byte[] SALT = { 65, -63, 108, 125, 38, -43, 29, -28, -82,
            -87, -114, 24, -33, -40, 77, 115 };

    public static final String TABLE = "snapshoots";

    public static final String IMEI = "imei";
    public static final String SIGNATURE = "signature";

    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public MessageSnapshootDao() {
        super();
    }

    public boolean saveSignature(final List<DeviceMessage> messages) {
        if (messages.size() == 0) {
            return false;
        }

        final String signature = createSignature(messages);
        return doSaveReceived(messages.get(0).getImei(), signature);
    }

    /**
     * @param imei device IMEI.
     * @param signature signature.
     * @return
     */
    protected boolean doSaveReceived(final String imei, final String signature) {
        final Map<String, Object> params = new HashMap<>();
        params.put("imei", imei);
        params.put("signature", signature);

        final int result = jdbc.update("insert ignore into "
                + TABLE + "(" + IMEI + ", " + SIGNATURE + ")"
                + " values(:imei, :signature)", params);
        return result > 0;
    }
    /**
     * @param messages
     * @return
     */
    public static String createSignature(final List<DeviceMessage> messages) {
        final DeviceMessage first = messages.get(0);
        final String imei = first.getImei();
        final Date time = first.getTime();

        //build messages hash
        final StringBuilder sb = new StringBuilder();
        for (final DeviceMessage dm : messages) {
            sb.append(dm.toString());
        }

        return String.join("|", imei,
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(time),
                createMd5Hash(sb.toString()));
    }

    public static String createMd5Hash(final String str) {
        String hash = null;
        try {
            // Create MessageDigest instance for MD5
            final MessageDigest md = MessageDigest.getInstance("MD5");
            // Add password bytes to digest
            md.update(SALT);

            // Get the hash's bytes
            final byte[] bytes = md.digest(str.getBytes());

            // This bytes[] has bytes in decimal format;
            // Convert it to hexadecimal format
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            // Get complete hashed password in hex format
            hash = sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return hash;
    }
}
