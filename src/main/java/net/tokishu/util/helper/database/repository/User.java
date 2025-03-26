package net.tokishu.util.helper.database.repository;

import net.tokishu.util.helper.database.Manager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static net.tokishu.command.minecraft.Unlink.updateLinkedNicknames;

public class User {
    private static final String PREFIX = Manager.getPrefix();
    private static final String DB_TYPE = Manager.getDbType();

    /**
     * Get the linked Discord ID for a specific player UUID
     * @param connection Database connection
     * @param uuid Player's UUID
     * @return Discord ID or null if not linked
     */
    public static String getLinkedDiscordId(Connection connection, String uuid) {
        String query = "SELECT discord_id FROM " + PREFIX + "linked_accounts WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("discord_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database] Error getting discord_id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get the linked UUID for a specific Discord ID
     * @param connection Database connection
     * @param discordId Discord ID
     * @return UUID or null if not linked
     */
    public static String getLinkedUuid(Connection connection, String discordId) {
        String query = "SELECT uuid FROM " + PREFIX + "linked_accounts WHERE discord_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, discordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("uuid");
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database] Error getting UUID by discord_id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Check if a Discord ID is already linked to any account
     * @param connection Database connection
     * @param discordId Discord ID to check
     * @return True if the Discord ID is linked, false otherwise
     */
    public static boolean isDiscordIdLinked(Connection connection, String discordId) {
        String query = "SELECT uuid FROM " + PREFIX + "linked_accounts WHERE discord_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, discordId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[Database] Error checking if discord_id is linked: " + e.getMessage());
        }
        return false;
    }

    /**
     * Links a player to a Discord ID, optionally validating a registration code
     * @param connection Database connection
     * @param uuid Player's UUID
     * @param discordId Discord ID to link
     * @param code Optional registration code for verification
     * @return True if linking was successful, false otherwise
     */
    public static boolean linkPlayerToDiscord(Connection connection, String uuid, String discordId, String code) {
        if (code != null && !code.isEmpty()) {
            if (!Code.isValidRegistrationCode(connection, uuid, code)) {
                return false;
            }
            Code.removeRegistrationCode(connection, uuid);
        }

        String query = getLinkPlayerQuery();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            stmt.setString(2, discordId);
            stmt.executeUpdate();
            updateLinkedNicknames(connection);
            return true;
        } catch (SQLException e) {
            System.err.println("[Database] Error linking player to Discord: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve all linked users with their UUIDs and Discord IDs
     * @param connection Database connection
     * @return List of linked users, where each entry is a String array [uuid, discord_id]
     */
    public static List<String[]> getAllLinkedUsers(Connection connection) {
        List<String[]> linkedUsers = new ArrayList<>();
        String query = "SELECT uuid, discord_id FROM " + PREFIX + "linked_accounts";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String uuid = rs.getString("uuid");
                String discordId = rs.getString("discord_id");
                linkedUsers.add(new String[]{uuid, discordId});
            }
        } catch (SQLException e) {
            System.err.println("[Database] Error retrieving linked users: " + e.getMessage());
        }

        return linkedUsers;
    }

    private static String getLinkPlayerQuery() {
        if ("mysql".equalsIgnoreCase(DB_TYPE) || "postgres".equalsIgnoreCase(DB_TYPE)) {
            return "INSERT INTO " + PREFIX + "linked_accounts (uuid, discord_id) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE discord_id = VALUES(discord_id);";
        } else {
            return "INSERT OR REPLACE INTO " + PREFIX + "linked_accounts (uuid, discord_id) VALUES (?, ?);";
        }
    }

    /**
     * Check if a player is linked to a Discord account
     * @param connection Database connection
     * @param uuid Player's UUID
     * @return True if the player is linked, false otherwise
     */
    public static boolean isPlayerLinked(Connection connection, String uuid) {
        String query = "SELECT discord_id FROM " + PREFIX + "linked_accounts WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[Database] Error checking if player is linked: " + e.getMessage());
        }
        return false;
    }

    /**
     * Unlink a player's Discord account
     * @param connection Database connection
     * @param uuid Player's UUID
     * @return True if successfully unlinked, false otherwise
     */
    public static boolean unlinkDiscord(Connection connection, String uuid) {
        String query = "DELETE FROM " + PREFIX + "linked_accounts WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            int affectedRows = stmt.executeUpdate();
            updateLinkedNicknames(connection);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("[Database] Error unlinking Discord account: " + e.getMessage());
            return false;
        }
    }
}