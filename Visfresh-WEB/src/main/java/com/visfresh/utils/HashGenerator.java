/**
 *
 */
package com.visfresh.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class HashGenerator {
    private static byte[] SALT = { 65, -63, 108, 125, 38, -43, 29, -28, -82,
            -87, -114, 24, -33, -40, 77, 115 };

    /**
     * Default constructor.
     */
    private HashGenerator() {
        super();
    }

    public static String createMd5Hash(final String password) {
        String generatedPassword = null;
        try {
            // Create MessageDigest instance for MD5
            final MessageDigest md = MessageDigest.getInstance("MD5");
            // Add password bytes to digest
            md.update(SALT);

            // Get the hash's bytes
            final byte[] bytes = md.digest(password.getBytes());

            // This bytes[] has bytes in decimal format;
            // Convert it to hexadecimal format
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            // Get complete hashed password in hex format
            generatedPassword = sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return generatedPassword;
    }
    /**
     * @param args
     */
    public static void main(final String[] args) {
        System.out.println(createMd5Hash(args[0]));
    }
}
