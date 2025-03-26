package net.tokishu.util.helper.database;

import net.tokishu.util.Base;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.Random;

public class Manager extends Base {
    private static Manager instance;
    private static Connection connection;
    private static String dbType = "";
    private String host;
    private String database;
    private String user;
    private String password;
    private static String prefix = "";
    private int port;
    private String sqliteFullPath;
    private static int codeExpiryTime = 0;
    private static final Random random = new Random();

    private Manager(String host, String database, String user, String password, int port, String sqliteFullPath) {
        super();
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.port = port;
        this.sqliteFullPath = sqliteFullPath;
        setupDatabaseConfiguration();
        setupDB();
    }

    private void setupDatabaseConfiguration() {
        dbType = config.getString("database.type", "mysql");
        this.host = config.getString("database.host", "localhost");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.name", "obsidiangate");
        this.user = config.getString("database.user", "root");
        this.password = config.getString("database.password", "password");
        prefix = config.getString("database.prefix", "OG_");
        String sqliteFileName = config.getString("database.sqlite-path", "obsidiangate.db");
        this.sqliteFullPath = new File(plugin.getDataFolder(), sqliteFileName).getAbsolutePath();
        codeExpiryTime = config.getInt("2fa-expiry-time", 300);
    }

    private Manager(JavaPlugin plugin) {
        super(plugin);
        setupDatabaseConfiguration();
        setupDB();
    }

    public static Manager getInstance() {
        if (instance == null) {
            instance = new Manager(plugin);
        }
        return instance;
    }

    private void setupDB() {
        try {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            connectToDatabase();
            createTablesIfNeeded();
        } catch (SQLException e) {
            handleDatabaseConnectionError(e);
        }
    }

    private void connectToDatabase() throws SQLException {
        if ("mysql".equalsIgnoreCase(dbType) || "postgres".equalsIgnoreCase(dbType)) {
            String url = "jdbc:" + dbType + "://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url, user, password);
        } else if ("sqlite".equalsIgnoreCase(dbType)) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFullPath);
        } else {
            throw new SQLException("Invalid database type");
        }
    }

    private void handleDatabaseConnectionError(SQLException e) {
        plugin.getLogger().severe("[Database] Connection error: " + e.getMessage());
        plugin.getLogger().severe("[Database] PLUGIN WILL BE DISABLED DUE TO DB CONNECTION ERROR");
        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }

    private void createTablesIfNeeded() {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(createLinkedAccountsTableQuery());
            stmt.executeUpdate(createRegistrationCodesTableQuery());
            stmt.executeUpdate(createTwoFaSessionsTableQuery());
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error creating tables: " + e.getMessage());
        }
    }

    private String createLinkedAccountsTableQuery() {
        return "CREATE TABLE IF NOT EXISTS " + prefix + "linked_accounts (" +
                "uuid TEXT PRIMARY KEY," +
                "discord_id TEXT NOT NULL" +
                ");";
    }

    private String createRegistrationCodesTableQuery() {
        return "CREATE TABLE IF NOT EXISTS " + prefix + "registration_codes (" +
                "uuid TEXT PRIMARY KEY," +
                "code TEXT NOT NULL," +
                "expiry_time BIGINT NOT NULL" +
                ");";
    }

    private String createTwoFaSessionsTableQuery() {
        return "CREATE TABLE IF NOT EXISTS " + prefix + "2fa_sessions (" +
                "uuid TEXT PRIMARY KEY," +
                "session_token TEXT NOT NULL," +
                "ip_address TEXT NOT NULL," +
                "last_login BIGINT NOT NULL," +
                "is_confirmed BOOLEAN DEFAULT FALSE" +
                ");";
    }

    public Connection getConnection() {
        return connection;
    }

    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] Error closing connection: " + e.getMessage());
        }
    }

    // Getter for database prefix
    public static String getPrefix() {
        return prefix;
    }

    // Getter for database type
    public static String getDbType() {
        return dbType;
    }
}