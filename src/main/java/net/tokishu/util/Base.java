package net.tokishu.util;

import net.tokishu.ObsidianGate;
import net.tokishu.util.helper.database.Manager;
import net.tokishu.util.helper.database.repository.User;
import net.tokishu.util.helper.minecraft.MinecraftAPI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import net.tokishu.bot.Bot;

import java.io.File;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Base {
    protected static final ObsidianGate plugin = ObsidianGate.getInstance();
    public static FileConfiguration config = ObsidianGate.getPluginConfig();
    protected static File sqliteFile;
    public static Connection connection;
    public static String botTag = null;

    protected static Map<String, String> linkedUsersMap = new HashMap<>();

    public Base(JavaPlugin plugin) {}
    public Base() {}

    /**
     * Let’s say NO to incorrect initialization!
     * "Don't run before your father into the fire~"
     */
    public static void initializeConnection() {
        config = ObsidianGate.getPluginConfig();
        sqliteFile = new File(plugin.getDataFolder(), config.getString("database.sqlite-path", "obsidiangate.db"));
        connection = Manager.getInstance().getConnection();
    }

    /**
     * Updates the map of linked Minecraft usernames.
     * This method can be called periodically or when needed to refresh the linked users.
     */
    public static void updateLinkedNicknamesMap() {
        List<String[]> linkedUsers = User.getAllLinkedUsers(connection);
        linkedUsersMap = MinecraftAPI.fetchMinecraftUsernames(linkedUsers);
    }

    /**
     * Retrieves the current map of linked users.
     *
     * @return Map of UUIDs to Minecraft usernames
     */
    public static Map<String, String> getLinkedUsersMap() {
        if (linkedUsersMap.isEmpty()) {
            updateLinkedNicknamesMap();
        }
        return linkedUsersMap;
    }

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