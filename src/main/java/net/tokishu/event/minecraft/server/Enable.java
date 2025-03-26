package net.tokishu.event.minecraft.server;

import net.tokishu.bot.Bot;
import net.tokishu.event.minecraft.player.Join;
import net.tokishu.util.Base;
import net.tokishu.util.helper.database.Manager;
import net.tokishu.util.helper.config.ApiKey;
import net.tokishu.util.helper.database.repository.Code;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import static net.tokishu.util.helper.config.Initialization.checkConfigIntegrity;

public class Enable extends Base {

    public Enable() {
        initialize();
        registerListeners();
        new Bot().startBot();
    }

    private void initialize() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.getLogger().warning("[First start] Configuration file not found! Generating default config...");
            plugin.saveDefaultConfig();
            plugin.reloadConfig();

            FileConfiguration config = plugin.getConfig();

            if ("GENERATE_KEY_HERE".equals(config.getString("api-key"))) {
                String apiKey = ApiKey.generateApiKey();
                config.set("api-key", apiKey);
                plugin.saveConfig();
                plugin.getLogger().info("[First start] API-Key generated!");
            }

            plugin.getLogger().severe("[First start] Please configure 'config.yml' and restart the plugin!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return;
        }

        FileConfiguration config = plugin.getConfig();

        if (!checkConfigIntegrity()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Code.cleanupExpiredCodes(connection);
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60, 20 * 60 * 3); // 3 минуты

        plugin.getLogger().info("ObsidianGate launched successfully!");
    }



    private void registerListeners(){
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvents(new Join(plugin), plugin);
        plugin.getServer().getCommandMap().register("obsidian", new Command("obsidian"));
    }
}
