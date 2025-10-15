package hu.kxtsoo.fungun.listener;

import dev.dejvokep.boostedyaml.YamlDocument;
import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.database.DatabaseManager;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class ProjectileHitListener implements Listener {

    private final JavaPlugin plugin;

    public ProjectileHitListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball && snowball.hasMetadata("FunGunShot")) {
            handleProjectileHit(snowball);
        }
    }

    private void handleProjectileHit(Snowball snowball) {
        Player shooter = snowball.getShooter() instanceof Player ? (Player) snowball.getShooter() : null;

        if (shooter == null) {
            getLogger().warning("No shooter associated with the FunGun shot.");
            return;
        }

        World world = snowball.getWorld();
        Location location = snowball.getLocation();

        String styleOption = FunGun.getInstance().getConfig().getString("fungun.options.style");
        String selectedEffect;

        if ("switchable".equalsIgnoreCase(styleOption)) {
            try {
                selectedEffect = DatabaseManager.getSelectedEffect(shooter.getUniqueId().toString());
            } catch (SQLException e) {
                getLogger().severe("Failed to retrieve selected effect for player: " + e.getMessage());
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
                getLogger().warning("No effect configured in the config file.");
                return;
            }
        }

        YamlDocument effect = FunGun.getInstance().getConfigUtil().getEffect(selectedEffect);
        if (effect != null) {
            applyEffect(world, location, effect);
        } else {
            getLogger().warning("Unknown FunGun effect: " + selectedEffect);
        }
    }

    private void applyEffect(World world, Location location, YamlDocument effect) {
        List<String> sounds = effect.getStringList("sounds");
        List<String> particles = effect.getStringList("particles");

        for (String sound : sounds) {
            String[] parts = sound.split(",");
            Sound effectSound = Sound.valueOf(parts[0]);
            float volume = Float.parseFloat(parts[1]);
            float pitch = Float.parseFloat(parts[2]);
            world.playSound(location, effectSound, volume, pitch);
        }

        for (String particleConfig : particles) {
            String[] parts = particleConfig.split(",");

            if (parts.length < 6) {
                getLogger().warning("Particle config has insufficient data: " + particleConfig);
                continue;
            }

            Particle effectParticle = Particle.valueOf(parts[0]);
            int count = Integer.parseInt(parts[1]);
            double offsetX = Double.parseDouble(parts[2]);
            double offsetY = Double.parseDouble(parts[3]);
            double offsetZ = Double.parseDouble(parts[4]);

            if (effectParticle == Particle.REDSTONE) {
                try {
                    String[] colorParts = parts[5].split(",");
                    if (colorParts.length != 3) {
                        throw new IllegalArgumentException("Invalid color format for REDSTONE particle.");
                    }
                    int red = Integer.parseInt(colorParts[0].trim());
                    int green = Integer.parseInt(colorParts[1].trim());
                    int blue = Integer.parseInt(colorParts[2].trim());

                    if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
                        throw new IllegalArgumentException("RGB values must be between 0 and 255.");
                    }

                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(red, green, blue), 1.0F);
                    world.spawnParticle(effectParticle, location, count, offsetX, offsetY, offsetZ, dustOptions);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (effectParticle == Particle.BLOCK_CRACK || effectParticle == Particle.BLOCK_DUST || effectParticle == Particle.FALLING_DUST) {
                try {
                    Material material = Material.matchMaterial(parts[5]);
                    if (material == null) {
                        throw new IllegalArgumentException("Invalid block type: " + parts[5]);
                    }
                    BlockData blockData = material.createBlockData();
                    world.spawnParticle(effectParticle, location, count, offsetX, offsetY, offsetZ, blockData);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                double extra = Double.parseDouble(parts[5]);
                world.spawnParticle(effectParticle, location, count, offsetX, offsetY, offsetZ, extra);
            }
        }
    }
}
