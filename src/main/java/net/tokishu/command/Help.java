package net.tokishu.command;

import net.tokishu.util.Base;
import org.bukkit.command.CommandSender;

public class Help extends Base {
    public static void sendMessage(CommandSender sender) {
        sender.sendMessage("§7[§dObsidianGate§7] §fCommands:\n" +
                "/obsidian help\n" +
                "/obsidian reload");
    }
}
