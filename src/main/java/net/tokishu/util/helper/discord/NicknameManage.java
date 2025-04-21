package net.tokishu.util.helper.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.tokishu.bot.Bot;
import net.tokishu.util.Base;
import net.tokishu.util.helper.database.repository.User;
import net.tokishu.util.helper.minecraft.MinecraftAPI;

import java.util.Map;

public class NicknameManage extends Base {

    /**
     * Sets a nickname for a user in a guild.
     *
     * @param guildId The server ID.
     * @param userId The user ID.
     * @param nickname The nickname to set. Null or empty string will reset the nickname.
     * @return true if the nickname was successfully set or reset, otherwise false.
     */
    public static boolean setNickname(long guildId, long userId, String nickname) {
        JDA bot = Bot.getInstance().getBot();
        try {
            Guild guild = bot.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("[NicknameManage] Server not found: " + guildId);
                return false;
            }

            if (!MemberManage.isMemberOfGuild(guildId, userId)) {
                plugin.getLogger().warning("[NicknameManage] Discord user not found: " + userId);
                return false;
            }

            Member member = guild.retrieveMemberById(userId).complete();

            // If nickname is null or empty, it will reset the nickname
            String action = (nickname == null || nickname.isEmpty()) ? "reset" : "set to '" + nickname + "'";

            guild.modifyNickname(member, nickname).queue(
                    success -> plugin.getLogger().info("[NicknameManage] Nickname for user " + userId + " " + action),
                    error -> plugin.getLogger().warning("[NicknameManage] Error while modifying nickname: " + error.getMessage())
            );

            return true;
        } catch (HierarchyException e) {
            plugin.getLogger().warning("[NicknameManage] Insufficient permissions to modify nickname: " + e.getMessage());
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("[NicknameManage] Unexpected error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Clears a user's nickname, resetting it to their username.
     *
     * @param guildId The server ID.
     * @param userId The user ID.
     * @return true if the nickname was successfully cleared, otherwise false.
     */
    public static boolean clearNickname(long guildId, long userId) {
        return setNickname(guildId, userId, null);
    }

    /**
     * Gets the current nickname of a user. Returns null if the user has no nickname.
     *
     * @param guildId The server ID.
     * @param userId The user ID.
     * @return The user's nickname, or null if they don't have one or if an error occurred.
     */
    public static String getNickname(long guildId, long userId) {
        JDA bot = Bot.getInstance().getBot();
        try {
            Guild guild = bot.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("[NicknameManage] Server not found: " + guildId);
                return null;
            }

            if (!MemberManage.isMemberOfGuild(guildId, userId)) {
                plugin.getLogger().warning("[NicknameManage] Discord user not found: " + userId);
                return null;
            }

            Member member = guild.retrieveMemberById(userId).complete();
            return member.getNickname();
        } catch (Exception e) {
            plugin.getLogger().warning("[NicknameManage] Error while getting nickname: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a user has a nickname in the given guild.
     *
     * @param guildId The server ID.
     * @param userId The user ID.
     * @return true if the user has a nickname, otherwise false.
     */
    public static boolean hasNickname(long guildId, long userId) {
        String nickname = getNickname(guildId, userId);
        return nickname != null && !nickname.isEmpty();
    }

    /**
     * Sets a user's nickname to match their Minecraft username if they are linked.
     * If the user is not linked to a Minecraft account, their nickname will be cleared.
     * Function accepts either Minecraft UUID or Discord ID.
     *
     * @param guildId The server ID.
     * @param value The player's UUID or Discord ID
     * @return true if the nickname was successfully synchronized or cleared, otherwise false.
     */
    public static boolean syncMinecraftNickname(long guildId, String value) {
        try {
            String playerUUID;
            long discordId;

            if (value.matches("\\d{17,}")) {
                discordId = Long.parseLong(value);

                if (!User.isDiscordIdLinked(connection, value)) {
                    plugin.getLogger().info("[NicknameManage] Discord user " + discordId + " is not linked to a Minecraft account. Clearing nickname.");
                    return clearNickname(guildId, discordId);
                }

                playerUUID = User.getLinkedUuid(connection, value);

                if (playerUUID == null || playerUUID.isEmpty()) {
                    plugin.getLogger().warning("[NicknameManage] No linked Minecraft account found for Discord user: " + discordId);
                    return clearNickname(guildId, discordId);
                }
            } else {
                playerUUID = value;
                String discordIdStr = User.getLinkedDiscordId(connection, playerUUID);

                if (discordIdStr == null || discordIdStr.isEmpty()) {
                    plugin.getLogger().warning("[NicknameManage] No linked Discord account found for Minecraft UUID: " + playerUUID);
                    return false;
                }

                discordId = Long.parseLong(discordIdStr);
            }

            String playerName = MinecraftAPI.getMinecraftUsername(playerUUID);

            if (playerName == null || playerName.isEmpty()) {
                plugin.getLogger().warning("[NicknameManage] Could not retrieve Minecraft username for UUID: " + playerUUID);
                return clearNickname(guildId, discordId);
            }

            plugin.getLogger().info("[NicknameManage] Syncing nickname for Discord user " + discordId + " to Minecraft name: " + playerName);
            return setNickname(guildId, discordId, playerName);
        } catch (Exception e) {
            plugin.getLogger().warning("[NicknameManage] Error while syncing Minecraft nickname: " + e.getMessage());
            return false;
        }
    }
}