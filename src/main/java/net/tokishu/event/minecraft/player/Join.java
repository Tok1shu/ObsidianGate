package net.tokishu.event.minecraft.player;

import net.kyori.adventure.text.Component;
import net.tokishu.util.Base;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.tokishu.util.helper.DataBase;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Join extends Base implements Listener {

    public Join(JavaPlugin plugin) {
        super(plugin); // Предполагается, что Base принимает плагин
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (config.getBoolean("require-discord-link")) {
            String pUUID = e.getPlayer().getUniqueId().toString();
            if (!DataBase.isPlayerLinked(pUUID)){
                Component message = Component.text("Test reason");
                PlayerKickEvent.Cause cause = PlayerKickEvent.Cause.PLUGIN;
                e.getPlayer().kick(message, cause);
            }
        }
    }
}
