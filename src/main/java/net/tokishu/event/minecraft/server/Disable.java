package net.tokishu.event.minecraft.server;

import net.tokishu.bot.Bot;
import net.tokishu.util.Base;

public class Disable extends Base {

    public Disable() {
        shutdown();
        new Bot().stopBot();
    }

    private void shutdown(){
        plugin.getLogger().info("Bye :3");
    }
}
