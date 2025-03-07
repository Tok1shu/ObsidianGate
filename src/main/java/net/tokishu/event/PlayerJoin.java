package net.tokishu.event;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.tokishu.ObsidianGate;

public class PlayerJoin implements Listener {
    private ObsidianGate plugin;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        FileConfiguration config = ObsidianGate.getPluginConfig();

        if (config.getBoolean("require-discord-link")) {
            // TODO: Проверить в бд а есть ли игрок там.
        }
    }
}
