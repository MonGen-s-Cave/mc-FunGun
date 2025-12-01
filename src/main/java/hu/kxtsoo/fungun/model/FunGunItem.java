package hu.kxtsoo.fungun.model;

import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.util.ChatUtil;
import hu.kxtsoo.fungun.util.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public final class FunGunItem {
    private static final NamespacedKey KEY = new NamespacedKey(FunGun.getInstance(), "fungun-id");
    private static final String VALUE = "FUN_GUN";

    @NotNull
    public static ItemStack createFunGunItem(@NotNull ConfigUtil configUtil) {
        Material itemType = Material.getMaterial(configUtil.getConfig().getString("fungun.item", "BLAZE_ROD"));
        String itemName = ChatUtil.colorizeHex(configUtil.getConfig().getString("fungun.name"));
        List<String> itemLore = configUtil.getConfig().getStringList("fungun.lore");
        int customModelData = configUtil.getConfig().getInt("fungun.custommodeldata");

        if (itemType == null) {
            itemType = Material.BLAZE_ROD;
        }

        ItemStack item = new ItemStack(itemType);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(itemName);
            meta.setLore(itemLore.stream().map(ChatUtil::colorizeHex).collect(Collectors.toList()));
            meta.setCustomModelData(customModelData);

            meta.getPersistentDataContainer().set(KEY, PersistentDataType.STRING, VALUE);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static boolean isFunGunItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        String stored = meta.getPersistentDataContainer().get(KEY, PersistentDataType.STRING);
        return VALUE.equals(stored);
    }

    public static boolean isWorldDisabled(@NotNull World world) {
        return FunGun.getInstance()
                .getConfigUtil()
                .getConfig()
                .getStringList("fungun.options.disabled-worlds")
                .contains(world.getName());
    }
}