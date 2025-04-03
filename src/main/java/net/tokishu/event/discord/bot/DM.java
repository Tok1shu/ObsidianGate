package net.tokishu.event.discord.bot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.tokishu.command.minecraft.Link;
import net.tokishu.util.Base;
import net.tokishu.util.helper.database.Manager;
import net.tokishu.util.helper.database.repository.User;
import net.tokishu.util.helper.discord.RoleManage;
import net.tokishu.util.helper.database.repository.Code;

import java.sql.Connection;
import java.sql.SQLException;

public class DM extends Base {
    public void handleDirectMessage(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw().trim();
        String discordId = event.getAuthor().getId();

        // Processing a 6-digit number (registration code)
        if (message.matches("\\d{6}")) {
            String linkedUuid = User.getLinkedUuid(connection, discordId);
            if (linkedUuid != null) {
                event.getAuthor().openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage("You are already linked to a Minecraft account.").queue()
                );
            } else {
                String playerUuid = Code.getUuidByCode(connection, message);
                if (playerUuid != null) {
                    boolean linkSuccess = User.linkPlayerToDiscord(connection, playerUuid, discordId, message);
                    if (linkSuccess) {
                        String mainGuildId = plugin.getConfig().getString("main-guild-id");
                        String verifiedRoleId = plugin.getConfig().getString("verified-role-id");

                        if (mainGuildId != null && verifiedRoleId != null) {
                            RoleManage.addRoleToUser(
                                    Long.parseLong(mainGuildId),
                                    Long.parseLong(discordId),
                                    Long.parseLong(verifiedRoleId)
                            );
                        }

                        event.getAuthor().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage("✅ Successfully verified! Your Minecraft account has been linked.").queue()
                        );
                        Link.successfullyLink(playerUuid);
                    } else {
                        event.getAuthor().openPrivateChannel().queue(privateChannel ->
                                privateChannel.sendMessage("❌ Verification failed. Please try again or contact support.").queue()
                        );
                    }
                } else {
                    event.getAuthor().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage("❌ Invalid code. Please check and try again.").queue()
                    );
                }
            }

        }
        // Handling the unlink command
        else if (message.equalsIgnoreCase("unlink")) {
            String linkedUuid = User.getLinkedUuid(connection, discordId);
            if (linkedUuid != null) {
                boolean unlinked = User.unlinkDiscord(connection, linkedUuid, "self");
                if (unlinked) {
                    event.getAuthor().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage("Your Minecraft account has been unlinked.").queue()
                    );
                } else {
                    event.getAuthor().openPrivateChannel().queue(privateChannel ->
                            privateChannel.sendMessage("Failed to unlink your account. Please contact support.").queue()
                    );
                }
            } else {
                event.getAuthor().openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage("No Minecraft account is currently linked to your Discord.").queue()
                );
            }
        }
        // Other message
        else {
            String linkedUuid = User.getLinkedUuid(connection, discordId);
            if (linkedUuid != null) {
                event.getAuthor().openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage("Your account is linked with UUID: `" + linkedUuid + "`\n\n" +
                                "If you want to unlink your account, type **unlink**").queue()
                );
            } else {
                event.getAuthor().openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage("Your Discord account is not linked to a Minecraft account.\n\n" +
                                "Please enter the 6-digit code provided on the server to link your account.").queue()
                );
            }
        }
    }
}