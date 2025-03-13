package net.tokishu.event.minecraft.server;

import net.tokishu.bot.Main;
import net.tokishu.util.Base;

public class Disable extends Base {

    public Disable() {
        shutdown();
        new Main().stopBot();
    }

    private void shutdown(){
        plugin.getLogger().info("Bye :3");
    }
}
