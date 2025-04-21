package net.tokishu.util.helper.minecraft;

import net.tokishu.util.Base;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginVersion extends Base {
    private static final Logger logger = plugin.getLogger();
    private static final Pattern VERSION_PATTERN = Pattern.compile("([A-Z]+)-(\\d{6})-(\\d{4})-P(\\d+)-V(\\d+).*");

    private String buildType;
    private LocalDateTime buildDate;
    private int parameterCount;
    private String targetVersion;
    private String fullVersionString;

    public PluginVersion() {
        checkVersion();
    }

    /**
     * Main method to check the plugin version and handle updates if needed
     */
    public void checkVersion() {
        String codeVersion = Base.buildVersion;
        String configVersion = config.getString("build", "");

        logger.info("[PluginVersion] Checking plugin version...");

        VersionInfo codeVersionInfo = parseVersion(codeVersion);
        VersionInfo configVersionInfo = parseVersion(configVersion);

        if (codeVersionInfo == null || configVersionInfo == null) {
            logger.warning("[PluginVersion] Could not parse version information. Using fallback procedures.");
            return;
        }

        // Check if versions are identical
        if (codeVersion.equals(configVersion)) {
            logger.info("[PluginVersion] Plugin version is up to date.");
            return;
        }

        // Check if config version is older than code version (update needed)
        if (codeVersionInfo.buildDate.isBefore(configVersionInfo.buildDate)) {
            logger.info("[PluginVersion] Config version is newer than code version. No update needed.");
            return;
        }

        // Handle update process
        handleUpdate(codeVersionInfo, configVersionInfo);
    }

    /**
     * Parse the version string into components
     */
    private VersionInfo parseVersion(String versionString) {
        Matcher matcher = VERSION_PATTERN.matcher(versionString);
        if (!matcher.matches()) {
            logger.warning("[PluginVersion] Invalid version format: " + versionString);
            return null;
        }

        try {
            VersionInfo info = new VersionInfo();
            info.fullVersion = versionString;
            info.buildType = matcher.group(1);

            String dateStr = matcher.group(2);
            String timeStr = matcher.group(3);

            String year = "20" + dateStr.substring(0, 2);
            String month = dateStr.substring(2, 4);
            String day = dateStr.substring(4, 6);

            String hour = timeStr.substring(0, 2);
            String minute = timeStr.substring(2, 4);

            String fullDateTimeStr = year + "-" + month + "-" + day + "T" + hour + ":" + minute + ":00";
            info.buildDate = LocalDateTime.parse(fullDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // Parse parameter count and target version
            info.parameterCount = Integer.parseInt(matcher.group(4));

            String versionNumberStr = matcher.group(5);
            info.targetVersion = versionNumberStr.length() == 3
                    ? versionNumberStr.substring(0, 1) + "." + versionNumberStr.substring(1, 3)
                    : versionNumberStr;

            return info;
        } catch (Exception e) {
            logger.warning("[PluginVersion] Error parsing version: " + e.getMessage());
            return null;
        }
    }

    /**
     * Handle the update process including config migration
     */
    private void handleUpdate(VersionInfo codeInfo, VersionInfo configInfo) {
        logger.info("[PluginVersion] Updating plugin from version " + configInfo.fullVersion + " to " + codeInfo.fullVersion);

        // 1. Create backup of the current config
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        File backupFile = new File(plugin.getDataFolder(), "config.yml.preUpdate-" +
                configInfo.buildDate.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm")));

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            if (configFile.exists()) {
                Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                logger.info("[PluginVersion] Created backup of existing config: " + backupFile.getName());
            }

            // 2. Load existing configuration
            FileConfiguration oldConfig = null;
            if (configFile.exists()) {
                oldConfig = YamlConfiguration.loadConfiguration(configFile);
                if (!configFile.delete()){
                    logger.severe("[PluginVersion] error while deleting old config file!");
                }
            }

            // 3. Generate new default config
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            FileConfiguration newConfig = plugin.getConfig();

            // 4. Apply old values to new config (with improved nested handling)
            if (oldConfig != null) {
                mergeConfigurations(newConfig, oldConfig, "", new HashSet<>());
            }

            // 5. Update build version in config
            newConfig.set("build", codeInfo.fullVersion);

            // 6. Save updated config
            plugin.saveConfig();
            logger.info("[PluginVersion] Config updated successfully to version " + codeInfo.fullVersion);

            // 7. Check for parameter count changes
            if (codeInfo.parameterCount > configInfo.parameterCount) {
                int newParamsCount = codeInfo.parameterCount - configInfo.parameterCount;
                logger.info("[PluginVersion] Plugin update added " + newParamsCount + " new parameter(s)");

                findNewParameters(newConfig, oldConfig, "");
            }

            // 8. Run giggling function for additional update actions
            giggling();

        } catch (IOException e) {
            logger.severe("[PluginVersion] Error during plugin update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Recursively merges old configuration values into the new configuration,
     * preserving all nested parameters including custom ones not in the default config
     */
    private void mergeConfigurations(FileConfiguration newConfig, FileConfiguration oldConfig,
                                     String path, Set<String> processedPaths) {
        if (path.equals("build")) {
            return;
        }

        if (processedPaths.contains(path)) {
            return;
        }

        processedPaths.add(path);

        ConfigurationSection currentNewSection = path.isEmpty() ? newConfig : newConfig.getConfigurationSection(path);
        ConfigurationSection currentOldSection = path.isEmpty() ? oldConfig : oldConfig.getConfigurationSection(path);

        if (currentNewSection == null || currentOldSection == null) {
            if (currentNewSection == null && currentOldSection != null) {
                copySection(oldConfig, newConfig, path);
            }
            return;
        }

        for (String key : currentOldSection.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (currentOldSection.isConfigurationSection(key)) {
                if (!newConfig.contains(fullPath)) {
                    copySection(oldConfig, newConfig, fullPath);
                } else {
                    mergeConfigurations(newConfig, oldConfig, fullPath, processedPaths);
                }
            } else {
                if (newConfig.contains(fullPath)) {
                    newConfig.set(fullPath, oldConfig.get(fullPath));
                } else {
                    newConfig.set(fullPath, oldConfig.get(fullPath));
                    logger.info("[PluginVersion] Preserved custom parameter: " + fullPath + " = " + oldConfig.get(fullPath));
                }
            }
        }
    }

    /**
     * Helper method to copy an entire configuration section
     */
    private void copySection(FileConfiguration source, FileConfiguration target, String path) {
        ConfigurationSection section = source.getConfigurationSection(path);
        if (section == null) return;

        for (String key : section.getKeys(true)) {
            String fullPath = path + "." + key;
            if (!section.isConfigurationSection(key)) {
                target.set(fullPath, source.get(fullPath));
                logger.info("[PluginVersion] Copied custom section item: " + fullPath);
            }
        }
    }

    /**
     * Find and report new parameters added in the updated config
     */
    private void findNewParameters(FileConfiguration newConfig, FileConfiguration oldConfig, String path) {
        ConfigurationSection newSection = path.isEmpty() ? newConfig : newConfig.getConfigurationSection(path);

        if (newSection == null) return;

        for (String key : newSection.getKeys(false)) {
            String fullPath = path.isEmpty() ? key : path + "." + key;

            if (fullPath.equals("build")) continue;

            if (newSection.isConfigurationSection(key)) {
                findNewParameters(newConfig, oldConfig, fullPath);
            } else if (!oldConfig.contains(fullPath)) {
                logger.info("[PluginVersion] New parameter detected: " + fullPath + " = " + newConfig.get(fullPath));
            }
        }
    }

    /**
     * Method for custom actions to perform during update
     * This will be implemented in future updates
     */
    private void giggling() {
        // This function is intentionally left empty for now
        // It will be used for specific update actions in the future
    }

    /**
     * Inner class to store parsed version information
     */
    private static class VersionInfo {
        String buildType;
        LocalDateTime buildDate;
        int parameterCount;
        String targetVersion;
        String fullVersion;

        @Override
        public String toString() {
            return "VersionInfo{" +
                    "buildType='" + buildType + '\'' +
                    ", buildDate=" + buildDate +
                    ", parameterCount=" + parameterCount +
                    ", targetVersion='" + targetVersion + '\'' +
                    ", fullVersion='" + fullVersion + '\'' +
                    '}';
        }
    }
}