package net.tokishu.command.minecraft;

import net.tokishu.util.Base;
import net.tokishu.util.helper.database.repository.User;
import net.tokishu.util.helper.minecraft.MinecraftAPI;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class Unlink extends Base {

    /**
     * Handles unlinking a Minecraft account by nickname.
     *
     * @param sender The command sender
     * @param nickname Minecraft username to unlink
     */
    public static void unlink(CommandSender sender, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            sender.sendMessage("§7[§dObsidianGate§7] §cHow did you end up here?");
            return;
        }

        UUID linkedUuid = MinecraftAPI.findUuidByNicknameFromMap(nickname);

        if (linkedUuid == null) {
            sender.sendMessage("§7[§dObsidianGate§7] §cNo linked account found for nickname: " + nickname);
            return;
        }

        try {
            User.unlinkDiscord(connection, linkedUuid.toString(), sender.getName());
            sender.sendMessage("§7[§dObsidianGate§7] §aAccount successfully unlinked for nickname: " + nickname);
        } catch (Exception e) {
            sender.sendMessage("§7[§dObsidianGate§7] §cError unlinking account: " + e.getMessage());
        }
    }

    /**
     * Provides instructions for unlinking an account.
     *
     * @param sender The command sender
     */
    public static void unlink(CommandSender sender) {
        sender.sendMessage("§7[§dObsidianGate§7] §eTo unlink your account, send 'unlink' to §a" + getBotTag() + "§e in private messages.");
    }

    /**
     * Provides tab completion for linked Minecraft usernames.
     *
     * @param partialNickname Partial nickname to complete
     * @return List of matching nicknames
     */
    public static List<String> getLinkedNicknamesCompletion(String partialNickname) {
        return MinecraftAPI.getLinkedNicknamesCompletion(partialNickname);
    }
}