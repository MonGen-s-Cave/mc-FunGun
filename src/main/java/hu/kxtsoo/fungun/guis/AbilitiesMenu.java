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

public class AbilitiesMenu {

    public void openMenu(Player player) throws SQLException {
        ConfigUtil configUtil = FunGun.getInstance().getConfigUtil();
        var guis = configUtil.getGUIs();

        String title = guis.getString("abilities-menu.title", "&aFunGun Abilities Menu");

        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            title = PlaceholderAPI.setPlaceholders(player, title);
        } else {
            title = guis.getString("abilities-menu.title", "&aFunGun Abilities Menu");
        }

        title = ChatUtil.colorizeHex(title);
        int rows = guis.getInt("abilities-menu.rows", 3);

        int totalSlots = rows * 9;
        int maxAbilitySlot = guis.getInt("abilities-menu.max-item-slot", totalSlots - 1);

        PaginatedGui gui = Gui.paginated()
                .title(Component.text(title))
                .rows(rows)
                .pageSize(maxAbilitySlot)
                .create();

        List<String> abilityKeys = new ArrayList<>(configUtil.getAbilitiesMap().keySet());

        String prevItem = guis.getString("abilities-menu.navigation.previous-page.item", "ARROW");
        String nextItem = guis.getString("abilities-menu.navigation.next-page.item", "ARROW");

        List<Integer> prevSlots = guis.getIntList("abilities-menu.navigation.previous-page.slots");
        List<Integer> nextSlots = guis.getIntList("abilities-menu.navigation.next-page.slots");

        Material prevMaterial = Material.valueOf(prevItem);
        Material nextMaterial = Material.valueOf(nextItem);

        String previousPageName = guis.getString("abilities-menu.navigation.previous-page.name", "&aPrevious Page");

        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            previousPageName = PlaceholderAPI.setPlaceholders(player, previousPageName);
        } else {
            previousPageName = guis.getString("abilities-menu.navigation.previous-page.name", "&aPrevious Page");
        }

        previousPageName = ChatUtil.colorizeHex(previousPageName);

        GuiItem previousPage = ItemBuilder.from(prevMaterial)
                .name(Component.text(previousPageName))
                .asGuiItem(event -> {
                    gui.previous();
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("abilities-menu.navigation.previous-page.sound");

                    if (!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1, 1);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("The sound is invalid in guis.yml (abilities-menu/navigation/previous-page)");
                        }
                    }
                    gui.update();
                    event.setCancelled(true);
                });

        if (guis.contains("abilities-menu.navigation.previous-page.custom-model-data")) {
            int customModelData = guis.getInt("abilities-menu.navigation.previous-page.custom-model-data");
            ItemMeta meta = previousPage.getItemStack().getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(customModelData);
                previousPage.getItemStack().setItemMeta(meta);
            }
        }

        String nextPageName = guis.getString("abilities-menu.navigation.next-page.name", "&aNext Page");

        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            nextPageName = PlaceholderAPI.setPlaceholders(player, nextPageName);
        } else {
            nextPageName = guis.getString("abilities-menu.navigation.next-page.name", "&aNext Page");
        }

        nextPageName = ChatUtil.colorizeHex(nextPageName);

        GuiItem nextPage = ItemBuilder.from(nextMaterial)
                .name(Component.text(nextPageName))
                .asGuiItem(event -> {
                    gui.next();
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("abilities-menu.navigation.next-page.sound");

                    if (!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1, 1);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("The sound is invalid in guis.yml (abilities-menu/navigation/next-page)");
                        }
                    }
                    gui.update();
                    event.setCancelled(true);
                });

        if (guis.contains("abilities-menu.navigation.next-page.custom-model-data")) {
            int customModelData = guis.getInt("abilities-menu.navigation.next-page.custom-model-data");
            ItemMeta meta = nextPage.getItemStack().getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(customModelData);
                nextPage.getItemStack().setItemMeta(meta);
            }
        }

        for (int slot : prevSlots) {
            gui.setItem(slot, previousPage);
        }
        for (int slot : nextSlots) {
            gui.setItem(slot, nextPage);
        }

        List<Integer> closeSlots = guis.getIntList("abilities-menu.close-item.slots");
        String closeItem = guis.getString("abilities-menu.close-item.item", "BARRIER");
        Material closeMaterial = Material.valueOf(closeItem);

        String closeMenuName = guis.getString("abilities-menu.close-item.name", "&cClose Menu");

        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            closeMenuName = PlaceholderAPI.setPlaceholders(player, closeMenuName);
        } else {
            closeMenuName = guis.getString("abilities-menu.close-item.name", "&cClose Menu");
        }

        closeMenuName = ChatUtil.colorizeHex(closeMenuName);

        GuiItem closeMenu = ItemBuilder.from(closeMaterial)
                .name(Component.text(closeMenuName))
                .asGuiItem(event -> {
                    player.closeInventory();
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("abilities-menu.close-item.sound");

                    if (!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1, 1);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("The sound is invalid in guis.yml (abilities-menu/close-item)");
                        }
                    }
                    event.setCancelled(true);
                });

        if (guis.contains("abilities-menu.close-item.custom-model-data")) {
            int customModelData = guis.getInt("abilities-menu.close-item.custom-model-data");
            ItemMeta meta = closeMenu.getItemStack().getItemMeta();
            if (meta != null) {
                meta.setCustomModelData(customModelData);
                closeMenu.getItemStack().setItemMeta(meta);
            }
        }

        for (int slot : closeSlots) {
            gui.setItem(slot, closeMenu);
        }

        var decorations = guis.getSection("abilities-menu.decoration");
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
                                return ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, line));
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

        for (String abilityKey : abilityKeys) {
            var abilityDoc = configUtil.getAbility(abilityKey);

            if (abilityDoc.getBoolean("ability.enabled", true)) {
                String displayItemStr = abilityDoc.getString("ability.display.display-item", "IRON_SWORD");
                Material displayItem;
                try {
                    displayItem = Material.valueOf(displayItemStr);
                } catch (IllegalArgumentException e) {
                    displayItem = Material.BEDROCK;
                }

                String displayName = abilityDoc.getString("ability.display.display-name", "&cUnknown Ability");

                if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                    displayName = PlaceholderAPI.setPlaceholders(player, displayName);
                } else {
                    displayName = abilityDoc.getString("ability.display.display-name", "&cUnknown Ability");
                }

                displayName = ChatUtil.colorizeHex(displayName);

                List<String> descriptionList = abilityDoc.getStringList("ability.display.description").stream()
                        .map(line -> {
                            if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                                return ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, line));
                            } else {
                                return ChatUtil.colorizeHex(line);
                            }
                        })
                        .collect(Collectors.toList());

                List<String> lore = new ArrayList<>();
                String itemDisplayName = "";

                if (!player.hasPermission("fungun.ability." + abilityKey)) {
                    itemDisplayName = guis.getString("abilities-menu.item-template.no-permission.title", "%display-name%")
                            .replace("%display-name%", displayName);

                    List<String> loreTemplate = guis.getStringList("abilities-menu.item-template.no-permission.description").stream()
                            .map(line -> {
                                if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                                    return ChatUtil.colorizeHex(PlaceholderAPI.setPlaceholders(player, line));
                                } else {
                                    return line;
                                }

                            })
                            .map(ChatUtil::colorizeHex)
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

                } else if (DatabaseManager.isAbilitySelected(player.getUniqueId().toString(), abilityKey)) {
                    itemDisplayName = guis.getString("abilities-menu.item-template.selected.title", "%display-name%")
                            .replace("%display-name%", displayName);

                    List<String> loreTemplate = guis.getStringList("abilities-menu.item-template.selected.description").stream()
                            .map(line -> {
                                if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                                    return PlaceholderAPI.setPlaceholders(player, line);
                                } else {
                                    return line;
                                }
                            })
                            .map(ChatUtil::colorizeHex)
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
                    itemDisplayName = guis.getString("abilities-menu.item-template.unselected.title", "%display-name%")
                            .replace("%display-name%", displayName);

                    List<String> loreTemplate = guis.getStringList("abilities-menu.item-template.unselected.description").stream()
                            .map(line -> {
                                if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                                    return PlaceholderAPI.setPlaceholders(player, line);
                                } else {
                                    return line;
                                }
                            })
                            .map(ChatUtil::colorizeHex)
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
                                if (!player.hasPermission("fungun.ability." + abilityKey)) {
                                    if (!configUtil.getMessage("messages.abilities-menu.ability-no-permission").isEmpty()) {
                                        player.sendMessage(configUtil.getMessage("messages.abilities-menu.ability-no-permission").replace("%ability%", finalDisplayName));
                                    }

                                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("abilities-menu.item-template.no-permission.sound");

                                    if (!soundName.isEmpty()) {
                                        try {
                                            Sound sound = Sound.valueOf(soundName);
                                            player.playSound(player.getLocation(), sound, 1, 1);
                                        } catch (IllegalArgumentException e) {
                                            getLogger().warning("The sound is invalid in guis.yml (abilities-menu/item-template/no-permission)");
                                        }
                                    }
                                } else if (DatabaseManager.isAbilitySelected(player.getUniqueId().toString(), abilityKey)) {
                                    if (!configUtil.getMessage("messages.abilities-menu.ability-already-selected").isEmpty()) {
                                        player.sendMessage(configUtil.getMessage("messages.abilities-menu.ability-already-selected").replace("%ability%", finalDisplayName));
                                    }
                                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("abilities-menu.item-template.selected.sound");

                                    if (!soundName.isEmpty()) {
                                        try {
                                            Sound sound = Sound.valueOf(soundName);
                                            player.playSound(player.getLocation(), sound, 1, 1);
                                        } catch (IllegalArgumentException e) {
                                            getLogger().warning("The sound is invalid in guis.yml (abilities-menu/item-template/selected)");
                                        }
                                    }
                                } else {
                                    DatabaseManager.saveSelectedAbility(player.getUniqueId().toString(), abilityKey);

                                    if (!configUtil.getMessage("messages.abilities-menu.ability-select").isEmpty()) {
                                        player.sendMessage(configUtil.getMessage("messages.abilities-menu.ability-select").replace("%ability%", finalDisplayName));
                                    }

                                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("abilities-menu.item-template.unselected.sound");

                                    if (!soundName.isEmpty()) {
                                        try {
                                            Sound sound = Sound.valueOf(soundName);
                                            player.playSound(player.getLocation(), sound, 1, 1);
                                        } catch (IllegalArgumentException e) {
                                            getLogger().warning("The sound is invalid in guis.yml (abilities-menu/item-template/unselected)");
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
