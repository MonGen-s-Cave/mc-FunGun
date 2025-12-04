package hu.kxtsoo.fungun.listener;

import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!FunGun.getInstance().getConfigUtil().getConfig().getBoolean("fungun.options.give-on-join", true)) {
            return;
        }

        int slot = FunGun.getInstance().getConfigUtil().getConfig().getInt("fungun.options.slot") - 1;
        if (slot < 0 || slot > 8) {
            return;
        }

        if (FunGunItem.canHaveFunGun(player.getWorld())) {
            player.getInventory().setItem(slot, FunGunItem.createFunGunItem(FunGun.getInstance().getConfigUtil()));
        }
    }
}