package net.tokishu.util;

import net.tokishu.ObsidianGate;
import net.tokishu.util.helper.database.Manager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import net.tokishu.bot.Bot;

import java.io.File;
import java.sql.Connection;

public class Base {
    protected static final ObsidianGate plugin = ObsidianGate.getInstance();
    public static final FileConfiguration config = ObsidianGate.getPluginConfig();
    protected static final File sqliteFile = new File(plugin.getDataFolder(), config.getString("database.sqlite-path", "obsidiangate.db"));
    public static Connection connection = Manager.getInstance().getConnection();
    public static String botTag = null;

    public Base(JavaPlugin plugin) {}
    public Base() {}

    public static String getBotTag() {
        if (botTag == null) {
            Bot bot = Bot.getInstance();
            if (bot != null && bot.getBot() != null) {
                botTag = bot.getBot().getSelfUser().getAsTag();
            }
        }
        return botTag != null ? botTag : "Bot not initialized";
    }
}