package net.tokishu.event;

import net.tokishu.util.Base;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.tokishu.ObsidianGate;
import net.tokishu.util.helper.DataBase;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.UUID;

public class PlayerJoin extends Base implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (config.getBoolean("require-discord-link")) {
            String pUUID = e.getPlayer().getUniqueId().toString();
            if (!DataBase.isPlayerLinked(pUUID)){
                //TODO: KICK PLAYER
            }
        }
    }
}
