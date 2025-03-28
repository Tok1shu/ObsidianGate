package net.tokishu.event.minecraft.server;

import net.tokishu.bot.Bot;
import net.tokishu.util.Base;
import net.tokishu.util.helper.database.Manager;

public class Disable extends Base {

    public Disable() {
        shutdown();
    }

    private void shutdown(){
        // new Bot().stopBot(); // - WORKING BAD
        plugin.getLogger().info("Bye :3");
        Manager.close();
    }
}
