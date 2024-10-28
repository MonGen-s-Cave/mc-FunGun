package hu.kxtsoo.fungun.events;

import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class PlayerSwapHandItemsListener implements Listener {

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (event.getOffHandItem() != null && FunGunItem.isFunGunItem(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }
}
