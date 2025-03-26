package net.tokishu.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.tokishu.event.discord.bot.DM;
import net.tokishu.util.Base;
import org.bukkit.configuration.file.FileConfiguration;

public class Bot extends Base {

    private static Bot instance;
    private JDA bot;

    public Bot() {
        instance = this;
    }

    public static Bot getInstance() {
        return instance;
    }

    public void startBot() {
        FileConfiguration config = plugin.getConfig();
        String token = config.getString("discord-token");

        if (token == null || token.isEmpty()) {
            plugin.getLogger().severe("Discord token is missing! Check config.yml");
            return;
        }

        try {
            bot = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES)
                    .addEventListeners(new DiscordListener())
                    .build();

            plugin.getLogger().info("[Bot] Discord bot started!");
        } catch (Exception e) {
            plugin.getLogger().severe("[Bot] Failed to start Discord bot: " + e.getMessage());
        }
    }

    public void stopBot() {
        if (bot != null) {
            bot.shutdown();
            plugin.getLogger().info("[Bot] Discord bot stopped.");
        }
    }

    public void restartBot() {
        stopBot();
        startBot();
        plugin.getLogger().info("[Bot] Discord bot restarted!");
    }

    private static class DiscordListener extends ListenerAdapter {
        private DM dmHandler = new DM();

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;

            if (event.getChannel().getType().isMessage()) {
                dmHandler.handleDirectMessage(event);
                return;
            }

            if (event.getAuthor().isBot()) return;

            String message = event.getMessage().getContentRaw();
            if (message.equalsIgnoreCase("^ping")) {
                event.getChannel().sendMessage("Pong!").queue();
            }
        }
    }

    public JDA getBot() {
        return bot;
    }
}