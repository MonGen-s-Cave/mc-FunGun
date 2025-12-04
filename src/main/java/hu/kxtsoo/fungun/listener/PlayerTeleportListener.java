package hu.kxtsoo.fungun.listener;

import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements Listener {

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        int slot = FunGun.getInstance().getConfigUtil().getConfig().getInt("fungun.options.slot") - 1;

        if (slot < 0 || slot > 8) return;

        if (FunGunItem.canHaveFunGun(event.getTo().getWorld())) {
            player.getInventory().setItem(slot, FunGunItem.createFunGunItem(FunGun.getInstance().getConfigUtil()));
        } else {
            player.getInventory().setItem(slot, null);
        }
    }
}