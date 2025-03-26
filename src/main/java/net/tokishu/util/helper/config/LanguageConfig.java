package net.tokishu.util.helper.config;

import net.tokishu.util.Base;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;

public class LanguageConfig extends Base {
    private static FileConfiguration languageConfig;
    private static File languageFile;

    /**
     * Loads the language configuration based on the language specified in the main config
     * @param plugin The JavaPlugin instance
     */
    public static void loadLanguageConfig(JavaPlugin plugin) {
        // Get the language from the main config
        String lang = config.getString("language", "english"); // Default to English

        // Create languages folder if it doesn't exist
        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        // Construct language file path
        languageFile = new File(langFolder, lang + ".yml");

        // Create default language file if it doesn't exist
        if (!languageFile.exists()) {
            try {
                InputStream resourceStream = plugin.getResource("languages/" + lang + ".yml");

                if (resourceStream != null) {
                    Files.copy(resourceStream, languageFile.toPath());
                } else {
                    languageFile.createNewFile();
                    plugin.getLogger().warning("No default language file found for: " + lang);
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create language file: " + e.getMessage());
            }
        }

        // Load the language configuration
        languageConfig = YamlConfiguration.loadConfiguration(languageFile);
    }

    /**
     * Get a translated string from the language configuration
     * @param path The path to the translation key
     * @return The translated string, or the path if not found
     */
    public static String getText(String path) {
        if (languageConfig == null) {
            throw new IllegalStateException("Language configuration not loaded. Call loadLanguageConfig() first.");
        }

        return languageConfig.getString(path, path);
    }

    /**
     * Get a translated string with formatting
     * @param path The path to the translation key
     * @param args Formatting arguments
     * @return The formatted translated string
     */
    public static String getText(String path, Object... args) {
        String text = getText(path);
        return String.format(text, args);
    }

    /**
     * Save changes to the language configuration file
     * @throws IOException If saving fails
     */
    public static void saveLanguageConfig() throws IOException {
        if (languageFile != null && languageConfig != null) {
            languageConfig.save(languageFile);
        }
    }
}