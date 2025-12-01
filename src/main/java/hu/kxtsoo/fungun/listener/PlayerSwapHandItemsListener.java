package hu.kxtsoo.fungun.listener;

import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerSwapHandItemsListener implements Listener {

    @EventHandler
    public void onPlayerSwapHandItems(@NotNull PlayerSwapHandItemsEvent event) {
        if (event.getOffHandItem() != null && FunGunItem.isFunGunItem(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }
}