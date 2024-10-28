package hu.kxtsoo.fungun.guis;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.database.DatabaseManager;
import hu.kxtsoo.fungun.util.ChatUtil;
import hu.kxtsoo.fungun.util.ConfigUtil;
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

        String title = ChatUtil.colorizeHex(guis.getString("abilities-menu.title", "&aFunGun Abilities Menu"));
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

        int prevSlot = guis.getInt("abilities-menu.navigation.previous-page.slot", 19);
        int nextSlot = guis.getInt("abilities-menu.navigation.next-page.slot", 25);

        Material prevMaterial = Material.valueOf(prevItem);
        Material nextMaterial = Material.valueOf(nextItem);

        GuiItem previousPage = ItemBuilder.from(prevMaterial)
                .name(Component.text(ChatUtil.colorizeHex(guis.getString("abilities-menu.navigation.previous-page.name", "&aPrevious Page"))))
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

        GuiItem nextPage = ItemBuilder.from(nextMaterial)
                .name(Component.text(ChatUtil.colorizeHex(guis.getString("abilities-menu.navigation.next-page.name", "&aNext Page"))))
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

        gui.setItem(prevSlot, previousPage);
        gui.setItem(nextSlot, nextPage);

        int closeSlot = guis.getInt("abilities-menu.close-item.slot", 22);
        String closeItem = guis.getString("abilities-menu.close-item.item", "BARRIER");
        Material closeMaterial = Material.valueOf(closeItem);

        GuiItem closeMenu = ItemBuilder.from(closeMaterial)
                .name(Component.text(ChatUtil.colorizeHex(guis.getString("abilities-menu.close-item.name", "&cClose Menu"))))
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

        gui.setItem(closeSlot, closeMenu);

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

                String displayName = ChatUtil.colorizeHex(decoration.getString("title", "&r"));
                List<String> lore = decoration.getStringList("lore").stream()
                        .map(ChatUtil::colorizeHex)
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

                String displayName = ChatUtil.colorizeHex(abilityDoc.getString("ability.display.display-name", "&cUnknown Ability"));
                List<String> descriptionList = abilityDoc.getStringList("ability.display.description");

                List<String> lore = new ArrayList<>();
                String itemDisplayName = "";

                if (!player.hasPermission("fungun.ability." + abilityKey)) {
                    itemDisplayName = guis.getString("abilities-menu.item-template.no-permission.title", "%display-name%")
                            .replace("%display-name%", displayName);

                    List<String> loreTemplate = guis.getStringList("abilities-menu.item-template.no-permission.description");
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

                    List<String> loreTemplate = guis.getStringList("abilities-menu.item-template.selected.description");
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

                    List<String> loreTemplate = guis.getStringList("abilities-menu.item-template.unselected.description");
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
                                if (!player.hasPermission("fungun.ability." + abilityKey)) {
                                    if (!configUtil.getMessage("messages.abilities-menu.ability-no-permission").isEmpty()) {
                                        player.sendMessage(configUtil.getMessage("messages.abilities-menu.ability-no-permission").replace("%ability%", displayName));
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
                                        player.sendMessage(configUtil.getMessage("messages.abilities-menu.ability-already-selected").replace("%ability%", displayName));
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
                                        player.sendMessage(configUtil.getMessage("messages.abilities-menu.ability-select").replace("%ability%", displayName));
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
