package net.tokishu.util.helper;

import net.tokishu.ObsidianGate;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Objects;
import java.util.Random;

public class ApiKey {
    private ObsidianGate plugin;

    public static String generateApiKey() {
        Random random = new Random();
        StringBuilder apiKey = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int randomValue = random.nextInt(16);
            apiKey.append(Integer.toHexString(randomValue).toUpperCase());
        }
        return apiKey.toString();
    }

    public static boolean checkApiKey(String key) {
        FileConfiguration config = ObsidianGate.getPluginConfig();
        String configKey = config.getString("api-key");
        return Objects.equals(configKey, key);
    }
}
