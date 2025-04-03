package net.tokishu.util.helper.database.repository;

import net.tokishu.util.Base;
import net.tokishu.util.helper.database.Manager;
import java.sql.*;
import java.time.Instant;
import java.util.Random;

public class Code extends Base {
    private static final Random random = new Random();
    private static final String PREFIX = Manager.getPrefix();
    private static final String DB_TYPE = Manager.getDbType();

    /**
     * Generates a 6-digit registration code for the player
     * @param connection DB connection
     * @param uuid Player's UUID (String)
     * @param codeExpiryTime Expiry time in seconds (default 300 seconds)
     * @return The generated 6-digit code
     */
    public static String generateRegistrationCode(Connection connection, String uuid, int codeExpiryTime) {
        String existingCode = getExistingCode(connection, uuid);
        if (existingCode != null) {
            return existingCode;
        }

        String code = generateRandomCode();
        long expiryTime = Instant.now().getEpochSecond() + codeExpiryTime;

        String query = getInsertCodeQuery();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            stmt.setString(2, code);
            stmt.setLong(3, expiryTime);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error generating registration code: " + e.getMessage());
            return null;
        }

        return code;
    }

    private static String generateRandomCode() {
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            codeBuilder.append(random.nextInt(10));
        }
        return codeBuilder.toString();
    }

    private static String getInsertCodeQuery() {
        if ("mysql".equalsIgnoreCase(DB_TYPE) || "postgres".equalsIgnoreCase(DB_TYPE)) {
            return "INSERT INTO " + PREFIX + "registration_codes (uuid, code, expiry_time) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE code = VALUES(code), expiry_time = VALUES(expiry_time);";
        } else {
            return "INSERT OR REPLACE INTO " + PREFIX + "registration_codes (uuid, code, expiry_time) VALUES (?, ?, ?);";
        }
    }

    private static String getExistingCode(Connection connection, String uuid) {
        String query = "SELECT code FROM " + PREFIX + "registration_codes WHERE uuid = ? LIMIT 1;";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("code");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error checking existing registration code: " + e.getMessage());
        }

        return null;
    }

    /**
     * Checks if a registration code is valid
     * @param connection Database connection
     * @param uuid Player's UUID
     * @param code Code to check
     * @return True if the code is valid, false otherwise
     */
    public static boolean isValidRegistrationCode(Connection connection, String uuid, String code) {
        String query = "SELECT code, expiry_time FROM " + PREFIX + "registration_codes WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedCode = rs.getString("code");
                    long expiryTime = rs.getLong("expiry_time");
                    long currentTime = Instant.now().getEpochSecond();

                    return storedCode.equals(code) && currentTime <= expiryTime;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error validating registration code: " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves the UUID associated with a registration code
     * @param connection Database connection
     * @param code Registration code to verify
     * @return UUID if code is valid, null otherwise
     */
    public static String getUuidByCode(Connection connection, String code) {
        String query = "SELECT uuid FROM " + PREFIX + "registration_codes WHERE code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("uuid");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error getting UUID by code: " + e.getMessage());
        }
        return null;
    }

    /**
     * Removes a registration code from the database
     * @param connection Database connection
     * @param uuid Player's UUID
     */
    public static void removeRegistrationCode(Connection connection, String uuid) {
        String query = "DELETE FROM " + PREFIX + "registration_codes WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error removing registration code: " + e.getMessage());
        }
    }

    /**
     * Cleans up expired registration codes from the database
     * @param connection Database connection
     */
    public static void cleanupExpiredCodes(Connection connection) {
        String query = "DELETE FROM " + PREFIX + "registration_codes WHERE expiry_time < ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            long currentTime = Instant.now().getEpochSecond();
            stmt.setLong(1, currentTime);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error cleaning up expired codes: " + e.getMessage());
        }
    }
}