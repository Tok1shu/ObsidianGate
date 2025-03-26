package net.tokishu.util.helper.minecraft;

import org.bukkit.entity.Player;

public class LP {
    public static boolean checkPlayerPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }
}
