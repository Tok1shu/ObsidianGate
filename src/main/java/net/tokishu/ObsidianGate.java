package net.tokishu;

import net.tokishu.util.helper.ApiKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import static net.tokishu.util.helper.Initialization.checkConfigIntegrity;

public final class ObsidianGate extends JavaPlugin {
    private static ObsidianGate instance;
    @Override
    public void onEnable() {
        instance = this;
        FileConfiguration config = this.getConfig();

        if (!checkConfigIntegrity()) {
            getLogger().severe("Configuration errors detected! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if ("GENERATE_KEY_HERE".equals(config.getString("api-key"))) {
            String apiKey = ApiKey.generateApiKey();
            config.set("api-key", apiKey);
            saveConfig();
            getLogger().info("API-Key generated!");
        }

        getLogger().info("ObsidianGate launched successfully!");
    }

    @Override
    public void onDisable() {

    }

    public static ObsidianGate getInstance() {
        return instance;
    }

    public static FileConfiguration getPluginConfig() {
        return getInstance().getConfig();
    }
}
