package hu.kxtsoo.fungun.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.entity.FallingBlock;

public class FallingBlockListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFallingBlockChange(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            if (fallingBlock.hasMetadata("fungun_visual")) event.setCancelled(true);
        }
    }
}