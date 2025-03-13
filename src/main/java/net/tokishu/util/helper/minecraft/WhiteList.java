package net.tokishu.util.helper.minecraft;

import net.tokishu.util.Base;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WhiteList extends Base {

    public void addToWhitelist(Player p){
        if (isVanillaWhitelist()){
            p.setWhitelisted(true);
        }else{
            try {
                String command = config.getString("whitelist.add-command").replace("{username}", p.getName());
                Boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                if (!success) {
                    throw new Exception();
                }
            } catch (Exception e) {
                plugin.getLogger().severe("[WhiteList] Error while adding player " + p.getName() + " to the custom whitelist \n Please check your config.yml");
            }
        }
    }

    public void removeFromWhitelist(Player p){
        if (isVanillaWhitelist()){
            p.setWhitelisted(false);
        }else{
            try {
                String command = config.getString("whitelist.remove-command").replace("{username}", p.getName());
                Boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

                if (!success) {
                    throw new Exception();
                }
            } catch (Exception e) {
                plugin.getLogger().severe("[WhiteList] Error while adding player " + p.getName() + " to the custom whitelist \n Please check your config.yml");
            }
        }
    }

    private boolean isVanillaWhitelist() {
        return "vanilla".equals(config.getString("whitelist.type"));
    }
}
