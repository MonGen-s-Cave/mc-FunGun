package hu.kxtsoo.fungun.events;

import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (FunGunItem.isFunGunItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }
}
