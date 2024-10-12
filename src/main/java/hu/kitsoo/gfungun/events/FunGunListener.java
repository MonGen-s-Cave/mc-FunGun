package hu.kitsoo.gfungun.events;

import hu.kitsoo.gfungun.util.ChatUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.stream.Collectors;

public class FunGunListener implements Listener {

    private final JavaPlugin plugin;
    private final CooldownListener cooldownHandler;

    public FunGunListener(JavaPlugin plugin, CooldownListener cooldownHandler) {
        this.plugin = plugin;
        this.cooldownHandler = cooldownHandler;
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        giveFunGun(player);
    }

    private void giveFunGun(Player player) {
        int slot = plugin.getConfig().getInt("fungun.options.slot") - 1;
        if (slot >= 0 && slot < 9) {
            ItemStack funGunItem = createFunGunItem();
            player.getInventory().setItem(slot, funGunItem);
        } else {
            plugin.getLogger().warning(ChatUtil.colorizeHex("Invalid slot number in config.yml. Must be between 1 and 9."));
        }
    }

        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            String prefix = this.plugin.getConfig().getString("prefix");

            if (event.getAction().toString().contains("RIGHT")) {
                ItemStack configItem = createFunGunItem();
                if (itemInHand.isSimilar(configItem)) {
                    String requiredPermission = plugin.getConfig().getString("fungun.options.permission", "gfungun.use");
                    if (!player.hasPermission(requiredPermission)) {
                        String permissionDeniedMessage = plugin.getConfig().getString("fungun.options.permission-denied", "&cYou don't have permission to use this.");
                        player.sendMessage(ChatUtil.colorizeHex(prefix + permissionDeniedMessage));
                        return;
                    }

                    event.setCancelled(true);

                    if (cooldownHandler.hasCooldown(player)) {
                        String remainingCooldownMessage = plugin.getConfig().getString("fungun.options.cooldown-system.chat-message", "&cYou can use the FunGun after %duration% seconds.");
                        double remainingCooldown = cooldownHandler.getRemainingCooldown(player);
                        String message = remainingCooldownMessage.replace("%duration%", String.valueOf(remainingCooldown));
                        player.sendMessage(ChatUtil.colorizeHex(prefix + message));
                        return;
                    }

                    Snowball snowball = player.launchProjectile(Snowball.class);
                    snowball.setVelocity(player.getLocation().getDirection().multiply(2));
                    snowball.setMetadata("FunGunShot", new FixedMetadataValue(plugin, true));
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.0f);

                    int cooldownSeconds = plugin.getConfig().getInt("fungun.options.cooldown-system.cooldown", 5);
                    cooldownHandler.startCooldown(player, cooldownSeconds);
                }
            }
        }



    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();

        if (projectile instanceof Snowball snowball && projectile.hasMetadata("FunGunShot")) {
            giveFunGun(snowball.getShooter() instanceof Player ? (Player) snowball.getShooter() : null);

            World world = snowball.getWorld();
            Location location = snowball.getLocation();

            String style = plugin.getConfig().getString("fungun.options.style", "LAVA").toLowerCase();

            switch (style.toUpperCase()) {
                case "LAVA":
                    world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
                    world.spawnParticle(Particle.FLAME, location, 50);
                    world.spawnParticle(Particle.LAVA, location, 20);
                    world.spawnParticle(Particle.HEART, location, 30);
                    world.spawnParticle(Particle.ENCHANTMENT_TABLE, location, 30);
                    world.spawnParticle(Particle.PORTAL, location, 30);
                    world.playSound(location, Sound.ENTITY_CAT_AMBIENT, 1.0f, 1.0f);
                    break;
                case "AQUA":
                    world.playSound(location, Sound.ENTITY_DOLPHIN_AMBIENT, 1.0f, 1.0f);
                    world.spawnParticle(Particle.WATER_SPLASH, location, 100, 1, 1, 1, 0);
                    world.spawnParticle(Particle.BUBBLE_POP, location, 50, 1, 1, 1, 0);
                    world.spawnParticle(Particle.NAUTILUS, location, 30, 1, 1, 1, 0);
                    world.spawnParticle(Particle.DRIP_WATER, location, 30, 1, 1, 1, 0);
                    world.playSound(location, Sound.BLOCK_WATER_AMBIENT, 1.0f, 1.0f);

                    world.spawnParticle(Particle.BUBBLE_COLUMN_UP, location, 50, 0.5, 2.0, 0.5, 0);
                    world.spawnParticle(Particle.WATER_BUBBLE, location, 50, 0.5, 2.0, 0.5, 0);
                    world.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0f, 1.0f);
                    break;
                case "COSMIC":
                    world.playSound(location, Sound.ENTITY_ARROW_SHOOT, 1.0f, 1.0f);
                    world.spawnParticle(Particle.CRIT_MAGIC, location, 100, 1, 1, 1, 0);
                    world.spawnParticle(Particle.ENCHANTMENT_TABLE, location, 50, 1, 1, 1, 0);
                    world.spawnParticle(Particle.END_ROD, location, 30, 1, 1, 1, 0);
                    break;
                case "MYSTICAL":
                    world.playSound(location, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.0f);
                    world.spawnParticle(Particle.SPELL_WITCH, location, 100, 1, 1, 1, 0);
                    world.spawnParticle(Particle.PORTAL, location, 50, 1, 1, 1, 0);
                    world.spawnParticle(Particle.SMOKE_LARGE, location, 30, 1, 1, 1, 0);
                    break;
                case "FIERY":
                    world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f);
                    world.spawnParticle(Particle.FLAME, location, 100, 1, 1, 1, 0);
                    world.spawnParticle(Particle.LAVA, location, 50, 1, 1, 1, 0);
                    world.spawnParticle(Particle.SMOKE_LARGE, location, 30, 1, 1, 1, 0);
                    break;
                case "FROSTY":
                    world.playSound(location, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
                    world.spawnParticle(Particle.SNOWBALL, location, 100, 1, 1, 1, 0);
                    world.spawnParticle(Particle.SNOWFLAKE, location, 50, 1, 1, 1, 0);
                    world.spawnParticle(Particle.CLOUD, location, 30, 1, 1, 1, 0);
                    world.playSound(location, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
                    world.playSound(location, Sound.BLOCK_SNOW_BREAK, 1.0f, 1.0f);
                    break;
                case "GALACTIC":
                    world.playSound(location, Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0f, 1.0f);
                    world.spawnParticle(Particle.END_ROD, location, 100, 1, 1, 1, 0);
                    world.spawnParticle(Particle.PORTAL, location, 50, 1, 1, 1, 0);
                    world.spawnParticle(Particle.REVERSE_PORTAL, location, 30, 1, 1, 1, 0);
                    break;
                case "STORMY":
                    world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                    world.spawnParticle(Particle.VILLAGER_ANGRY, location, 100, 1, 1, 1, 0);
                    world.spawnParticle(Particle.SMOKE_NORMAL, location, 50, 1, 1, 1, 0);
                    world.spawnParticle(Particle.WATER_SPLASH, location, 30, 1, 1, 1, 0);
                    world.playSound(location, Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 1.0f, 1.0f);
                    break;
                case "ECLIPSE":
                    world.playSound(location, Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                    world.spawnParticle(Particle.ASH, location, 100, 1, 1, 1, 0);
                    world.spawnParticle(Particle.COMPOSTER, location, 50, 1, 1, 1, 0);
                    world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, location, 30, 1, 1, 1, 0);
                    break;
                case "AURORA":
                    world.playSound(location, Sound.BLOCK_BELL_USE, 1.0f, 1.0f);
                    world.spawnParticle(Particle.SPELL_MOB_AMBIENT, location, 100, 1, 1, 1, 1);
                    world.spawnParticle(Particle.GLOW, location, 50, 1, 1, 1, 0);
                    world.spawnParticle(Particle.NAUTILUS, location, 30, 1, 1, 1, 0);
                    break;
                case "WIZARDS_FANTASY":
                    world.playSound(location, Sound.ENTITY_EVOKER_CAST_SPELL, 1.0f, 1.0f);
                    world.spawnParticle(Particle.ENCHANTMENT_TABLE, location, 200, 1, 1, 1, 0.1);
                    world.spawnParticle(Particle.CRIT_MAGIC, location, 150, 1, 1, 1, 0.1);
                    world.spawnParticle(Particle.SPELL, location, 100, 1, 1, 1, 0.1);
                    break;
                case "EMBERS":
                    world.playSound(location, Sound.BLOCK_REDSTONE_TORCH_BURNOUT, 1.0f, 1.0f);
                    Particle.DustOptions redstoneColor = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0f);
                    world.spawnParticle(Particle.REDSTONE, location, 100, 0.5, 0.5, 0.5, redstoneColor);
                    world.spawnParticle(Particle.FLAME, location, 50, 0.5, 0.5, 0.5, 0.05);
                    world.spawnParticle(Particle.LAVA, location, 30, 0.5, 0.5, 0.5, 0.05);
                    break;
                case "WARDEN_FURY":
                    world.playSound(location, Sound.ENTITY_WARDEN_AMBIENT, 1.0f, 1.0f);
                    world.spawnParticle(Particle.SMOKE_LARGE, location, 100, 1, 1, 1, 0.05);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 150, 1, 1, 1, 0.1);
                    world.spawnParticle(Particle.PORTAL, location, 200, 1, 1, 1, 0.1);

                    world.spawnParticle(Particle.SMOKE_NORMAL, location, 100, 1, 1, 1, 0.1); // Alternatív hatás

                    world.spawnParticle(Particle.ASH, location, 50, 1, 1, 1, 0.05);
                    world.spawnParticle(Particle.CRIT_MAGIC, location, 100, 1, 1, 1, 0.1);
                    world.spawnParticle(Particle.REVERSE_PORTAL, location, 50, 1, 1, 1, 0.05);
                    world.spawnParticle(Particle.SOUL_FIRE_FLAME, location, 200, 1.5, 1.5, 1.5, 0.15);
                    world.spawnParticle(Particle.SMOKE_LARGE, location, 150, 1.5, 1.5, 1.5, 0.07);
                    world.playSound(location, Sound.ENTITY_WARDEN_HURT, 0.8f, 0.8f);
                    world.playSound(location, Sound.BLOCK_SCULK_SENSOR_CLICKING, 0.5f, 1.0f);
                    break;
                default:
                    plugin.getLogger().warning("Unknown FunGun style: " + style);
                    break;
            }
        }
    }

@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    Player player = (Player) event.getWhoClicked();
    ItemStack clickedItem = event.getCurrentItem();
    ItemStack funGunItem = createFunGunItem();

    if (player.getGameMode() != GameMode.SURVIVAL) {
        return;
    }

    if (event.getClickedInventory() != null && (event.getClickedInventory().getType() == InventoryType.CRAFTING || event.getClickedInventory().getType() == InventoryType.PLAYER)) {
        if (clickedItem != null && clickedItem.isSimilar(funGunItem)) {
            event.setCancelled(true);
            return;
        }
    }
    if (event.getClick().equals(ClickType.NUMBER_KEY)) {
        ItemStack hotbarItem = player.getInventory().getItem(event.getHotbarButton());
        if (hotbarItem != null && hotbarItem.isSimilar(funGunItem)) {
            event.setCancelled(true);
        }
    }
}

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        ItemStack funGunItem = createFunGunItem();

        if (droppedItem.isSimilar(funGunItem)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        ItemStack offhandItem = event.getOffHandItem();
        ItemStack funGunItem = createFunGunItem();

        if (offhandItem != null && offhandItem.isSimilar(funGunItem)) {
            event.setCancelled(true);
        }
    }

    private ItemStack createFunGunItem() {
        Material itemType = Material.getMaterial(plugin.getConfig().getString("fungun.item", "BLAZE_ROD"));
        String itemName = ChatUtil.colorizeHex(plugin.getConfig().getString("fungun.name"));
        List<String> itemLore = plugin.getConfig().getStringList("fungun.lore");

        assert itemType != null;
        ItemStack item = new ItemStack(itemType);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(itemName);

            List<String> coloredLore = itemLore.stream()
                    .map(ChatUtil::colorizeHex)
                    .collect(Collectors.toList());

            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }

        return item;
    }
}