package hu.kxtsoo.fungun.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.entity.FallingBlock;

public class FallingBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFallingBlockChange(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            event.setCancelled(true);
        }
    }
}
