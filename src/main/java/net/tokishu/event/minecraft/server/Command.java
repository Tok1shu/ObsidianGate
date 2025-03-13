package net.tokishu.event.minecraft.server;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class Command extends org.bukkit.command.Command {

    public Command(@NotNull String name) {
        super(name);
        this.setDescription("ObsidianGate main command");
        this.setUsage("/obsidian [help|reload]");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7[§dObsidianGate§7] §cUse /obsidian help for help.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help":
                sender.sendMessage("§7[§dObsidianGate§7] §fCommands:\n /obsidian help\n /obsidian reload");
                break;
            case "reload":
                if (sender.hasPermission("obsidiangate.admin")) {
                    sender.getServer().getPluginManager().getPlugin("ObsidianGate").reloadConfig();
                    sender.sendMessage("§7[§dObsidianGate§7] §aConfig reloaded!");
                } else {
                    sender.sendMessage("§7[§dObsidianGate§7] §cYou don't have permission!");
                }
                break;
            default:
                sender.sendMessage("§7[§dObsidianGate§7] §cUnknown subcommand. Use /obsidian help.");
                break;
        }
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("help", "reload");
        }
        return List.of();
    }
}
