package hu.kxtsoo.fungun.model;

import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.util.ChatUtil;
import hu.kxtsoo.fungun.util.ConfigUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FunGunItem {

    public static ItemStack createFunGunItem(ConfigUtil configUtil) {
        Material itemType = Material.getMaterial(configUtil.getConfig().getString("fungun.item", "BLAZE_ROD"));
        String itemName = ChatUtil.colorizeHex(configUtil.getConfig().getString("fungun.name"));
        List<String> itemLore = configUtil.getConfig().getStringList("fungun.lore");
        int customModelData = configUtil.getConfig().getInt("fungun.custommodeldata");

        assert itemType != null;
        ItemStack item = new ItemStack(itemType);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(itemName);
            meta.setLore(itemLore.stream().map(ChatUtil::colorizeHex).collect(Collectors.toList()));
            meta.setCustomModelData(customModelData);
            item.setItemMeta(meta);
        }

        return item;
    }

    public static boolean isFunGunItem(ItemStack item, ConfigUtil configUtil) {
        ItemStack funGunItem = createFunGunItem(configUtil);
        if (item == null || !item.hasItemMeta() || !funGunItem.hasItemMeta()) {
            return false;
        }

        ItemMeta itemMeta = item.getItemMeta();
        ItemMeta funMeta = funGunItem.getItemMeta();

        return item.getType() == funGunItem.getType()
                && Objects.equals(itemMeta.getDisplayName(), funMeta.getDisplayName())
                && Objects.equals(itemMeta.getLore(), funMeta.getLore());
    }

    public static boolean isWorldDisabled(World world){
        return FunGun.getInstance().getConfigUtil().getConfig().getStringList("fungun.options.disabled-worlds").contains(world.getName());
    }
}