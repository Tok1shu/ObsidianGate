package net.tokishu.event.minecraft.server;

import net.tokishu.command.Help;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import net.tokishu.command.Reload;

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
                Help.sendMessage(sender);
                break;
            case "reload":
                Reload.plugin(sender);
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
