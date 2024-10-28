package hu.kxtsoo.fungun.events;

import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.abilities.AbilityHandler;
import hu.kxtsoo.fungun.abilities.DashAbility;
import hu.kxtsoo.fungun.database.DatabaseManager;
import hu.kxtsoo.fungun.guis.AbilitiesMenu;
import hu.kxtsoo.fungun.guis.EffectsMenu;
import hu.kxtsoo.fungun.manager.CooldownManager;
import hu.kxtsoo.fungun.model.FunGunItem;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getLogger;

public class PlayerInteractListener implements Listener {

    private final JavaPlugin plugin;
    private final CooldownManager cooldownHandler;
    private final Map<String, AbilityHandler> abilities = new HashMap<>();

    public PlayerInteractListener(JavaPlugin plugin, CooldownManager cooldownHandler) {
        this.plugin = plugin;
        this.cooldownHandler = cooldownHandler;
        abilities.put("DASH", new DashAbility(cooldownHandler));
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) throws SQLException {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        boolean abilitiesEnabled = FunGun.getInstance().getConfigUtil().getConfig().getBoolean("fungun.options.abilities.enabled", true);
        String abilityStyle = FunGun.getInstance().getConfigUtil().getConfig().getString("fungun.options.abilities.ability", "switchable");
        String defaultAbility = FunGun.getInstance().getConfigUtil().getConfig().getString("fungun.options.abilities.default-ability", "none");

        if (FunGunItem.isFunGunItem(itemInHand) && event.getAction().toString().contains("RIGHT")) {
            String styleOption = FunGun.getInstance().getConfig().getString("fungun.options.style");

            if ("switchable".equalsIgnoreCase(styleOption)) {
                if (player.isSneaking()) {
                    new EffectsMenu().openMenu(player);
                    String openMessage = FunGun.getInstance().getConfigUtil().getMessage("messages.effects-menu.open-message");
                    if (!openMessage.isEmpty()) {
                        player.sendMessage(openMessage);
                    }
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("effects-menu.open-sound");

                    if (!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1, 1);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("The sound is invalid in guis.yml (effects-menu/open-sound)");
                        }
                    }
                } else {
                    handleGunUse(player);
                }
            } else {
                handleGunUse(player);
            }

            event.setCancelled(true);
        }

        if (FunGunItem.isFunGunItem(itemInHand) && event.getAction().toString().contains("LEFT")) {

            if (!abilitiesEnabled) {
                return;
            }

            String selectedAbility = DatabaseManager.getSelectedAbility(player.getUniqueId().toString());
            AbilityHandler ability;

            if (!"switchable".equalsIgnoreCase(abilityStyle)) {
                if ("none".equalsIgnoreCase(abilityStyle)) {
                    player.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.abilities-menu.ability-not-selected"));
                    return;
                }
                ability = FunGun.getInstance().getConfigUtil().getAbilityHandler(abilityStyle);
                if (ability != null) {
                    ability.useAbility(player);
                } else {
                    getLogger().warning("&cNo ability found for: " + abilityStyle);
                }
            } else {
                if (player.isSneaking()) {
                    new AbilitiesMenu().openMenu(player);
                    String openMessage = FunGun.getInstance().getConfigUtil().getMessage("messages.abilities-menu.open-message");
                    if (!openMessage.isEmpty()) {
                        player.sendMessage(openMessage);
                    }
                    String soundName = FunGun.getInstance().getConfigUtil().getGUIs().getString("abilities-menu.open-sound");
                    if (!soundName.isEmpty()) {
                        try {
                            Sound sound = Sound.valueOf(soundName);
                            player.playSound(player.getLocation(), sound, 1, 1);
                        } catch (IllegalArgumentException e) {
                            getLogger().warning("The sound is invalid in guis.yml (abilities-menu/open-sound)");
                        }
                    }
                } else {
                    if (selectedAbility == null || selectedAbility.isEmpty()) {
                        if (!"none".equalsIgnoreCase(defaultAbility)) {
                            ability = FunGun.getInstance().getConfigUtil().getAbilityHandler(defaultAbility);
                        } else {
                            player.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.abilities-menu.ability-not-selected"));
                            return;
                        }
                    } else {
                        ability = FunGun.getInstance().getConfigUtil().getAbilityHandler(selectedAbility);
                    }

                    if (ability != null) {
                        ability.useAbility(player);
                    } else {
                        getLogger().warning("&cNo ability found for: " + (selectedAbility != null ? selectedAbility : defaultAbility));
                    }
                }
            }

            event.setCancelled(true);
        }
    }

    private void handleGunUse(Player player) {
        if (!player.hasPermission("mcfungun.use")) {
            player.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.fungun.no-use-permission"));
            return;
        }

        if (cooldownHandler.hasEffectCooldown(player)) {
            double remainingCooldown = cooldownHandler.getRemainingEffectCooldown(player);
            if (remainingCooldown > 0 && remainingCooldown == Math.floor(remainingCooldown)) {
                player.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.fungun.effect-cooldown")
                        .replace("%cooldown%", String.valueOf((int) remainingCooldown)));
            }
            return;
        }

        String styleOption = FunGun.getInstance().getConfig().getString("fungun.options.style");
        String selectedEffect;

        if ("switchable".equalsIgnoreCase(styleOption)) {
            try {
                selectedEffect = DatabaseManager.getSelectedEffect(player.getUniqueId().toString());
            } catch (SQLException e) {
                player.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.database-error"));
                e.printStackTrace();
                return;
            }

            if (selectedEffect == null || selectedEffect.isEmpty()) {
                selectedEffect = FunGun.getInstance().getConfig().getString("fungun.options.default-style");
                if (selectedEffect == null || selectedEffect.isEmpty()) {
                    getLogger().warning("No default effect set in config for FunGun.");
                    return;
                }
            }
        } else {
            selectedEffect = styleOption;
            if (selectedEffect == null || selectedEffect.isEmpty()) {
                player.sendMessage(FunGun.getInstance().getConfigUtil().getMessage("messages.fungun.no-effect-configured"));
                return;
            }
        }

        launchFunGunProjectile(player);
        cooldownHandler.startEffectCooldown(player, FunGun.getInstance().getConfigUtil().getEffectCooldown(selectedEffect));
    }

    private void launchFunGunProjectile(Player player) {
        Snowball snowball = player.launchProjectile(Snowball.class);
        snowball.setVelocity(player.getLocation().getDirection().multiply(2));
        snowball.setMetadata("FunGunShot", new FixedMetadataValue(plugin, true));
        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);
    }
}