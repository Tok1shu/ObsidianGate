package net.tokishu.util.helper.config;
import net.tokishu.util.Base;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Initialization extends Base {

    public static boolean checkConfigIntegrity() {
        Map<String, ConfigValidator> requiredKeysWithValidation = new HashMap<>();

        // General settings
        requiredKeysWithValidation.put("language", new ConfigValidator(
                "english|russian",
                "Language must be either 'english' or 'russian'"
        ));

        requiredKeysWithValidation.put("discord-token", new ConfigValidator(
                "[A-Za-z0-9._-]+",
                "Discord token cannot be empty and must contain only letters, numbers, dots, underscores, or hyphens"
        ));

        requiredKeysWithValidation.put("main-guild-id", new ConfigValidator(
                "\\d+",
                "Main guild ID must be a numeric value"
        ));

        requiredKeysWithValidation.put("allow-bot-on-other-servers", new ConfigValidator(
                "true|false",
                "Allow bot on other servers must be either 'true' or 'false'"
        ));

        requiredKeysWithValidation.put("assign-verified-role", new ConfigValidator(
                "true|false",
                "Assign verified role must be either 'true' or 'false'"
        ));

        requiredKeysWithValidation.put("verified-role-id", new ConfigValidator(
                "\\d+",
                "Verified role ID must be a numeric value"
        ));

        requiredKeysWithValidation.put("auto-relink-on-nickname-change", new ConfigValidator(
                "true|false",
                "Auto relink on nickname change must be either 'true' or 'false'"
        ));

        // Whitelist settings
        requiredKeysWithValidation.put("whitelist.enabled", new ConfigValidator(
                "true|false",
                "Whitelist enabled must be either 'true' or 'false'"
        ));

        requiredKeysWithValidation.put("whitelist.type", new ConfigValidator(
                "vanilla|custom",
                "Whitelist type must be either 'vanilla' or 'custom'"
        ));

        requiredKeysWithValidation.put("whitelist.add-command", new ConfigValidator(
                ".*\\{username\\}.*",
                "Whitelist add command must contain {username} placeholder"
        ));

        requiredKeysWithValidation.put("whitelist.remove-command", new ConfigValidator(
                ".*\\{username\\}.*",
                "Whitelist remove command must contain {username} placeholder"
        ));

        // Authentication settings
        requiredKeysWithValidation.put("require-discord-link", new ConfigValidator(
                "true|false",
                "Require Discord link must be either 'true' or 'false'"
        ));

        requiredKeysWithValidation.put("enable-2fa", new ConfigValidator(
                "true|false",
                "Enable 2FA must be either 'true' or 'false'"
        ));

        requiredKeysWithValidation.put("2fa-mode", new ConfigValidator(
                "always|location-change|ip-change",
                "2FA mode must be one of: 'always', 'location-change', or 'ip-change'"
        ));

        requiredKeysWithValidation.put("require-2fa", new ConfigValidator(
                "true|false",
                "Require 2FA must be either 'true' or 'false'"
        ));

        requiredKeysWithValidation.put("2fa-expiry-time", new ConfigValidator(
                "\\d+",
                "2FA expiry time must be a positive integer value in seconds"
        ));

        // Database settings
        requiredKeysWithValidation.put("database.type", new ConfigValidator(
                "mysql|sqlite|postgres",
                "Database type must be one of: 'mysql', 'sqlite', or 'postgres'"
        ));

        // Database type-specific validations will be handled conditionally

        // API settings
        requiredKeysWithValidation.put("enable-api", new ConfigValidator(
                "true|false",
                "Enable API must be either 'true' or 'false'"
        ));

        requiredKeysWithValidation.put("server-ip", new ConfigValidator(
                "localhost|0\\.0\\.0\\.0|127\\.0\\.0\\.1|\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b",
                "Server IP must be a valid IPv4 address or 'localhost'"
        ));

        requiredKeysWithValidation.put("api-port", new ConfigValidator(
                "\\d{1,5}",
                "API port must be a valid port number between 1 and 65535"
        ));

        requiredKeysWithValidation.put("api-key", new ConfigValidator(
                "[A-Za-z0-9_-]+",
                "API key cannot be empty and must contain only letters, numbers, underscores, or hyphens"
        ));

        // Role sync settings
        requiredKeysWithValidation.put("role-sync.enable", new ConfigValidator(
                "true|false",
                "Role sync enable must be either 'true' or 'false'"
        ));

        // Optional role IDs - only check if role-sync is enabled
        // These will be checked conditionally

        // Notification settings
        requiredKeysWithValidation.put("join-leave-notifications", new ConfigValidator(
                "true|false",
                "Join leave notifications must be either 'true' or 'false'"
        ));

        requiredKeysWithValidation.put("log-nickname-changes", new ConfigValidator(
                "true|false",
                "Log nickname changes must be either 'true' or 'false'"
        ));

        boolean isValid = true;

        // Perform the basic validation
        for (Map.Entry<String, ConfigValidator> entry : requiredKeysWithValidation.entrySet()) {
            String key = entry.getKey();
            ConfigValidator validator = entry.getValue();

            // Check if key exists
            if (!config.contains(key)) {
                plugin.getLogger().severe("[Initialization] Missing key: " + key);
                isValid = false;
                continue;
            }

            // Get value and check if empty
            String value = config.getString(key);
            if (value == null || value.trim().isEmpty()) {
                plugin.getLogger().warning("[Initialization] Empty value for key: " + key + ". " + validator.errorMessage);
                continue;
            }

            // If regex pattern is provided, validate against it
            if (validator.pattern != null) {
                try {
                    Pattern pattern = Pattern.compile(validator.pattern);
                    if (!pattern.matcher(value).matches()) {
                        plugin.getLogger().severe("[Initialization] Invalid value for key: " + key +
                                ". Value '" + value + "': " + validator.errorMessage);
                        isValid = false;
                    }
                } catch (PatternSyntaxException e) {
                    plugin.getLogger().severe("[Initialization] Internal error: Invalid regex pattern for key: " + key +
                            ". Pattern: " + validator.pattern);
                    isValid = false;
                }
            }
        }

        // Database conditional validation
        String dbType = config.getString("database.type", "").toLowerCase();

        // Validate SQLite path if SQLite is selected
        if ("sqlite".equals(dbType)) {
            if (!config.contains("database.sqlite-path") || config.getString("database.sqlite-path", "").trim().isEmpty()) {
                plugin.getLogger().severe("[Initialization] SQLite database selected but 'database.sqlite-path' is missing or empty");
                isValid = false;
            }
        }
        // Validate MySQL/PostgreSQL settings if needed
        else if ("mysql".equals(dbType) || "postgres".equals(dbType)) {
            Map<String, String> dbSettings = new HashMap<>();
            dbSettings.put("database.host", "Database host cannot be empty");
            dbSettings.put("database.port", "Database port must be a valid port number");
            dbSettings.put("database.name", "Database name cannot be empty");
            dbSettings.put("database.user", "Database username cannot be empty");
            dbSettings.put("database.password", "Database password is required");
            dbSettings.put("database.prefix", "Database table prefix cannot be empty");

            if ("password".equals(config.getString("database.password"))) {
                plugin.getLogger().severe("[Initialization] Field \"database.password\" is \"password\"! THIS IS UNSECURE! Please update config.yml and restart the plugin.");
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                isValid = false;
            }

            for (Map.Entry<String, String> dbEntry : dbSettings.entrySet()) {
                String dbKey = dbEntry.getKey();
                String errorMsg = dbEntry.getValue();

                if (!config.contains(dbKey) || config.getString(dbKey, "").trim().isEmpty()) {
                    plugin.getLogger().severe("[Initialization] " + dbType + " database selected but '" + dbKey + "' is missing or empty. " + errorMsg);
                    isValid = false;
                }
            }

            // Validate port is numeric
            String portStr = config.getString("database.port", "");
            if (!portStr.matches("\\d+")) {
                plugin.getLogger().severe("[Initialization] Invalid database port: " + portStr + ". Must be a numeric value");
                isValid = false;
            }
        } else {
            plugin.getLogger().severe("[Initialization] Unknown database type: " + dbType);
            isValid = false;
        }

        // Role sync conditional validation
        boolean roleSyncEnabled = config.getBoolean("role-sync.enable", false);
        if (roleSyncEnabled) {
            String[] roleKeys = {"role-sync.roles.admin", "role-sync.roles.moderator", "role-sync.roles.vip"};
            for (String roleKey : roleKeys) {
                String roleId = config.getString(roleKey, "");
                if (!roleId.trim().isEmpty() && !roleId.matches("\\d+")) {
                    plugin.getLogger().severe("[Initialization] Invalid role ID for " + roleKey + ": " + roleId + ". Must be a numeric Discord role ID");
                    isValid = false;
                }
            }
        }

        // Special checks for values that might still contain placeholder text
        String token = config.getString("discord-token", "");
        if (token.equals("PASTE TOKEN HERE")) {
            plugin.getLogger().severe("[Initialization] Discord token is still set to default value 'PASTE TOKEN HERE'. Please set a valid token.");
            isValid = false;
        }

        String guildId = config.getString("main-guild-id", "");
        if (guildId.equals("PASTE MAIN GUILD ID HERE")) {
            plugin.getLogger().severe("[Initialization] Main guild ID is still set to default value 'PASTE MAIN GUILD ID HERE'. Please set a valid Discord guild ID.");
            isValid = false;
        }

        String roleId = config.getString("verified-role-id", "");
        if (roleId.equals("PASTE VERIFIED ROLE ID HERE")) {
            plugin.getLogger().severe("[Initialization] Verified role ID is still set to default value 'PASTE VERIFIED ROLE ID HERE'. Please set a valid Discord role ID.");
            isValid = false;
        }

//        String apiKey = config.getString("api-key", "");
//        if (apiKey.equals("GENERATE_KEY_HERE")) {
//            plugin.getLogger().severe("[Initialization] API key is still set to default value 'GENERATE_KEY_HERE'. Please generate a proper API key.");
//            isValid = false;
//        }

        if (isValid) {
            plugin.getLogger().info("[Initialization] Configuration integrity check passed.");
        } else {
            plugin.getLogger().severe("[Initialization] Configuration integrity check failed. Please fix the indicated issues.");
        }

        return isValid;
    }

    private static class ConfigValidator {
        final String pattern;
        final String errorMessage;

        ConfigValidator(String pattern, String errorMessage) {
            this.pattern = pattern;
            this.errorMessage = errorMessage;
        }
    }
}