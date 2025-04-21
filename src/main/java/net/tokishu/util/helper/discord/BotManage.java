package net.tokishu.util.helper.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.tokishu.bot.Bot;
import net.tokishu.util.Base;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BotManage extends Base {

    private static String botTag = null;

    /**
     * Gets the bot's tag (username#discriminator).
     *
     * @return The bot's tag, or "Bot not initialized" if the bot isn't available.
     */
    public static String getBotTag() {
        if (botTag == null) {
            Bot bot = Bot.getInstance();
            if (bot != null && bot.getBot() != null) {
                botTag = bot.getBot().getSelfUser().getAsTag();
            }
        }
        return botTag != null ? botTag : "Bot not initialized";
    }

    /**
     * Gets the bot's ID.
     *
     * @return The bot's ID, or -1 if the bot isn't available.
     */
    public static long getBotId() {
        Bot bot = Bot.getInstance();
        if (bot != null && bot.getBot() != null) {
            return bot.getBot().getSelfUser().getIdLong();
        }
        return -1;
    }

    /**
     * Checks if the bot is a member of the specified guild.
     *
     * @param guildId The guild ID to check.
     * @return true if the bot is a member of the guild, otherwise false.
     */
    public static boolean isBotInGuild(long guildId) {
        JDA bot = Bot.getBot();
        try {
            Guild guild = bot.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("[BotManage] Server not found: " + guildId);
                return false;
            }

            plugin.getLogger().info("[BotManage] Bot is a member of guild " + guildId);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("[BotManage] Error while checking bot membership: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a map of all permission statuses for the bot in a specific guild.
     *
     * @param guildId The guild ID to check.
     * @return A map of Permission -> Boolean, or null if an error occurred.
     */
    public static Map<Permission, Boolean> getBotPermissions(long guildId) {
        JDA bot = Bot.getBot();
        Map<Permission, Boolean> permissionMap = new EnumMap<>(Permission.class);

        try {
            Guild guild = bot.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("[BotManage] Server not found: " + guildId);
                return null;
            }

            long botId = bot.getSelfUser().getIdLong();
            if (guild.getMemberById(botId) == null) {
                plugin.getLogger().warning("[BotManage] Bot is not a member of guild: " + guildId);
                return null;
            }

            // Initialize all permissions as false
            for (Permission permission : Permission.values()) {
                permissionMap.put(permission, false);
            }

            // Set the actual permissions
            for (Permission permission : guild.getMemberById(botId).getPermissions()) {
                permissionMap.put(permission, true);
            }

            return permissionMap;
        } catch (Exception e) {
            plugin.getLogger().warning("[BotManage] Error while retrieving bot permissions: " + e.getMessage());
            return null;
        }
    }

    /**
     * Checks if the bot has a specific permission in a guild.
     *
     * @param guildId The guild ID to check.
     * @param permission The permission to check.
     * @return true if the bot has the permission, otherwise false.
     */
    public static boolean hasPermission(long guildId, Permission permission) {
        JDA bot = Bot.getBot();
        try {
            Guild guild = bot.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("[BotManage] Server not found: " + guildId);
                return false;
            }

            long botId = bot.getSelfUser().getIdLong();
            if (guild.getMemberById(botId) == null) {
                plugin.getLogger().warning("[BotManage] Bot is not a member of guild: " + guildId);
                return false;
            }

            return guild.getMemberById(botId).hasPermission(permission);
        } catch (Exception e) {
            plugin.getLogger().warning("[BotManage] Error while checking permission: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets a list of guild IDs where the bot is present.
     *
     * @return A list of guild IDs, or empty list if an error occurred.
     */
    public static List<Long> getGuildsWithBot() {
        JDA bot = Bot.getBot();
        List<Long> guildIds = new ArrayList<>();

        try {
            for (Guild guild : bot.getGuilds()) {
                guildIds.add(guild.getIdLong());
            }

            plugin.getLogger().info("[BotManage] Bot is present in " + guildIds.size() + " guilds");
            return guildIds;
        } catch (Exception e) {
            plugin.getLogger().warning("[BotManage] Error while getting guild list: " + e.getMessage());
            return guildIds;
        }
    }

    /**
     * Gets the bot's name (without discriminator).
     *
     * @return The bot's name, or "Unknown" if the bot isn't available.
     */
    public static String getBotName() {
        Bot bot = Bot.getInstance();
        if (bot != null && bot.getBot() != null) {
            return bot.getBot().getSelfUser().getName();
        }
        return "Unknown";
    }

    /**
     * Gets the bot's avatar URL.
     *
     * @return The URL of the bot's avatar, or null if the bot isn't available.
     */
    public static String getBotAvatarUrl() {
        Bot bot = Bot.getInstance();
        if (bot != null && bot.getBot() != null) {
            SelfUser selfUser = bot.getBot().getSelfUser();
            return selfUser.getEffectiveAvatarUrl();
        }
        return null;
    }

    /**
     * Checks if the bot has administrative permissions in a guild.
     *
     * @param guildId The guild ID to check.
     * @return true if the bot has ADMINISTRATOR permission, otherwise false.
     */
    public static boolean isAdmin(long guildId) {
        return hasPermission(guildId, Permission.ADMINISTRATOR);
    }

    /**
     * Gets the number of guilds the bot is in.
     *
     * @return The number of guilds, or 0 if an error occurred.
     */
    public static int getGuildCount() {
        JDA bot = Bot.getBot();
        try {
            return bot.getGuilds().size();
        } catch (Exception e) {
            plugin.getLogger().warning("[BotManage] Error while getting guild count: " + e.getMessage());
            return 0;
        }
    }
}