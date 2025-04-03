package net.tokishu.event.minecraft.server;

import net.tokishu.command.minecraft.Help;
import net.tokishu.command.minecraft.Link;
import net.tokishu.command.minecraft.Reload;
import net.tokishu.command.minecraft.Unlink;
import net.tokishu.util.helper.database.repository.User;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.tokishu.util.helper.minecraft.LP;

import java.util.ArrayList;
import java.util.List;

import static net.tokishu.util.Base.config;
import static net.tokishu.util.Base.connection;

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

        switch (args[0].toLowerCase()) {
            case "help":
                Help.sendMessage(sender);
                break;
            case "reload":
                if (isPlayer(sender)){
                    Player player = (Player) sender;
                    if (LP.checkPlayerPermission(player, "obsidian.admin.reload")) {
                        Reload.plugin(sender);
                    }else{
                        sender.sendMessage("§7[§dObsidianGate§7] §cYou do nat permission to do that!");
                    }
                }else {
                    Reload.plugin(sender);
                }
                break;
            case "unlink":
                if (args.length > 1) {
                    if (isPlayer(sender)){
                        Player player = (Player) sender;
                        if (LP.checkPlayerPermission(player, "obsidian.admin.unlink")) {
                            Unlink.unlink(sender, args[1]);
                        }else{
                            sender.sendMessage("§7[§dObsidianGate§7] §cYou do nat permission to do that!");
                        }
                    }else {
                        Unlink.unlink(sender, args[1]);
                    }
                } else {
                    if (isPlayer(sender)) {
                        Unlink.unlink(sender);
                    }else{
                        sender.sendMessage("HEY! You really need to unlink your account? (-_-)");
                    }
                }
                break;
            case "link":
                Link.link(sender);
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
            Player player = (Player) sender;

            availableCommands.add("help");

            if (sender.hasPermission("obsidian.admin.reload")) {
                availableCommands.add("reload");
            }

            availableCommands.add("unlink");
            if (!User.isPlayerLinked(connection, player.getUniqueId().toString())){
                if (!config.getBoolean("require-discord-link")){
                    availableCommands.add("link");
                }
            }

            return availableCommands;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("unlink")) {
            if (sender.hasPermission("obsidian.admin.unlink")) {
                return Unlink.getLinkedNicknamesCompletion(args[1]);
            }
        }

        return List.of();
    }

    private boolean isPlayer(CommandSender sender){
        return sender instanceof Player;
    }
}