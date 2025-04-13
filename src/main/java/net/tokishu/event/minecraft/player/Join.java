package net.tokishu.event.minecraft.player;

import net.kyori.adventure.text.Component;
import net.tokishu.util.Base;
import net.tokishu.util.helper.database.repository.Code;
import net.tokishu.util.helper.database.repository.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Join extends Base implements Listener {

    private final Set<UUID> kickedPlayers = new HashSet<>();

    public Join(JavaPlugin plugin) {
        super(plugin);
    }

    @EventHandler()
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        if (config.getBoolean("require-discord-link")) {
            UUID uuid = e.getUniqueId();
            String pUUID = uuid.toString();

            if (!User.isPlayerLinked(connection, pUUID)) {
                String generatedCode = Code.generateRegistrationCode(connection, pUUID, 300);
                Component message = Component.text("Please send to " + getBotTag() + " code " + generatedCode);
                kickedPlayers.add(uuid);
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        UUID playerUUID = e.getPlayer().getUniqueId();
        if (kickedPlayers.remove(playerUUID)) {
            e.quitMessage(null);
        }
    }
}