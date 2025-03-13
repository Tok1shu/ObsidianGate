package net.tokishu.event.minecraft.player;

import net.kyori.adventure.text.Component;
import net.tokishu.util.Base;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.tokishu.util.helper.database.Manager;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Join extends Base implements Listener {

    public Join(JavaPlugin plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (config.getBoolean("require-discord-link")) {
            String pUUID = e.getPlayer().getUniqueId().toString();
            if (!Manager.isPlayerLinked(pUUID)){
                String generatedCode = Manager.generateRegistrationCode(pUUID);
                Component message = Component.text("Please send to Hentai#8330 code "+ generatedCode);
                PlayerKickEvent.Cause cause = PlayerKickEvent.Cause.PLUGIN;
                e.getPlayer().kick(message, cause);
            }
        }
    }
}
