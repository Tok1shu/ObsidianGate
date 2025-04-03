package net.tokishu.command.minecraft;

import it.unimi.dsi.fastutil.shorts.ShortRBTreeSet;
import net.tokishu.util.Base;
import net.tokishu.util.helper.database.repository.Code;
import net.tokishu.util.helper.database.repository.User;
import net.tokishu.util.helper.minecraft.PlayerAction;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static org.bukkit.Bukkit.getPlayer;

public class Link extends Base {
    /**
     * Handles linking a Minecraft account.
     *
     * @param sender The command sender
     */
    public static void link(CommandSender sender) {
        if (config.getBoolean("require-discord-link")){
            sender.sendMessage("§7[§dObsidianGate§7] §cYou can't use this command now!");
            return;
        }
        Player player = (Player) sender;
        if (!User.isPlayerLinked(connection, player.getUniqueId().toString())){
            String code = Code.generateRegistrationCode(connection, player.getUniqueId().toString(), 300);
            sender.sendMessage("§7[§dObsidianGate§7] §ePlease, send code §a" + code + " §eto the discord bot §a" + getBotTag());
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }else{
            sender.sendMessage("§7[§dObsidianGate§7] §cYou are already linked to a Discord account.");
        }
    }

    /**
     * Notify player that's successfully linked account
     * @param uuidStr UUID (String)
     * @return status is it was successfully sent (if player online)
     */
    public static boolean successfullyLink(String uuidStr) {
        Player player = Bukkit.getPlayer(UUID.fromString(uuidStr));
        if (player != null) {
            player.sendMessage("§7[§dObsidianGate§7] §aYour account was successfully linked!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            return true;
        }
        return false;
    }
}
