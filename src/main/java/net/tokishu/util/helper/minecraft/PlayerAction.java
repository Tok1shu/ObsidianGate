package net.tokishu.util.helper.minecraft;

import net.kyori.adventure.text.Component;
import net.tokishu.util.Base;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.UUID;

public class PlayerAction extends Base {
    /**
     * Checks if a player is currently online on the server
     * @param uuid Player's UUID
     * @return true if online, false if offline
     */
    public static boolean isPlayerOnline(String uuid) {
        try {
            UUID playerUUID = UUID.fromString(uuid);
            Player player = Bukkit.getPlayer(playerUUID);

            return player != null && player.isOnline();
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("[PlayerAction Class] BAD UUID: " + uuid);
            return false;
        }
    }

    public static void kickPlayerWithReason(Player player, String reason){
        Component message = Component.text(reason);
        PlayerKickEvent.Cause cause = PlayerKickEvent.Cause.PLUGIN;
        player.getPlayer().kick(message, cause);
    }
}
