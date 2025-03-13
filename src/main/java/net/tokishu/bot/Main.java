package net.tokishu.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.tokishu.util.Base;

public class Main extends Base {

    private JDA bot;

    public void startBot() {
        String token = config.getString("discord-token");

        if (token == null || token.isEmpty()) {
            plugin.getLogger().severe("Discord token is missing! Check config.yml");
            return;
        }

        try {
            bot = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new DiscordListener())
                    .build();
            plugin.getLogger().info("Discord bot started!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start Discord bot: " + e.getMessage());
        }
    }

    public void stopBot() {
        if (bot != null) {
            bot.shutdown();
            plugin.getLogger().info("Discord bot stopped.");
        }
    }

    private static class DiscordListener extends ListenerAdapter {
        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;

            String message = event.getMessage().getContentRaw();
            if (message.equalsIgnoreCase("!ping")) {
                event.getChannel().sendMessage("Pong!").queue();
            }
        }
    }
}
