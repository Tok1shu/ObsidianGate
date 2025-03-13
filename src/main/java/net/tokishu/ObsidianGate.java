package net.tokishu;

import net.tokishu.event.minecraft.server.Disable;
import net.tokishu.event.minecraft.server.Enable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

public final class ObsidianGate extends JavaPlugin {
    private static ObsidianGate instance;
    @Override
    public void onEnable() {
        instance = this;
        new Enable();
    }

    @Override
    public void onDisable() {
        new Disable();
    }

    public static ObsidianGate getInstance() {return instance;}
    public static FileConfiguration getPluginConfig() {return getInstance().getConfig();}
}