package net.tokishu;

import net.tokishu.util.helper.ApiKey;
import net.tokishu.util.helper.DataBase;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

import static net.tokishu.util.helper.Initialization.checkConfigIntegrity;

public final class ObsidianGate extends JavaPlugin {
    private static ObsidianGate instance;
    @Override
    public void onEnable() {
        instance = this;

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().warning("[First start] Configuration file not found! Generating default config...");
            saveDefaultConfig();

            reloadConfig();
            FileConfiguration config = getConfig();

            if ("GENERATE_KEY_HERE".equals(config.getString("api-key"))) {
                String apiKey = ApiKey.generateApiKey();
                config.set("api-key", apiKey);
                saveConfig();
                getLogger().info("[Fisrt start] API-Key generated!");
            }

            getLogger().severe("[First start] Please configure 'config.yml' and restart the plugin!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        FileConfiguration config = this.getConfig();

        if (!checkConfigIntegrity()) {
            getLogger().severe("Configuration errors detected! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if ("password".equals(config.getString("database.password").toLowerCase()) && !"sqlite".equals(config.getString("database.type"))) {
            getLogger().severe("[STARTER] Field \"database.password\" is \"password\"! THIS IS UNSECURE! Please update config.yml and restart the plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        DataBase database = DataBase.getInstance();

        new BukkitRunnable() {
            @Override
            public void run() {
                database.cleanupExpiredCodes();
            }
        }.runTaskTimerAsynchronously(this, 20 * 60, 20 * 60 * 3); // 3 minutes

        getLogger().info("ObsidianGate launched successfully!");
    }

    @Override
    public void onDisable() {

    }

    public static ObsidianGate getInstance() {return instance;}
    public static FileConfiguration getPluginConfig() {return getInstance().getConfig();}
}
