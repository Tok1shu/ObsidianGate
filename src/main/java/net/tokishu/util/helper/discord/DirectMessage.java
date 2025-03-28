package net.tokishu.util.helper.discord;

import net.dv8tion.jda.api.entities.User;
import net.tokishu.bot.Bot;
import net.dv8tion.jda.api.JDA;

public class DirectMessage extends Bot {

    /**
     * Sends a direct message to a specified Discord user.
     *
     * @param userId The unique Discord user ID to send the message to
     * @param message The text message to be sent
     * @return boolean indicating whether the message was successfully sent
     */
    public static boolean send(String userId, String message) {
        try {
            JDA bot = getBot();
            if (bot == null) {
                return false;
            }
            User user = bot.retrieveUserById(userId).complete();
            if (user == null) {
                return false;
            }
            user.openPrivateChannel().complete().sendMessage(message).queue();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}