package net.tokishu.util;

import net.tokishu.ObsidianGate;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class Base {
    protected static final ObsidianGate plugin = ObsidianGate.getInstance();
    protected static final FileConfiguration config = ObsidianGate.getPluginConfig();
    protected static final File sqliteFile = new File(plugin.getDataFolder(), config.getString("database.sqlite-path", "obsidiangate.db"));
}
