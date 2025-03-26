package net.tokishu.command.minecraft;

import net.tokishu.bot.Bot;
import net.tokishu.util.Base;
import org.bukkit.command.CommandSender;

public class Reload extends Base {
    public static void plugin(CommandSender sender){
        if (sender.hasPermission("obsidiangate.admin")) {
            sender.getServer().getPluginManager().getPlugin("ObsidianGate").reloadConfig();
            new Bot().restartBot();
            sender.sendMessage("§7[§dObsidianGate§7] §aConfig reloaded!");
        } else {
            sender.sendMessage("§7[§dObsidianGate§7] §cYou don't have permission!");
        }
    }
}
