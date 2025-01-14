package hu.kxtsoo.fungun.guis;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.database.DatabaseManager;
import hu.kxtsoo.fungun.reflection.ClassUtils;
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

        String title = guis.getString("effects-menu.title", "&aFunGun Effects Menu");
        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            title = PlaceholderAPI.setPlaceholders(player, title);
        } else {
            title = guis.getString("effects-menu.title", "&aFunGun Effects Menu");
        }
        title = ChatUtil.colorizeHex(title);
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

        List<Integer> prevSlots = guis.getIntList("effects-menu.navigation.previous-page.slots");
        List<Integer> nextSlots = guis.getIntList("effects-menu.navigation.next-page.slots");

        Material prevMaterial = Material.valueOf(prevItem);
        Material nextMaterial = Material.valueOf(nextItem);

        String previousPageName = guis.getString("effects-menu.navigation.previous-page.name", "&aPrevious Page");
        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            previousPageName = PlaceholderAPI.setPlaceholders(player, previousPageName);
        } else {
            previousPageName = guis.getString("effects-menu.navigation.previous-page.name", "&aPrevious Page");
        }
        previousPageName = ChatUtil.colorizeHex(previousPageName);

        GuiItem previousPage = ItemBuilder.from(prevMaterial)
                .name(Component.text(previousPageName))
                .asGuiItem(event -> {
                    gui.previous();
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.navigation.previous-page.sound");

                    if (!soundName.isEmpty()) {
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

        String nextPageName = guis.getString("effects-menu.navigation.next-page.name", "&aNext Page");
        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            nextPageName = PlaceholderAPI.setPlaceholders(player, nextPageName);
        } else {
            nextPageName = guis.getString("effects-menu.navigation.next-page.name", "&aNext Page");
        }
        nextPageName = ChatUtil.colorizeHex(nextPageName);

        GuiItem nextPage = ItemBuilder.from(nextMaterial)
                .name(Component.text(nextPageName))
                .asGuiItem(event -> {
                    gui.next();
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.navigation.next-page.sound");

                    if (!soundName.isEmpty()) {
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

        for (int slot : prevSlots) {
            gui.setItem(slot, previousPage);
        }
        for (int slot : nextSlots) {
            gui.setItem(slot, nextPage);
        }

        List<Integer> closeSlots = guis.getIntList("effects-menu.close-item.slots");
        String closeItem = guis.getString("effects-menu.close-item.item", "BARRIER");
        Material closeMaterial = Material.valueOf(closeItem);

        String closeMenuName = guis.getString("effects-menu.close-item.name", "&cClose Menu");
        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            closeMenuName = PlaceholderAPI.setPlaceholders(player, closeMenuName);
        } else {
            closeMenuName = guis.getString("effects-menu.close-item.name", "&cClose Menu");
        }
        closeMenuName = ChatUtil.colorizeHex(closeMenuName);

        GuiItem closeMenu = ItemBuilder.from(closeMaterial)
                .name(Component.text(closeMenuName))
                .asGuiItem(event -> {
                    player.closeInventory();
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.close-item.sound");

                    if (!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1, 1);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("The sound is invalid in guis.yml (effects-menu/close-item)");
                        }
                    }
                    event.setCancelled(true);
                });

        for (int slot : closeSlots) {
            gui.setItem(slot, closeMenu);
        }


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

                String displayName = decoration.getString("title", "&r");
                if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                    displayName = PlaceholderAPI.setPlaceholders(player, displayName);
                } else {
                    displayName = decoration.getString("title", "&r");
                }
                displayName = ChatUtil.colorizeHex(displayName);

                List<String> lore = decoration.getStringList("lore").stream()
                        .map(line -> {
                            if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                                return PlaceholderAPI.setPlaceholders(player, line);
                            } else {
                                return ChatUtil.colorizeHex(line);
                            }
                        })
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

                String displayName = effectDoc.getString("effect.display.display-name", "&cUnknown Effect");
                if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                    displayName = PlaceholderAPI.setPlaceholders(player, displayName);
                } else {
                    displayName = effectDoc.getString("effect.display.display-name", "&cUnknown Effect");
                }

                displayName = ChatUtil.colorizeHex(displayName);

                List<String> descriptionList = effectDoc.getStringList("effect.display.description").stream()
                        .map(line -> {
                            if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                                return PlaceholderAPI.setPlaceholders(player, line);
                            } else {
                                return ChatUtil.colorizeHex(line);
                            }
                        })
                        .collect(Collectors.toList());

                List<String> lore = new ArrayList<>();
                String itemDisplayName = "";

                if (!player.hasPermission("fungun.effect." + effectKey)) {
                    itemDisplayName = guis.getString("effects-menu.item-template.no-permission.title", "%display-name%")
                            .replace("%display-name%", displayName);

                    List<String> loreTemplate = guis.getStringList("effects-menu.item-template.no-permission.description").stream()
                            .map(line -> {
                                if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                                    return PlaceholderAPI.setPlaceholders(player, line);
                                } else {
                                    return ChatUtil.colorizeHex(line);
                                }
                            })
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
                            .map(line -> {
                                if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                                    return PlaceholderAPI.setPlaceholders(player, line);
                                } else {
                                    return ChatUtil.colorizeHex(line);
                                }
                            })
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
                            .map(line -> {
                                if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                                    return PlaceholderAPI.setPlaceholders(player, line);
                                } else {
                                    return ChatUtil.colorizeHex(line);
                                }
                            })
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

                String finalDisplayName = displayName;
                GuiItem guiItem = ItemBuilder.from(displayItem)
                        .name(Component.text(itemDisplayName))
                        .lore(lore.stream().map(Component::text).collect(Collectors.toList()))
                        .asGuiItem(event -> {
                            try {
                                if (!player.hasPermission("fungun.effect." + effectKey)) {
                                    if (!configUtil.getMessage("messages.effects-menu.effect-no-permission").isEmpty()) {
                                        player.sendMessage(configUtil.getMessage("messages.effects-menu.effect-no-permission").replace("%effect%", finalDisplayName));
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
                                        player.sendMessage(configUtil.getMessage("messages.effects-menu.effect-already-selected").replace("%effect%", finalDisplayName));
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
                                        player.sendMessage(configUtil.getMessage("messages.effects-menu.effect-select").replace("%effect%", finalDisplayName));
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