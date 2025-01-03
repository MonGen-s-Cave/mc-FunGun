package hu.kxtsoo.fungun.events;

import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import static hu.kxtsoo.fungun.model.FunGunItem.isWorldDisabled;
import static org.bukkit.Bukkit.getLogger;

public class PlayerTeleportListener implements Listener {

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        int slot = FunGun.getInstance().getConfigUtil().getConfig().getInt("fungun.options.slot") - 1;
        if (slot <= 0 || slot > 9) {
            getLogger().warning("Invalid slot number in config.yml. Must be between 1 and 9.");
            return;
        }
        if(!isWorldDisabled(player.getWorld()))
            player.getInventory().setItem(slot, FunGunItem.createFunGunItem(FunGun.getInstance().getConfigUtil()));
    }

}
