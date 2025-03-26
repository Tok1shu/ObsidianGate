package net.tokishu.util.helper.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.tokishu.bot.Bot;
import net.tokishu.util.Base;

public class RoleManage extends Base {

    /**
     * Adds a role to a user by role ID and user ID.
     *
     * @param guildId The server ID.
     * @param userId The user ID.
     * @param roleId The role ID.
     * @return true if the role was successfully added, otherwise false.
     */
    public static boolean addRoleToUser(long guildId, long userId, long roleId) {
        JDA bot = Bot.getInstance().getBot();
        try {
            Guild guild = bot.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("[RoleManage] Server not found: " + guildId);
                return false;
            }

            Member member = guild.getMemberById(userId);
            Role role = guild.getRoleById(roleId);

            if (member == null) {
                plugin.getLogger().warning("[RoleManage] Discord user not found: " + userId);
                return false;
            }

            if (role == null) {
                plugin.getLogger().warning("[RoleManage] Unexpected error: " + roleId);
                return false;
            }

            guild.addRoleToMember(member, role).queue(
                    success -> plugin.getLogger().info("[RoleManage] Role " + roleId + " has been added from user " + userId),
                    error -> plugin.getLogger().warning("[RoleManage] Error while adding role: " + error.getMessage())
            );

            return true;
        } catch (HierarchyException e) {
            plugin.getLogger().warning("[RoleManage] Недостаточно прав для выдачи роли: " + e.getMessage());
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("[RoleManage] Unexpected error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes a role from a user by role ID and user ID.
     *
     * @param guildId The server ID.
     * @param userId The user ID.
     * @param roleId The role ID.
     * @return true if the role was successfully removed, otherwise false.
     */
    public static boolean removeRoleFromUser(long guildId, long userId, long roleId) {
        JDA bot = Bot.getInstance().getBot();
        try {
            Guild guild = bot.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("[RoleManage] Server not found: " + guildId);
                return false;
            }

            Member member = guild.getMemberById(userId);
            Role role = guild.getRoleById(roleId);

            if (member == null) {
                plugin.getLogger().warning("[RoleManage] Discord user not found: " + userId);
                return false;
            }

            if (role == null) {
                plugin.getLogger().warning("[RoleManage] Role not found: " + roleId);
                return false;
            }

            guild.removeRoleFromMember(member, role).queue(
                    success -> plugin.getLogger().info("[RoleManage] Role " + roleId + " has been removed from user " + userId),
                    error -> plugin.getLogger().warning("[RoleManage] Error while removing role: " + error.getMessage())
            );

            return true;
        } catch (HierarchyException e) {
            plugin.getLogger().warning("[RoleManage] I do not have permission to assign the role! Please grant me permissions.: " + e.getMessage());
            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("[RoleManage] Unexpected error.: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a user has a specific role.
     *
     * @param guildId The server ID.
     * @param userId The user ID.
     * @param roleId The role ID.
     * @return true if the user has the role, otherwise false.
     */
    public static boolean hasRole(long guildId, long userId, long roleId) {
        JDA bot = Bot.getInstance().getBot();
        try {
            Guild guild = bot.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().warning("[RoleManage] Server not found: " + guildId);
                return false;
            }

            Member member = guild.getMemberById(userId);
            Role role = guild.getRoleById(roleId);

            if (member == null || role == null) {
                return false;
            }

            return member.getRoles().contains(role);
        } catch (Exception e) {
            plugin.getLogger().warning("[RoleManage] Error while checking role: " + e.getMessage());
            return false;
        }
    }
}