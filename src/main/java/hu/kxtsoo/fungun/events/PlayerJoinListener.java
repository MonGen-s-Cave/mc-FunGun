package hu.kxtsoo.fungun.events;

import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import static hu.kxtsoo.fungun.model.FunGunItem.isWorldDisabled;
import static org.bukkit.Bukkit.getLogger;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!FunGun.getInstance().getConfigUtil().getConfig().getBoolean("fungun.options.give-on-join", true)) return;

        for(String world : FunGun.getInstance().getConfigUtil().getConfig().getStringList("fungun.options.disabled-worlds")){
            if(world.equals(player.getWorld().getName()))
                return;
        }

        int slot = FunGun.getInstance().getConfigUtil().getConfig().getInt("fungun.options.slot") - 1;
        if (slot >= 0 && slot < 9) {
            if(!isWorldDisabled(player.getWorld()))
                player.getInventory().setItem(slot, FunGunItem.createFunGunItem(FunGun.getInstance().getConfigUtil()));
        } else {
            getLogger().warning("Invalid slot number in config.yml. Must be between 1 and 9.");
        }
    }
}
