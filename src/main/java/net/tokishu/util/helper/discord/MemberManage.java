package net.tokishu.util.helper.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.tokishu.bot.Bot;
import net.tokishu.util.Base;

public class MemberManage extends Base {

    /**
     * Checks if a user is a member of the guild.
     *
     * @param guildId The server ID (default main guild).
     * @param userId The user ID to check.
     * @return true if the user is a member of the guild, otherwise false.
     */
    public static boolean isMemberOfGuild(Long guildId, Long userId) {
        JDA bot = Bot.getInstance().getBot();
        try {
            if (guildId == null) {
                guildId = config.getLong("main-guild-id");
            }
            Guild guild = bot.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("[MemberManage] Server not found: " + guildId);
                return false;
            }

            if (userId == null) {
                plugin.getLogger().warning("[MemberManage] Provided userId is null.");
                return false;
            }


            try {
                Member member = guild.retrieveMemberById(userId).complete();
                return member != null;
            } catch (Exception e) {
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[MemberManage] Error while checking member: " + e.getMessage());
            return false;
        }
    }
}