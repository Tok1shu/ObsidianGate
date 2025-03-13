package net.tokishu.util.helper;

import net.tokishu.util.Base;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.Random;

public class DataBase extends Base {

    private static DataBase instance;
    private static Connection connection;
    private final String dbType;
    private final String host;
    private final String database;
    private final String user;
    private final String password;
    private static String prefix = "";
    private final int port;
    private final String sqliteFileName;
    private final String sqliteFullPath;
    private final int codeExpiryTime;
    private final Random random = new Random();

    private DataBase() {

        this.dbType = config.getString("database.type", "mysql");
        this.host = config.getString("database.host", "localhost");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.name", "obsidiangate");
        this.user = config.getString("database.user", "root");
        this.password = config.getString("database.password", "password");
        prefix = config.getString("database.prefix", "OG_");
        this.sqliteFileName = config.getString("database.sqlite-path", "obsidiangate.db");
        this.sqliteFullPath = new File(plugin.getDataFolder(), sqliteFileName).getAbsolutePath();
        this.codeExpiryTime = config.getInt("2fa-expiry-time", 300);

        setupDB();
    }

    public static DataBase getInstance() {
        if (instance == null) {
            instance = new DataBase();
        }
        return instance;
    }

    private void setupDB() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            if ("mysql".equalsIgnoreCase(dbType) || "postgres".equalsIgnoreCase(dbType)) {
                String url = "jdbc:" + dbType + "://" + host + ":" + port + "/" + database;
                connection = DriverManager.getConnection(url, user, password);
            } else if ("sqlite".equalsIgnoreCase(dbType)) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFullPath);
            } else {
                plugin.getLogger().severe("[Database] Configuration error: The 'database.type' parameter is invalid or missing. Please specify one of the following options: 'mysql', 'postgres', or 'sqlite'.");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
            createTablesIfNeeded();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Connection error: " + e.getMessage());
            plugin.getLogger().severe("[Database] PLUGIN WILL BE DISABLED DUE TO DB CONNECTION ERROR");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private void createTablesIfNeeded() {
        try (Statement stmt = connection.createStatement()) {
            // Linked accounts table
            String linkedAccountsTable = "CREATE TABLE IF NOT EXISTS " + prefix + "linked_accounts (" +
                    "uuid TEXT PRIMARY KEY," +
                    "discord_id TEXT NOT NULL" +
                    ");";
            stmt.executeUpdate(linkedAccountsTable);

            // Registration codes table
            String registrationCodesTable = "CREATE TABLE IF NOT EXISTS " + prefix + "registration_codes (" +
                    "uuid TEXT PRIMARY KEY," +
                    "code TEXT NOT NULL," +
                    "expiry_time BIGINT NOT NULL" +
                    ");";
            stmt.executeUpdate(registrationCodesTable);

            // 2FA sessions table
            String twoFaSessionsTable = "CREATE TABLE IF NOT EXISTS " + prefix + "2fa_sessions (" +
                    "uuid TEXT PRIMARY KEY," +
                    "session_token TEXT NOT NULL," +
                    "ip_address TEXT NOT NULL," +
                    "last_login BIGINT NOT NULL," +
                    "is_confirmed BOOLEAN DEFAULT FALSE" +
                    ");";
            stmt.executeUpdate(twoFaSessionsTable);

        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error creating tables: " + e.getMessage());
        }
    }

    private ResultSet getData(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt.executeQuery();
    }

    private void sendData(String sql, Object... params) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        }
    }

    /**
     * Generates a 6-digit registration code for the player
     * @param uuid Player's UUID
     * @return The generated 6-digit code
     */
    public String generateRegistrationCode(String uuid) {
        // Generate a random 6-digit code
        StringBuilder codeBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            codeBuilder.append(random.nextInt(10));
        }
        String code = codeBuilder.toString();

        // Calculate expiry time (current time + config expiry time in seconds)
        long expiryTime = Instant.now().getEpochSecond() + codeExpiryTime;

        // Store code in database
        String query;
        if ("mysql".equalsIgnoreCase(dbType) || "postgres".equalsIgnoreCase(dbType)) {
            query = "INSERT INTO " + prefix + "registration_codes (uuid, code, expiry_time) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE code = VALUES(code), expiry_time = VALUES(expiry_time);";
        } else {
            query = "INSERT OR REPLACE INTO " + prefix + "registration_codes (uuid, code, expiry_time) VALUES (?, ?, ?);";
        }

        try {
            sendData(query, uuid, code, expiryTime);
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error generating registration code: " + e.getMessage());
            return null;
        }

        return code;
    }

    /**
     * Checks if a registration code is valid
     * @param uuid Player's UUID
     * @param code Code to check
     * @return True if the code is valid, false otherwise
     */
    public boolean isValidRegistrationCode(String uuid, String code) {
        String query = "SELECT code, expiry_time FROM " + prefix + "registration_codes WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedCode = rs.getString("code");
                    long expiryTime = rs.getLong("expiry_time");
                    long currentTime = Instant.now().getEpochSecond();

                    // Check if code matches and hasn't expired
                    return storedCode.equals(code) && currentTime <= expiryTime;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error validating registration code: " + e.getMessage());
        }
        return false;
    }

    /**
     * Removes a registration code from the database
     * @param uuid Player's UUID
     */
    public void removeRegistrationCode(String uuid) {
        String query = "DELETE FROM " + prefix + "registration_codes WHERE uuid = ?";
        try {
            sendData(query, uuid);
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error removing registration code: " + e.getMessage());
        }
    }

    public String getLinkedDiscordId(String uuid) {
        String query = "SELECT discord_id FROM " + prefix + "linked_accounts WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("discord_id");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error getting discord_id: " + e.getMessage());
        }
        return null;
    }

    /**
     * Links a player to a Discord ID, optionally validating a registration code
     * @param uuid Player's UUID
     * @param discordId Discord ID to link
     * @param code Optional registration code for verification
     * @return True if linking was successful, false otherwise
     */
    public boolean linkPlayerToDiscord(String uuid, String discordId, String code) {
        // If code is provided, validate it
        if (code != null && !code.isEmpty()) {
            if (!isValidRegistrationCode(uuid, code)) {
                return false;
            }

            // Remove the code after successful validation
            removeRegistrationCode(uuid);
        }

        // Link the accounts
        String query;
        if ("mysql".equalsIgnoreCase(dbType) || "postgres".equalsIgnoreCase(dbType)) {
            query = "INSERT INTO " + prefix + "linked_accounts (uuid, discord_id) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE discord_id = VALUES(discord_id);";
        } else {
            query = "INSERT OR REPLACE INTO " + prefix + "linked_accounts (uuid, discord_id) VALUES (?, ?);";
        }

        try {
            sendData(query, uuid, discordId);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error linking player to Discord: " + e.getMessage());
            return false;
        }
    }

    /**
     * Overload of linkPlayerToDiscord for backward compatibility
     */
    public void linkPlayerToDiscord(String uuid, String discordId) {
        linkPlayerToDiscord(uuid, discordId, null);
    }

    public static boolean isPlayerLinked(String uuid) {
        String query = "SELECT discord_id FROM " + prefix + "linked_accounts WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error checking if player is linked: " + e.getMessage());
        }
        return false;
    }

    /**
     * Cleans up expired registration codes from the database
     */
    public void cleanupExpiredCodes() {
        String query = "DELETE FROM " + prefix + "registration_codes WHERE expiry_time < ?";
        try {
            long currentTime = Instant.now().getEpochSecond();
            sendData(query, currentTime);
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error cleaning up expired codes: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error closing connection: " + e.getMessage());
        }
    }
}