package net.tokishu.util.helper.config;

import net.tokishu.util.Base;
import org.bukkit.Bukkit;

public class OnlineModeCheck extends Base {
    /**
     * Checks if the server is running in online mode.
     *
     * @return true if the server is in online mode, false otherwise
     */
    public boolean isOnlineMode() {
        return Bukkit.getServer().getOnlineMode();
    }
}