package hu.kxtsoo.fungun.listener;

import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerDropItemListener implements Listener {

    @EventHandler
    public void onPlayerDropItem(@NotNull PlayerDropItemEvent event) {
        if (FunGunItem.isFunGunItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }
}