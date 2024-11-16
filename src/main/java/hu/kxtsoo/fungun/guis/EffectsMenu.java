package hu.kxtsoo.fungun.guis;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.database.DatabaseManager;
import hu.kxtsoo.fungun.util.ChatUtil;
import hu.kxtsoo.fungun.util.ConfigUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getLogger;

public class EffectsMenu {

    public void openMenu(Player player) throws SQLException {
        ConfigUtil configUtil = FunGun.getInstance().getConfigUtil();
        var guis = configUtil.getGUIs();

        String title = ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, guis.getString("effects-menu.title", "&aFunGun Effects Menu")));
        int rows = guis.getInt("effects-menu.rows", 3);

        int totalSlots = rows * 9;
        int maxEffectSlot = guis.getInt("effects-menu.max-item-slot", totalSlots - 1);

        PaginatedGui gui = Gui.paginated()
                .title(Component.text(title))
                .rows(rows)
                .pageSize(maxEffectSlot)
                .create();

        List<String> effectKeys = new ArrayList<>(configUtil.getEffectsMap().keySet());

        String prevItem = guis.getString("effects-menu.navigation.previous-page.item", "ARROW");
        String nextItem = guis.getString("effects-menu.navigation.next-page.item", "ARROW");

        int prevSlot = guis.getInt("effects-menu.navigation.previous-page.slot", 19);
        int nextSlot = guis.getInt("effects-menu.navigation.next-page.slot", 25);

        Material prevMaterial = Material.valueOf(prevItem);
        Material nextMaterial = Material.valueOf(nextItem);

        GuiItem previousPage = ItemBuilder.from(prevMaterial)
                .name(Component.text(ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, guis.getString("effects-menu.navigation.previous-page.name", "&aPrevious Page")))))
                .asGuiItem(event -> {
                    gui.previous();
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.navigation.previous-page.sound");

                    if(!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1, 1);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("The sound is invalid in guis.yml (effects-menu/navigation/previous-page)");
                        }
                    }
                    gui.update();
                    event.setCancelled(true);
                });

        GuiItem nextPage = ItemBuilder.from(nextMaterial)
                .name(Component.text(ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, guis.getString("effects-menu.navigation.next-page.name", "&aNext Page")))))
                .asGuiItem(event -> {
                    gui.next();
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.navigation.next-page.sound");

                    if(!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1, 1);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("The sound is invalid in guis.yml (effects-menu/navigation/next-page)");
                        }
                    }
                    gui.update();
                    event.setCancelled(true);
                });

        gui.setItem(prevSlot, previousPage);
        gui.setItem(nextSlot, nextPage);

        int closeSlot = guis.getInt("effects-menu.close-item.slot", 22);
        String closeItem = guis.getString("effects-menu.close-item.item", "BARRIER");
        Material closeMaterial = Material.valueOf(closeItem);

        GuiItem closeMenu = ItemBuilder.from(closeMaterial)
                .name(Component.text(ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, guis.getString("effects-menu.close-item.name", "&cClose Menu")))))
                .asGuiItem(event -> {
                    player.closeInventory();
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.close-item.sound");

                    if(!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1, 1);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("The sound is invalid in guis.yml (effects-menu/close-item)");
                        }
                    }
                    event.setCancelled(true);
                });

        gui.setItem(closeSlot, closeMenu);


        var decorations = guis.getSection("effects-menu.decoration");
        if (decorations != null) {
            for (String key : decorations.getRoutesAsStrings(false)) {
                var decoration = decorations.getSection(key);

                String displayItemStr = decoration.getString("item", "STONE");
                Material displayItem;
                try {
                    displayItem = Material.valueOf(displayItemStr);
                } catch (IllegalArgumentException e) {
                    displayItem = Material.BEDROCK;
                }

                String displayName = ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, decoration.getString("title", "&r")));
                List<String> lore = decoration.getStringList("lore").stream()
                        .map(line -> ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, line)))
                        .collect(Collectors.toList());

                int slot = decoration.getInt("slot", 0);

                GuiItem decorativeItem = ItemBuilder.from(displayItem)
                        .name(Component.text(displayName))
                        .lore(lore.stream().map(Component::text).collect(Collectors.toList()))
                        .asGuiItem(event -> event.setCancelled(true));

                if (decoration.contains("custommodeldata")) {
                    int customModelData = decoration.getInt("custommodeldata");
                    ItemMeta meta = decorativeItem.getItemStack().getItemMeta();
                    if (meta != null) {
                        meta.setCustomModelData(customModelData);
                        decorativeItem.getItemStack().setItemMeta(meta);
                    }
                }

                gui.setItem(slot, decorativeItem);
            }
        }

        for (String effectKey : effectKeys) {
            var effectDoc = configUtil.getEffect(effectKey);

            if (effectDoc.getBoolean("effect.enabled", true)) {
                String displayItemStr = effectDoc.getString("effect.display.display-item", "BLAZE_ROD");
                Material displayItem;
                try {
                    displayItem = Material.valueOf(displayItemStr);
                } catch (IllegalArgumentException e) {
                    displayItem = Material.BEDROCK;
                }

                String displayName = ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, effectDoc.getString("effect.display.display-name", "&cUnknown Effect")));
                List<String> descriptionList = effectDoc.getStringList("effect.display.description").stream()
                        .map(line -> ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, line)))
                        .collect(Collectors.toList());

                List<String> lore = new ArrayList<>();
                String itemDisplayName = "";

                if (!player.hasPermission("fungun.effect." + effectKey)) {
                    itemDisplayName = guis.getString("effects-menu.item-template.no-permission.title", "%display-name%")
                            .replace("%display-name%", displayName);

                    List<String> loreTemplate = guis.getStringList("effects-menu.item-template.no-permission.description").stream()
                            .map(line -> ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, line)))
                            .collect(Collectors.toList());
                    for (String line : loreTemplate) {
                        if (line.contains("%description%")) {
                            for (String descLine : descriptionList) {
                                lore.add(ChatUtil.colorizeHex(line.replace("%description%", descLine.trim())));
                            }
                        } else {
                            lore.add(ChatUtil.colorizeHex(line));
                        }
                    }

                } else if (DatabaseManager.isEffectSelected(player.getUniqueId().toString(), effectKey)) {
                    itemDisplayName = guis.getString("effects-menu.item-template.selected.title", "%display-name%")
                            .replace("%display-name%", displayName);

                    List<String> loreTemplate = guis.getStringList("effects-menu.item-template.selected.description").stream()
                            .map(line -> ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, line)))
                            .collect(Collectors.toList());
                    for (String line : loreTemplate) {
                        if (line.contains("%description%")) {
                            for (String descLine : descriptionList) {
                                lore.add(ChatUtil.colorizeHex(line.replace("%description%", descLine.trim())));
                            }
                        } else {
                            lore.add(ChatUtil.colorizeHex(line));
                        }
                    }

                } else {
                    itemDisplayName = guis.getString("effects-menu.item-template.unselected.title", "%display-name%")
                            .replace("%display-name%", displayName);

                    List<String> loreTemplate = guis.getStringList("effects-menu.item-template.unselected.description").stream()
                            .map(line -> ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, line)))
                            .collect(Collectors.toList());
                    for (String line : loreTemplate) {
                        if (line.contains("%description%")) {
                            for (String descLine : descriptionList) {
                                lore.add(ChatUtil.colorizeHex(line.replace("%description%", descLine.trim())));
                            }
                        } else {
                            lore.add(ChatUtil.colorizeHex(line));
                        }
                    }
                }

                GuiItem guiItem = ItemBuilder.from(displayItem)
                        .name(Component.text(itemDisplayName))
                        .lore(lore.stream().map(Component::text).collect(Collectors.toList()))
                        .asGuiItem(event -> {
                            try {
                                if (!player.hasPermission("fungun.effect." + effectKey)) {
                                    if (!configUtil.getMessage("messages.effects-menu.effect-no-permission").isEmpty()) {
                                        player.sendMessage(configUtil.getMessage("messages.effects-menu.effect-no-permission").replace("%effect%", displayName));
                                    }

                                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.item-template.no-permission.sound");

                                    if(!soundName.isEmpty()) {
                                        try {
                                            Sound sound = Sound.valueOf(soundName);
                                            player.playSound(player.getLocation(), sound, 1, 1);
                                        } catch (IllegalArgumentException e) {
                                            getLogger().warning("The sound is invalid in guis.yml (effects-menu/item-template/no-permission)");
                                        }
                                    }
                                }

                                else if (DatabaseManager.isEffectSelected(player.getUniqueId().toString(), effectKey)) {
                                    if (!configUtil.getMessage("messages.effects-menu.effect-already-selected").isEmpty()) {
                                        player.sendMessage(configUtil.getMessage("messages.effects-menu.effect-already-selected").replace("%effect%", displayName));
                                    }
                                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.item-template.selected.sound");

                                    if(!soundName.isEmpty()) {
                                        try {
                                            Sound sound = Sound.valueOf(soundName);
                                            player.playSound(player.getLocation(), sound, 1, 1);
                                        } catch (IllegalArgumentException e) {
                                            getLogger().warning("The sound is invalid in guis.yml (effects-menu/item-template/selected)");
                                        }
                                    }
                                }

                                else {
                                    DatabaseManager.saveSelectedEffect(player.getUniqueId().toString(), effectKey);

                                    if (!configUtil.getMessage("messages.effects-menu.effect-select").isEmpty()) {
                                        player.sendMessage(configUtil.getMessage("messages.effects-menu.effect-select").replace("%effect%", displayName));
                                    }

                                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.item-template.unselected.sound");

                                    if(!soundName.isEmpty()) {
                                        try {
                                            Sound sound = Sound.valueOf(soundName);
                                            player.playSound(player.getLocation(), sound, 1, 1);
                                        } catch (IllegalArgumentException e) {
                                            getLogger().warning("The sound is invalid in guis.yml (effects-menu/item-template/unselected)");
                                        }
                                    }

                                    openMenu(player);
                                }
                            } catch (SQLException e) {
                                configUtil.getMessage("messages.database-error");
                            }
                            event.setCancelled(true);
                        });

                gui.addItem(guiItem);
            }
        }

        gui.open(player);
    }
}