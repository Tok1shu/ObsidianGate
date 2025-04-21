
// CLASS FOR TESTS!

package net.tokishu.command.minecraft;

import net.tokishu.util.Base;
import net.tokishu.util.helper.discord.BotManage;
import net.tokishu.util.helper.discord.MemberManage;
import org.bukkit.command.CommandSender;

public class Test extends Base {
    public static void test(CommandSender sender) {
        sender.sendMessage("Test ALL new helpers");
        sender.sendMessage("Bot Name " + BotManage.getBotName());
        sender.sendMessage("Bot Tag " + BotManage.getBotTag());
        sender.sendMessage("Bot ID " + BotManage.getBotId());
        sender.sendMessage("Is Bot in guild (main) " + BotManage.isBotInGuild(1270456750239383644L));
        sender.sendMessage("Bot Permissions " + BotManage.getBotPermissions(1270456750239383644L));
        sender.sendMessage("Bot isAdmin " + BotManage.isAdmin(1270456750239383644L));
        sender.sendMessage("Bot Guild ID count " + BotManage.getGuildCount());

        sender.sendMessage("Is member of guild " + MemberManage.isMemberOfGuild(null, 761175017588916224L));
    }

}
