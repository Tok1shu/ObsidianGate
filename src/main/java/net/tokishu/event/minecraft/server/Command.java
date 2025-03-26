package net.tokishu.event.minecraft.server;

import net.tokishu.command.minecraft.Help;
import net.tokishu.command.minecraft.Reload;
import net.tokishu.command.minecraft.Unlink;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.tokishu.util.helper.minecraft.LP;

import java.util.ArrayList;
import java.util.List;

public class Command extends org.bukkit.command.Command {

    public Command(@NotNull String name) {
        super(name);
        this.setDescription("ObsidianGate main command");
        this.setUsage("/obsidian [help|reload|unlink]");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§7[§dObsidianGate§7] §cUse /obsidian help for help.");
            return true;
        }

        Player player = (Player) sender;
        switch (args[0].toLowerCase()) {
            case "help":
                Help.sendMessage(sender);
                break;
            case "reload":
                if (LP.checkPlayerPermission(player, "obsidian.admin.unlink")) {
                    Unlink.unlink(sender, args[1]);
                }else{
                    sender.sendMessage("§7[§dObsidianGate§7] §cYou do nat permission to do that!");
                }
                Reload.plugin(sender);
                break;
            case "unlink":
                if (args.length > 1) {
                    if (LP.checkPlayerPermission(player, "obsidian.admin.unlink")) {
                        Unlink.unlink(sender, args[1]);
                    }else{
                        sender.sendMessage("§7[§dObsidianGate§7] §cYou do nat permission to do that!");
                    }
                } else {
                    Unlink.unlink(sender);
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
            List<String> availableCommands = new ArrayList<>();

            availableCommands.add("help");

            if (sender.hasPermission("obsidian.admin.reload")) {
                availableCommands.add("reload");
            }

            availableCommands.add("unlink");

            return availableCommands;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("unlink")) {
            if (sender.hasPermission("obsidian.admin.unlink")) {
                return Unlink.getLinkedNicknamesCompletion(args[1]);
            }
        }

        return List.of();
    }
}