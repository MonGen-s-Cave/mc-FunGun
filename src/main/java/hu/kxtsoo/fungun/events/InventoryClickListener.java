package hu.kxtsoo.fungun.events;

import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack funGunItem = FunGunItem.createFunGunItem();

        if (player.getGameMode() != GameMode.SURVIVAL) return;

        if (isInPlayerOrCraftingInventory(event) && clickedItem != null && clickedItem.isSimilar(funGunItem)) {
            event.setCancelled(true);
        }

        if (event.getClick().equals(ClickType.NUMBER_KEY)) {
            ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
            if (hotbarItem != null && hotbarItem.isSimilar(funGunItem)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isInPlayerOrCraftingInventory(InventoryClickEvent event) {
        return event.getClickedInventory().getType() == InventoryType.CRAFTING ||
                event.getClickedInventory().getType() == InventoryType.PLAYER;
    }
}
