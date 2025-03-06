package net.tokishu.util.helper;
import static org.bukkit.Bukkit.getLogger;
import net.tokishu.ObsidianGate;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.List;


public class Initialization {
    private ObsidianGate plugin;

    public static boolean checkConfigIntegrity() {
        FileConfiguration config = ObsidianGate.getPluginConfig();

        List<String> requiredKeys = List.of(
                "language",
                "discord-token",
                "main-guild-id",
                "allow-bot-on-other-servers",
                "assign-verified-role",
                "verified-role-id",
                "auto-relink-on-nickname-change",
                "whitelist.enabled",
                "whitelist.type",
                "whitelist.add-command",
                "whitelist.remove-command",
                "require-discord-link",
                "enable-2fa",
                "2fa-mode",
                "require-2fa",
                "2fa-expiry-time",
                "database.host",
                "database.port",
                "database.name",
                "database.user",
                "database.password",
                "database.prefix",
                "enable-api",
                "server-ip",
                "api-port",
                "api-key",
                "role-sync.enable",
                "role-sync.roles.admin",
                "role-sync.roles.moderator",
                "role-sync.roles.vip",
                "join-leave-notifications",
                "log-nickname-changes"
        );

        for (String key : requiredKeys) {
            if (!config.contains(key)) {
                getLogger().info("Missing key: " + key);
                return false;
            }

            String value = config.getString(key);
            if (value == null || value.trim().isEmpty()) {
                getLogger().warning("Empty value for key: " + key);
            }
        }

        getLogger().info("Configuration integrity check passed.");
        return true;
    }
}
