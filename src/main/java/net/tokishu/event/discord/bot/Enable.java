package net.tokishu.event.discord.bot;

import net.tokishu.bot.Bot;
import net.tokishu.util.Base;

public class Enable extends Base {
    public Enable() {
        if (!isBotInGuild(config.getString("main-guild-id"))){
            plugin.getLogger().severe("The bot is not on the main server!\n Invite the bot to the main server!");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    private boolean isBotInGuild(String guildId) {
        return Bot.getInstance().getBot().getGuildById(guildId) != null;
    }

}
